package org.hango.cloud.dashboard.apiserver.web.controller.dubbo;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.DubboInfoDto;
import org.hango.cloud.dashboard.apiserver.dto.DubboMetaDto;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IDubboMetaService;
import org.hango.cloud.dashboard.apiserver.service.IDubboService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.gdashboard.api.dto.DubboParamInfoDto;
import org.hango.cloud.gdashboard.api.meta.*;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.service.IDubboParamService;
import org.hango.cloud.gdashboard.api.service.IOperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/16
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX)
public class DubboController extends AbstractController {

    public static final Logger logger = LoggerFactory.getLogger(DubboController.class);

    @Autowired
    private IApiInfoService apiInfoService;

    @Autowired
    private IDubboParamService dubboParamService;

    @Autowired
    private IOperationLogService operationLogService;

    @Autowired
    private IApiParamTypeService apiParamTypeService;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IDubboService dubboService;

    @Autowired
    private IDubboMetaService dubboMetaService;


    /**
     * 添加dubboParam，dubboParam和body中的参数都存放在表nce_gateway_body_param中
     *
     * @param apiId
     * @param paramInfoDtoList
     * @return
     */
    @MethodReentrantLock
    @PostMapping(params = {"Action=CreateDubboParam", "Version=2018-08-09"})
    @Audit(eventName = "CreateDubboParam", description = "编辑API dubboParam参数")
    public String addDubboParam(@RequestParam("ApiId") long apiId, @RequestBody List<DubboParamInfoDto> paramInfoDtoList) {
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        int resourceIndex = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (DubboParamInfoDto dubboParamInfo : paramInfoDtoList) {
            //记录审计数据
            resourceDataDtoList.add(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API, StringUtils.EMPTY, dubboParamInfo.getParamName()));
            resourceIndex++;
            if (DubboType.DubboParam.name().equals(dubboParamInfo.getDubboType())) {
                stringBuilder.append(resourceIndex + ". 名称：参数, 参数名称：" + dubboParamInfo.getParamName()
                        + ", 类型Bean：" + dubboParamInfo.getParamAlias() + ", 描述：" + dubboParamInfo.getDescription() + ". ");
            } else if (DubboType.DubboResponse.name().equals(dubboParamInfo.getDubboType())) {
                stringBuilder.append(resourceIndex + ". 名称：" + dubboParamInfo.getParamName() + ", 取值：" + dubboParamInfo.getDefValue() + ", 描述：" + dubboParamInfo.getDescription() + ". ");
            } else if (DubboType.DubboInterface.name().equals(dubboParamInfo.getDubboType())) {
                stringBuilder.append(resourceIndex + ". 名称：Interface 名称, 取值：" + dubboParamInfo.getParamName() + ". ");
            } else if (DubboType.DubboMethod.name().equals(dubboParamInfo.getDubboType())) {
                stringBuilder.append(resourceIndex + ". 名称：方法名, 取值：" + dubboParamInfo.getParamName() + ". ");
            } else if (DubboType.DubboGroup.name().equals(dubboParamInfo.getDubboType())) {
                stringBuilder.append(resourceIndex + ". 名称：分组, 取值：" + dubboParamInfo.getParamName() + ". ");
            } else if (DubboType.DubboVersion.name().equals(dubboParamInfo.getDubboType())) {
                stringBuilder.append(resourceIndex + ". 名称：接口版本号, 取值：" + dubboParamInfo.getParamName() + ". ");
            }
        }
        //操作审计记录资源名称
        AuditResourceHolder.set(resourceDataDtoList);
        List<DubboParamInfo> paramInfoList = BeanUtil.copyList(paramInfoDtoList, DubboParamInfo.class);
        logger.info("请求创建dubbo param，apiId为{}，操作人：{}", apiId, UserPermissionHolder.getAccountId());
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(apiInfo.getServiceId());
        if (serviceInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        ApiErrorCode errorCode = dubboParamService.checkAndCompleteParam(paramInfoList, apiId, ServiceType.valueOf(serviceInfo.getServiceType()));
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            logger.info("出现错误，errMsg = {}", errorCode.getMessage());
            return apiReturn(errorCode);
        }
        dubboParamService.batchAdd(paramInfoList);

