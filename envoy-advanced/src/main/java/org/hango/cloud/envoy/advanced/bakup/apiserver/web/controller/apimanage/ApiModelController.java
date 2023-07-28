package org.hango.cloud.envoy.advanced.bakup.apiserver.web.controller.apimanage;

import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.operationaudit.annotation.Audit;
import org.hango.cloud.common.infra.operationaudit.meta.ResourceDataDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.envoy.advanced.bakup.apiserver.util.BeanUtil;
import org.hango.cloud.envoy.advanced.bakup.apiserver.util.Const;
import org.hango.cloud.gdashboard.api.dto.ApiParamDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.meta.errorcode.AbstractErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/26 下午2:46.
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class ApiModelController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(ApiModelController.class);

    @Autowired
    private IApiModelService apiModelService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IApiParamTypeService apiParamTypeService;

    /**
     * 创建新数据模型
     *
     * @param createApiModelDto
     * @return
     */
    @RequestMapping(params = {"Action=CreateApiModel"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateApiModel", description = "创建模型")
    public Object addModel(@Validated @RequestBody CreateApiModelDto createApiModelDto) {
        logger.info("请求创建模型，创建的数据模型：{}", createApiModelDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API_MODEL, null, createApiModelDto.getModelName());

        //服务id校验
        if (serviceProxyService.get(createApiModelDto.getServiceId()) == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        //参数检查
        AbstractErrorCode code = apiModelService.checkAddApiModelParam(createApiModelDto);
        if (!CommonApiErrorCode.Success.equals(code)) {
            return apiReturn(code);
        }

        //判断ModelName是否存在
        if (apiModelService.isApiModelExists(createApiModelDto.getModelName(), createApiModelDto.getServiceId())) {
            code = CommonApiErrorCode.ModelNameAlreadyExist;
            return apiReturn(code);
        }

        long modelId = apiModelService.addApiModel(createApiModelDto);

        Map<String, Object> response = Maps.newHashMap();

        response.put("ApiModelId", modelId);
        resource.setResourceId(modelId);
        return apiReturn(CommonErrorCode.SUCCESS, response);
    }


    /**
     * @param createApiModelDto
     * @return
     */
    @RequestMapping(params = {"Action=UpdateApiModel"}, method = RequestMethod.POST)
    @Audit(eventName = "ModifyApiModel", description = "修改模型")
    public Object modifyModel(@Validated @RequestBody CreateApiModelDto createApiModelDto) {
        //操作审计记录资源名称
        //AuditResourceHolder.set(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API_MODEL, createApiModelDto.getId(), createApiModelDto.getModelName()));

        //服务id校验
        if (serviceProxyService.get(createApiModelDto.getServiceId()) == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        //参数检查
        AbstractErrorCode code = apiModelService.checkAddApiModelParam(createApiModelDto);
        if (!CommonApiErrorCode.Success.equals(code)) {
            return apiReturn(code);
        }

        //根据Id查询模型基本信息
        ApiModel apiModel = apiModelService.getApiModelInfoByModelId(createApiModelDto.getId());
        //ModelId非法
        if (apiModel == null) {
            return apiReturn(CommonApiErrorCode.NoSuchModel);
        }

        //不能自引用，防止递归栈溢出
        ApiParamType apiParamType = apiParamTypeService.listModleParamType(apiModel.getId());
        if (apiParamType != null) {
            for (ApiParamDto apiParamDto : createApiModelDto.getParams()) {
                if (apiParamDto.getParamTypeId() == apiParamType.getId() ||
                        apiParamDto.getArrayDataTypeId() == apiParamType.getId()) {
                    code = CommonApiErrorCode.InvalidParamType;
                    return apiReturn(code);
                }
            }
        }

        //根据ModelName和serviceId判断是否重复
        ApiModel apiModelTemp = apiModelService.getApiModelByServiceIdAndModelName(createApiModelDto.getServiceId(), createApiModelDto.getModelName());
        if (apiModelTemp != null) {
            if (apiModelTemp.getId() != apiModel.getId()) {
                code = CommonApiErrorCode.InvalidParameterValueModelName(apiModelTemp.getModelName());
                return apiReturn(code);
            }
        }

        //判断是否修改了模型名，如果修改了，需要修改参数表
        boolean flag = !apiModel.getModelName().equals(createApiModelDto.getModelName());

        try {
            apiModelService.updateApiModel(createApiModelDto, createApiModelDto.getId(), apiModel.getModelName(), flag);

            return apiReturn(CommonErrorCode.SUCCESS);

        } catch (Exception e) {
            logger.error("更新数据模型失败！ModelId {}", createApiModelDto.getId(), e);
            code = CommonErrorCode.INTERNAL_SERVER_ERROR;
            return apiReturn(code);
        }

    }

    /**
     * 根据Id查询Model详情
     *
     * @param modelId
     * @return
     */
    @RequestMapping(params = {"Action=DescribeApiModel"}, method = RequestMethod.GET)
    public String describeApiModel(@RequestParam(value = "ModelId") long modelId) {
        logger.info("请求查询模型，modelId为：{}", modelId);

        //判断ModelId是否存在
        if (!apiModelService.isApiModelExists(modelId)) {
            return apiReturn(CommonApiErrorCode.NoSuchModel);
        }

        CreateApiModelDto createApiModelDto = apiModelService.getApiModelByModelId(modelId);

        //根据serviceId查询serviceName
        createApiModelDto.setDisplayName(serviceProxyService.get(createApiModelDto.getServiceId())
                .getName());

        Map<String, Object> response = Maps.newHashMap();

        response.put("ApiModelInfo", createApiModelDto);
        return apiReturn(CommonErrorCode.SUCCESS, response);
    }


    /**
     * 查询Model List
     *
     * @return
     */
    @RequestMapping(params = {"Action=DescribeModelList"}, method = RequestMethod.GET)
    public String describeApiModels(@RequestParam(value = "Pattern", required = false) String pattern,
                                    @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                    @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                                    @RequestParam(value = "ServiceId", required = false, defaultValue = "0") long serviceId) {

        logger.info("分页获取数据模型列表，offset:{},limit:{}", offset, limit);
        //offset,limit校验
        ErrorCode errorCode = CommonUtil.checkOffsetAndLimit(offset, limit);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }

        List<ApiModel> apiModelList = apiModelService.findAllApiModelByProjectLimit(serviceId, ProjectTraceHolder.getProId(), offset, limit, pattern);
        List<CreateApiModelDto> createApiModelDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiModelList)) {
            createApiModelDtos = BeanUtil.copyList(apiModelList, CreateApiModelDto.class);
            createApiModelDtos.forEach(createApiModelDto -> {
                ServiceProxyDto serviceProxyDto = serviceProxyService.get(createApiModelDto.getServiceId());
                if (serviceProxyDto != null){
                    createApiModelDto.setDisplayName(serviceProxyDto.getName());
                }
            });
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("TotalCount", apiModelService.getApiModelCountByProjectOrService(serviceId, ProjectTraceHolder.getProId(), pattern));
        result.put("ModelList", createApiModelDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    /**
     * 根据Id删除model
     *
     * @return
     */
    @RequestMapping(params = {"Action=DeleteApiModel"}, method = RequestMethod.GET)
    @Audit(eventName = "DeleteApiModel", description = "删除模型")
    public String deleteApiModelByModelId(@NotNull @RequestParam(value = "ModelId") long modelId) {

        logger.info("请求删除模型，modelId为：{}", modelId);

        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API_MODEL, modelId, null);


        ApiModel dbModel = apiModelService.getApiModelInfoByModelId(modelId);
        if (null == dbModel) {
            return apiReturn(CommonApiErrorCode.NoSuchModel);
        }

        //确认model是否被引用
        String message = apiModelService.getApiModelRefer(modelId);
        if (message != null) {
            return apiReturn(400, "ErrorOperation", message, null);
        }

        resource.setResourceName(dbModel.getModelName());
        apiModelService.deleteApiModelByModelId(modelId, true);
        return apiReturn(CommonErrorCode.SUCCESS);
    }
}
