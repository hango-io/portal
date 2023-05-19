package org.hango.cloud.gdashboard.api.service.impl.swagger;

import com.google.common.collect.Maps;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerDetailsDto;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.service.IGetProjectIdService;
import org.hango.cloud.gdashboard.api.service.swagger.ISwaggerModelService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SwaggerModelServiceImpl implements ISwaggerModelService {
    @Autowired
    private IApiModelService apiModelService;
    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IGetProjectIdService projectIdService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addSwaggerApiModels(List<CreateApiModelDto> apiModelDtos) {
        Map<String, Long> apiModelType = Maps.newHashMap();
        Map<String, Long> modelIdMap = Maps.newHashMap();
        //依次加入基本类型,paramType
        if (!CollectionUtils.isEmpty(apiModelDtos)) {
            apiModelDtos.forEach(createApiModelDto ->
            {
                //构造模型id
                long modelId = 0;
                ApiModel apiModel = apiModelService.getApiModelByServiceIdAndModelName(createApiModelDto.getServiceId(), createApiModelDto.getModelName());
                //如果存在当前model,serviceId,modelName相同，则更新APIModel,否则插入新的model
                if (apiModel != null) {
                    //如果是用户自己创建的数据模型，则以用户自己创建的数据模型为准，否则修改数据模型
                    if (apiModel.getSwaggerSync() == 0) {
                        return;
                    } else {
                        modelId = updateSwagggerApiModel(createApiModelDto, apiModel).getId();
                    }
                } else {
                    //模型id,新创建模型标识1，表示从swagger同步
                    apiModel = apiModelService.getApiModel(createApiModelDto);
                    apiModel.setSwaggerSync(1);
                    modelId = apiModelService.addApiModelBasic(apiModel);
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
        }
        //依次加入model Param
        if (!CollectionUtils.isEmpty(apiModelDtos)) {
            apiModelDtos.forEach(createApiModelDto ->
            {
                //如果是用户自己创建的数据模型，则以用户自己创建的数据模型为准，否则修改数据模型
                ApiModel apiModel = apiModelService.getApiModelByServiceIdAndModelName(createApiModelDto.getServiceId(), createApiModelDto.getModelName());
                if (apiModel != null && apiModel.getSwaggerSync() == 0) {
                    return;
                }
                apiModelService.addApiModelParam(createApiModelDto, modelIdMap.get(createApiModelDto.getModelName()), apiModelType);
            });
        }
    }

    @Override
    public void swaggerSyncToNotSync(List<CreateApiModelDto> apiModelDtos) {
        long serviceId = 0;
        if (!CollectionUtils.isEmpty(apiModelDtos)) {
            serviceId = apiModelDtos.get(0).getServiceId();
        }
        List<ApiModel> apiModels = apiModelService.findApiModelBySwaggerSync(serviceId, 1);
        apiModels.forEach(apiModel -> {
            apiModel.setSwaggerSync(2);
            apiModelService.updateApiModels(apiModel);
        });
    }

    @Override
    public ApiModel updateSwagggerApiModel(CreateApiModelDto createApiModelDto, ApiModel apiModel) {
        if (createApiModelDto.getDescription() != null) {
            apiModel.setDescription(createApiModelDto.getDescription());
        } else {
            apiModel.setDescription("该模型由swagger自动创建");
        }
        apiModel.setSwaggerSync(1);
        apiModel.setModifyDate(System.currentTimeMillis());
        apiModel.setProjectId(projectIdService.getProjectId());
        //更新APIModel
        apiModelService.updateApiModels(apiModel);
        //删除其参数，然后重新添加
        apiModelService.deleteApiModelByModelId(apiModel.getId(), false);
        return apiModel;
    }

    @Override
    public void addApiModel(List<CreateApiModelDto> createApiModelDtos, long serviceId) {
        createApiModelDtos.forEach(createApiModelDto -> {
            createApiModelDto.setServiceId(serviceId);
        });
        swaggerSyncToNotSync(createApiModelDtos);
        addSwaggerApiModels(createApiModelDtos);
    }

    @Override
    public List<SwaggerDetailsDto> getConflixApiModel(List<CreateApiModelDto> apiModelDtos, long serviceId, String serviceName) {
        List<SwaggerDetailsDto> conflixApiModel = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiModelDtos)) {
            apiModelDtos.forEach(createApiModelDto -> {
                ApiModel apiModel = apiModelService.getApiModelByServiceIdAndModelName(serviceId, createApiModelDto.getModelName());
                if (apiModel != null && apiModel.getSwaggerSync() == 0) {
                    SwaggerDetailsDto swaggerDetailsDto = new SwaggerDetailsDto(Const.MODEL_TYPE, apiModel.getModelName(),
                            Const.SWAGGER_SYNC_CONFLICT);
                    StringBuilder message = new StringBuilder();
                    message.append("冲突:").append(apiModel.getModelName()).append("(所属服务:").
                            append(serviceName).append(")");
                    swaggerDetailsDto.setMessage(message.toString());
                    conflixApiModel.add(swaggerDetailsDto);
                }
            });
        }
        return conflixApiModel;
    }

    @Override
    public List<SwaggerDetailsDto> getOverrideApiModel(List<CreateApiModelDto> apiModelDtos, long serviceId) {
        List<SwaggerDetailsDto> overrideApiModels = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiModelDtos)) {
            apiModelDtos.forEach(createApiModelDto -> {
                ApiModel apiModel = apiModelService.getApiModelByServiceIdAndModelName(serviceId, createApiModelDto.getModelName());
                if (apiModel != null && apiModel.getSwaggerSync() != 0) {
                    SwaggerDetailsDto swaggerDetailsDto = new SwaggerDetailsDto(Const.MODEL_TYPE, apiModel.getModelName(),
                            Const.SWAGGER_SYNC_COVER);
                    overrideApiModels.add(swaggerDetailsDto);
                }
            });
        }
        return overrideApiModels;
    }

    @Override
    public List<SwaggerDetailsDto> getNewApiModel(List<CreateApiModelDto> apiModelDtos, long serviceId) {
        List<SwaggerDetailsDto> newApiModels = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiModelDtos)) {
            apiModelDtos.forEach(createApiModelDto -> {
                if (apiModelService.getApiModelByServiceIdAndModelName(serviceId, createApiModelDto.getModelName()) == null) {
                    SwaggerDetailsDto swaggerDetailsDto = new SwaggerDetailsDto(Const.MODEL_TYPE, createApiModelDto.getModelName(),
                            Const.SWAGGER_SYNC_NEW);
                    newApiModels.add(swaggerDetailsDto);
                }
            });
        }
        return newApiModels;
    }
}