        //操作日志记录
        OperationLog operationLog = operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiId, Const.API, "修改了该API的dubboParam，修改后dubboParam中的参数信息为：" + stringBuilder.toString());
        operationLogService.addApiOperationLog(operationLog);

        return apiReturnSuccess(BeanUtil.copyList(paramInfoList, DubboParamInfoDto.class));
    }


    /**
     * 查询dubboparam
     *
     * @param apiId
     * @return
     */
    @GetMapping(params = {"Action=DescribeDubboParam", "Version=2018-08-09"})
    public String getDubboparam(@RequestParam("ApiId") long apiId) {
        logger.info("请求查询request body，apiId为{}，操作人：{}", apiId, UserPermissionHolder.getAccountId());
        if (apiInfoService.getApiById(apiId) == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        List<DubboParamInfo> dubboParamInfoList = dubboParamService.getDubboParamByApiId(apiId);
        for (DubboParamInfo dubboParamInfo : dubboParamInfoList) {
            if (apiParamTypeService.listApiParamType(dubboParamInfo.getParamTypeId()) == null) {
                logger.warn("api {} request body参数存在脏数据，请注意排查: {}", apiId, dubboParamInfo.getParamTypeId());
                dubboParamInfo.setParamType("String");
                continue;
            }
            dubboParamInfo.setParamType(apiParamTypeService.listApiParamType(dubboParamInfo.getParamTypeId()).getParamType());

            if (dubboParamInfo.getArrayDataTypeId() != 0) {
                ApiParamType apiParamType = apiParamTypeService.listApiParamType(dubboParamInfo.getArrayDataTypeId());
                if (apiParamType != null) {
                    dubboParamInfo.setArrayDataTypeName(apiParamType.getParamType());
                }
            }
        }
        return apiReturnSuccess(BeanUtil.copyList(dubboParamInfoList, DubboParamInfoDto.class));
    }


    /**
     * 发布Dubbo路由功能
     *
     * @param dubboInfoDto
     * @return
     */
    @MethodReentrantLock
    @PostMapping(path = Const.ENVOY_GATEWAY_TYPE, params = {"Action=PublishDubbo", "Version=2020-10-29"})
    @Audit(eventName = "PublishEnvoyDubbo", description = "路由Dubbo转换发布")
    public String publishEnvoyDubbo(@RequestBody @Validated DubboInfoDto dubboInfoDto) {
        logger.info("进行路由Dubbo转换发布操作，关联类型：{} ,关联ID：{} , 操作人：{}", dubboInfoDto.getObjectType(), dubboInfoDto.getObjectId(), UserPermissionHolder.getAccountId());
        //操作审计记录资源名称
        AuditResourceHolder.set(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, StringUtils.EMPTY, dubboInfoDto.getObjectType() + dubboInfoDto.getObjectId()));
        ErrorCode errorCode = dubboService.checkAndComplete(dubboInfoDto);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            logger.info("出现错误，errMsg = {}", errorCode.getMessage());
            return apiReturn(errorCode);
        }
        long id = dubboService.saveDubboInfo(dubboInfoDto);
        if (Const.ERROR_RESULT == id) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturnSuccess(id);
    }

    /**
     * 下线Dubbo路由功能
     *
     * @param objectId
     * @param objectType
     * @return
     */
    @MethodReentrantLock
    @GetMapping(path = Const.ENVOY_GATEWAY_TYPE, params = {"Action=OfflineDubbo", "Version=2020-10-29"})
    @Audit(eventName = "OfflineEnvoyDubbo", description = "路由Dubbo转换下线")
    public String offlineEnvoyDubbo(@RequestParam(value = "ObjectId") long objectId,
                                    @RequestParam(value = "ObjectType", required = false, defaultValue = Const.ROUTE) String objectType) {
        logger.info("进行路由Dubbo转换下线操作，，关联类型：{} ,关联ID：{} , 操作人：{}", objectType, objectId, UserPermissionHolder.getAccountId());
        //操作审计记录资源名称
        AuditResourceHolder.set(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, StringUtils.EMPTY, objectType + "-" + objectId));
        if (dubboService.deleteDubboInfo(objectId, objectType)) {
            return apiReturnSuccess(null);
        }
        return apiReturn(CommonErrorCode.InternalServerError);
    }

    /**
     * 查看Dubbo路由功能
     *
     * @param objectId
     * @param objectType
     * @return
     */
    @MethodReentrantLock
    @GetMapping(path = Const.ENVOY_GATEWAY_TYPE, params = {"Action=DescribePublishedDubbo", "Version=2020-10-29"})
    public String describeEnvoyDubbo(@RequestParam(value = "ObjectId") long objectId,
                                     @RequestParam(value = "ObjectType", required = false, defaultValue = Const.ROUTE) String objectType) {
        logger.info("进行路由Dubbo转换查询操作，，关联类型：{} ,关联ID：{} , 操作人：{}", objectType, objectId, UserPermissionHolder.getAccountId());
        DubboInfoDto dubboDto = dubboService.getDubboDto(objectId, objectType);
        dubboService.processMethodWorks(dubboDto);
        return apiReturnSuccess(dubboDto);
    }

    /**
     * 获取Dubbo Meta信息
     *
     * @param gwId
     * @param igv
     * @param method
     * @return
     */
    @MethodReentrantLock
    @GetMapping(path = Const.ENVOY_GATEWAY_TYPE, params = {"Action=DescribeDubboMeta", "Version=2021-10-30"})
    public String describeDubboMeta(@RequestParam(value = "GwId") long gwId,
                                    @RequestParam(value = "Igv") String igv,
                                    @RequestParam(value = "method", required = false) String method
    ) {
        logger.info("进行路由Dubbo 元信息查询操作，，网关ID：{} ,Igv信息：{} , 方法：{}", gwId, igv, method);
        List<DubboMetaDto> dubboDto = dubboMetaService.findByIgv(gwId, igv);
        if (CollectionUtils.isEmpty(dubboDto)) {
            dubboDto = dubboMetaService.refreshDubboMeta(gwId, igv);
        }
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, dubboDto);
        result.put(TOTAL_COUNT, dubboDto.size());
        return apiReturnSuccess(result);
    }


    /**
     * 刷新Dubbo Meta信息
     *
     * @param gwId
     * @param igv
     * @param method
     * @return
     */
    @MethodReentrantLock
    @GetMapping(path = Const.ENVOY_GATEWAY_TYPE, params = {"Action=RefreshDubboMeta", "Version=2021-10-30"})
    public String refreshDubboMeta(@RequestParam(value = "GwId") long gwId,
                                   @RequestParam(value = "Igv") String igv,
                                   @RequestParam(value = "method", required = false) String method) {
        logger.info("进行路由Dubbo 元信息查询操作，，网关ID：{} ,Igv信息：{} , 方法：{}", gwId, igv, method);
        List<DubboMetaDto> dubboDto = dubboMetaService.refreshDubboMeta(gwId, igv);
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, dubboDto);
        result.put(TOTAL_COUNT, dubboDto.size());
        return apiReturnSuccess(result);
    }

}
