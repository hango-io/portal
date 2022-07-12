package org.hango.cloud.gdashboard.api.service.impl;

import org.hango.cloud.gdashboard.api.dto.ApiParamDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.service.IApiModelExportService;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * ApiModel 导入导出相关service
 *
 * @author hanjiahao
 */
@Service
public class ApiModelExportServiceImpl implements IApiModelExportService {
    @Autowired
    private IApiModelService apiModelService;
    @Autowired
    private IApiParamTypeService apiParamTypeService;

    /**
     * 判断key是否重复
     *
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> same = new HashMap<>();
        return object -> same.putIfAbsent(keyExtractor.apply(object), Boolean.TRUE) == null;
    }

    @Override
    public List<CreateApiModelDto> getApiModels(long serviceId) {
        List<ApiModel> apiModels = apiModelService.getApiModelByServiceId(serviceId);
        if (CollectionUtils.isEmpty(apiModels)) return new ArrayList<>();

        List<CreateApiModelDto> createApiModelDtos = new ArrayList<>();
        apiModels.forEach(apiModel -> {
            CreateApiModelDto createApiModelDto = new CreateApiModelDto();
            createApiModelDto.setModelName(apiModel.getModelName());
            createApiModelDto.setDescription(apiModel.getDescription());
            createApiModelDto.setParams(apiModelService.getApiModelParamsByModelId(apiModel.getId()));
            createApiModelDtos.add(createApiModelDto);
        });
        return createApiModelDtos;
    }

    @Override
    public void addApiModel(List<CreateApiModelDto> createApiModelDtos, long serviceId) {
        if (CollectionUtils.isEmpty(createApiModelDtos)) return;
        //去除重复的model name
        List<CreateApiModelDto> collect = createApiModelDtos.stream().filter(distinctByKey(CreateApiModelDto::getModelName)).collect(Collectors.toList());
        collect.forEach(createApiModelDto -> {
            createApiModelDto.setServiceId(serviceId);
        });

        addJsonModel(collect);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addJsonModel(List<CreateApiModelDto> createApiModelDtos) {
        Map<String, Long> apiModelType = new HashMap<>();
        Map<String, Long> modelIdMap = new HashMap<>();
        if (CollectionUtils.isEmpty(createApiModelDtos)) return;
        createApiModelDtos.forEach(createApiModelDto -> {
            long modelId = 0;
            ApiModel apiModel = apiModelService.getApiModelByServiceIdAndModelName(createApiModelDto.getServiceId(), createApiModelDto.getModelName());
            if (apiModel == null) {
                apiModel = apiModelService.getApiModel(createApiModelDto);
                modelId = apiModelService.addApiModelBasic(apiModel);
            } else {
                //更新api model，删除model中的param
                modelId = apiModel.getId();
                apiModel.setDescription(createApiModelDto.getDescription());
                apiModel.setModifyDate(System.currentTimeMillis());
                apiModelService.updateApiModels(apiModel);
                apiModelService.deleteApiModelByModelId(apiModel.getId(), false);
            }
            //构造paramType ID
            long modelTypeId = 0;
            ApiParamType apiParamType = apiParamTypeService.listApiParamTypeByModelId(createApiModelDto.getModelName(), modelId);
            if (apiParamType != null) {
                modelTypeId = apiParamType.getId();
            } else {
                //param_type id
                modelTypeId = apiModelService.addApiParamModel(createApiModelDto, modelId);
            }
            apiModelType.put(createApiModelDto.getModelName(), modelTypeId);
            modelIdMap.put(createApiModelDto.getModelName(), modelId);
        });

        //依次加入model Param
        if (!CollectionUtils.isEmpty(createApiModelDtos)) {
            createApiModelDtos.forEach(createApiModelDto ->
            {
                //如果之前有，则覆盖。。。
                List<ApiParamDto> params = createApiModelDto.getParams();
                if (!CollectionUtils.isEmpty(params)) {
                    params.forEach(apiParamDto -> {
                        apiParamDto.setParamTypeId(apiParamTypeService.generateExactByService(apiParamDto.getParamTypeName(), createApiModelDto.getServiceId()));
                        apiParamDto.setArrayDataTypeId(apiParamTypeService.generateExactByService(apiParamDto.getArrayDataTypeName(), createApiModelDto.getServiceId()));
                    });
                }
                apiModelService.addApiModelParam(createApiModelDto, modelIdMap.get(createApiModelDto.getModelName()), apiModelType);
            });
        }
    }

}
