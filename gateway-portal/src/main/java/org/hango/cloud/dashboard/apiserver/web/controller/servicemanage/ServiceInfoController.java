package org.hango.cloud.dashboard.apiserver.web.controller.servicemanage;

import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.ServiceInfoDto;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Modified hanjiahao
 * 服务基本管理，包括服务创建，查询，修改
 * 服务元数据管理（g0网关和envoy网关不区分服务）
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class ServiceInfoController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(ServiceInfoController.class);

    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;


    /**
     * 添加Service
     *
     * @throws URISyntaxException
     */
    @MethodReentrantLock
    @Audit(eventName = "CreateService", description = "创建服务")
    @RequestMapping(params = {"Action=CreateService"}, method = RequestMethod.POST)
    public Object addService(@Validated @RequestBody ServiceInfoDto serviceInfoDto) {
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_SERVICE, serviceInfoDto.getId(), serviceInfoDto.getDisplayName());
        AuditResourceHolder.set(resource);

        logger.info("创建服务，serviceInfo:{}", serviceInfoDto);
        ErrorCode errorCode = serviceInfoService.checkCreateServiceParam(serviceInfoDto);
        //参数校验
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        } else {
            ServiceInfo serviceInfo = serviceInfoService.addServiceInfo(serviceInfoDto, ProjectTraceHolder.getProId());
            resource.setResourceId(serviceInfo.getId());
            return apiReturnSuccess(serviceInfo.getId());
        }
    }

    /**
     * 根据Id查询服务
     *
     * @param serviceId
     * @throws URISyntaxException
     */
    @RequestMapping(params = {"Action=DescribeServiceById"}, method = RequestMethod.GET)
    public Object getService(@RequestParam(value = "ServiceId") long serviceId) {
        logger.info("查询serviceId:{}服务", serviceId);
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfo == null) {
            logger.info("不存在当前serviceId的服务");
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("ServiceInfoBasic", ServiceInfoDto.fromMeta(serviceInfo));
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 修改Service
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=UpdateService"}, method = RequestMethod.POST)
    @Audit(eventName = "UpdateService", description = "编辑服务基本信息")
    public Object updateService(@Validated @RequestBody ServiceInfoDto serviceInfoDto) {
        //操作审计记录资源名称
        AuditResourceHolder.set(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_SERVICE, serviceInfoDto.getId(), serviceInfoDto.getServiceName()));

        logger.info("更新服务基本信息，serviceInfoFrontDto:{}", serviceInfoDto);
        ErrorCode errorCode = serviceInfoService.checkUpdateServiceParam(serviceInfoDto);
        //参数校验
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        } else {
            ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(serviceInfoDto.getId());
            serviceInfo.setDisplayName(serviceInfoDto.getDisplayName());
            serviceInfo.setContacts(serviceInfoDto.getContacts());
            serviceInfo.setDescription(serviceInfoDto.getDescription());
            serviceInfo.setHealthInterfacePath(serviceInfoDto.getHealthInterfacePath());
            serviceInfo.setServiceName(serviceInfoDto.getServiceName());
            serviceInfoService.updateService(serviceInfo);
            return apiReturn(CommonErrorCode.Success);
        }
    }


    /**
     * 查询Service列表，创建API时
     */
    @RequestMapping(params = {"Action=DescribeServiceForApi"}, method = RequestMethod.GET)
    public Object serviceListForCreateApiOrModel() {
        logger.info("创建API，请求查询service列表");
        List<ServiceInfo> serviceInfos = serviceInfoService.findAllServiceByProjectId(ProjectTraceHolder.getProId());
        List<ServiceInfoDto> serviceInfoDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(serviceInfos)) {
            serviceInfoDtos = BeanUtil.copyList(serviceInfos, ServiceInfoDto.class);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("ServiceInfoList", serviceInfoDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }


    /**
     * 查询服务列表，返回前端当前项目下的所有服务信息
     *
     * @throws URISyntaxException
     */
    @RequestMapping(params = {"Action=DescribeServiceList"}, method = RequestMethod.GET)
    public Object serviceList(@RequestParam(value = "Pattern", required = false) String pattern,
                              @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                              @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit) {
        logger.info("获取当前项目下的service列表，projectId：{}", ProjectTraceHolder.getProId());
        //offset,limit校验
        ErrorCode errorCode = CommonUtil.checkOffsetAndLimit(offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        List<ServiceInfo> serviceInfos = serviceInfoService.findAllServiceByProjectIdLimit(pattern, offset, limit, ProjectTraceHolder.getProId());
        //TODO 发布数量是否需要再计算，g0和envoy的发布数量计算方式不同
        List<ServiceInfoDto> serviceInfoDtos = serviceInfos.stream().map(ServiceInfoDto::fromMeta).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("ServiceCount", serviceInfoService.getServiceCountByProjectId(pattern, ProjectTraceHolder.getProId()));
        result.put("ServiceInfoList", serviceInfoDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }


    /**
     * 根据服务ID删除服务
     *
     * @param serviceId，服务id
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteService"}, method = RequestMethod.GET)
    @Audit(eventName = "DeleteService", description = "删除服务")
    public Object deleteService(@RequestParam(value = "ServiceId") long serviceId) {
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_SERVICE, serviceId, null);
        AuditResourceHolder.set(resource);

        logger.info("删除serviceId：{}下的服务", serviceId);
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfo != null) {
            resource.setResourceName(serviceInfo.getServiceName());
            if (serviceInfo.getStatus() == 1) {
                logger.info("服务已发布，不允许删除");
                return apiReturn(CommonErrorCode.CannotDeleteOnlineService);
            }
        }
        if (apiInfoService.getApiCountByServiceId(serviceId) > 0) {
            logger.info("服务下存在API，不允许删除服务");
            return apiReturn(CommonErrorCode.CannotDeleteApiService);
        }
        if (routeRuleInfoService.getRouteRuleInfoCount("", -1, serviceId, 0) > 0) {
            logger.info("服务下存在路由，不允许删除服务");
            return apiReturn(CommonErrorCode.CannotDeleteRouteRuleService);
        }
        serviceInfoService.delete(serviceId);
        return apiReturn(CommonErrorCode.Success);
    }

    /**
     * 根据Service id查询服务发布详情， 为了兼容之前的接口，因此移到此处
     */
    @RequestMapping(params = {"Action=DescribeServiceRoute"}, method = RequestMethod.GET)
    public Object getPublishedServiceInfoById(@RequestParam(value = "ServiceId") @NotNull long serviceId) {
        logger.info("查询服务的具体发布详情，serviceId:{}", serviceId);
        if (!serviceInfoService.isServiceExists(serviceId)) {
            logger.info("查询服务的具体发布详情，服务不存在");
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("PublishDetails", serviceProxyService.getPublishedDetailByService(serviceId));
        return apiReturn(CommonErrorCode.Success, result);
    }
}
