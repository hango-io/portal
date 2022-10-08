package org.hango.cloud.dashboard.apiserver.web.controller.published;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.RegistryCenterDto;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCodeEnum;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRegistryCenterService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyServiceWithPortDto;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.ICopyServiceProxy;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务元数据发布至网关相关controller
 * 指服务元数据和网关服务进行关联，便于控制面展示
 *
 * @author hanjiahao
 */
@RestController
@Validated
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class ServiceProxyController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProxyController.class);

    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IRegistryCenterService registryCenterService;
    @Autowired
    private ICopyServiceProxy copyServiceProxy;
    @Autowired
    private IGetFromApiPlaneService getFromApiPlaneService;
    @Autowired
    private IServiceInfoService serviceInfoService;

    @RequestMapping(params = {"Action=DescribeRegistryTypes"}, method = RequestMethod.GET)
    public String describeRegistryTypes(@RequestParam(value = "ServiceType") String serviceType) {
        ErrorCode errorCode = serviceInfoService.checkServiceType(serviceType);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        List<String> registryTypes = registryCenterService.describeRegistryTypesByServiceType(serviceType);
        result.put("RegistryTypes", registryTypes);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribeServiceListByGw"}, method = RequestMethod.GET)
    public String describeServiceList(@RequestParam(value = "GwId") long gwId,
                                      @RequestParam(value = "Name", required = false) String name,
                                      @RequestParam(value = "RegistryCenterType") String registryCenterType,
                                      final HttpServletRequest request) {
        logger.info("查询网关id：{}下的所有发布服务name:{}", gwId, name);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        RegistryCenterDto registry = registryCenterService.findByType(registryCenterType);
        String registryAlias = registry == null ? StringUtils.EMPTY : registry.getRegistryAlias();
        Map<String, String> serviceFilters = serviceProxyService.createServiceFilters(registry);
        List<EnvoyServiceWithPortDto> serviceListFromApiPlane =
                getFromApiPlaneService.getServiceListFromApiPlane(gwId, name, registryCenterType, registryAlias, serviceFilters);

        if (CollectionUtils.isEmpty(serviceListFromApiPlane)) {
            return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
        }
        List<String> serviceNameList = serviceListFromApiPlane.stream()
                .map(e -> e.getName(registryCenterType, serviceFilters))
                .collect(Collectors.toList());
        result.put("ServiceList", serviceNameList);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "PublishService", description = "发布服务")
    @RequestMapping(params = {"Action=PublishService"}, method = RequestMethod.POST)
    public String publishService(@Validated @RequestBody ServiceProxyDto serviceProxyDto) {
        logger.info("发布服务至envoy网关，服务发布信息envoyServiceProxyDto:{}", serviceProxyDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_SERVICE, serviceProxyDto.getServiceId(), null);
        AuditResourceHolder.set(resource);
        ErrorCode errorCode = serviceProxyService.checkPublishParam(serviceProxyDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        long id = serviceProxyService.publishServiceToGw(serviceProxyDto);
        if (id == Const.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("Id", id);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdatePublishService", description = "更新服务发布信息")
    @RequestMapping(params = {"Action=UpdatePublishService"}, method = RequestMethod.POST)
    public String updatePublishService(@Validated @RequestBody ServiceProxyDto serviceProxyDto) {
        logger.info("更新服务发布信息envoyServiceProxyDto:{}", serviceProxyDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_SERVICE, serviceProxyDto.getServiceId(), null);
        AuditResourceHolder.set(resource);
        ErrorCode errorCode = serviceProxyService.checkUpdatePublishParam(serviceProxyDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        //更新时，如果发现已被路由规则引用的版本被删除则不允许删除
        errorCode = serviceProxyService.getRouteRuleNameWithServiceSubset(serviceProxyDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        long id = serviceProxyService.updateServiceToGw(serviceProxyDto);
        if (id == Const.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("Id", serviceProxyDto.getId());
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "DeleteServiceProxy", description = "下线服务")
    @RequestMapping(params = {"Action=DeleteServiceProxy"}, method = RequestMethod.GET)
    public String deleteServiceProxy(@Min(1) @RequestParam(value = "GwId") long gwId,
                                     @Min(1) @RequestParam(value = "ServiceId") long serviceId) {
        logger.info("下线已经关联的服务，gwId:{},serviceId:{}", new Object[]{gwId, serviceId});
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_SERVICE, serviceId, null);
        AuditResourceHolder.set(resource);

        ErrorCode errorCode = serviceProxyService.checkDeleteServiceProxy(gwId, serviceId);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        if (!serviceProxyService.deleteServiceProxy(gwId, serviceId)) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    /**
     *
     */
    @RequestMapping(params = {"Action=DescribeEnvoyServiceProxyList"}, method = RequestMethod.GET)
    public Object envoyServiceProxyList(@RequestParam(value = "GwId", required = false, defaultValue = "0") long gwId,
                                        @RequestParam(value = "ServiceId", required = false, defaultValue = "0") long serviceId,
                                        @RequestParam(value = "GwClusterName", required = false, defaultValue = "") String gwClusterName,
                                        @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                        @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                                        @RequestParam(value = "QuerySource", required = false) String querySource,
                                        @RequestParam(value = "Pattern", required = false) String pattern) {
        logger.info("分页查询envoy service proxy list,gatewayId:{}, pattern:{}", gwId, pattern);
        //offset,limit校验
        ErrorCode errorCode = CommonUtil.checkOffsetAndLimit(offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        if (StringUtils.isNotBlank(gwClusterName)) {
            GatewayInfo gatewayInfoInDb = gatewayInfoService.getGatewayInfoByGwClusterName(gwClusterName);
            if (gatewayInfoInDb == null || !gatewayInfoInDb.getGwType().equals(Const.ENVOY_GATEWAY_TYPE)) {
                logger.info("查询envoy网关已发布服务存在脏数据，出现异常。网关不存在或网关非envoy类型");
                return apiReturn(CommonErrorCode.NoSuchGateway);
            }
            gwId = gatewayInfoInDb.getId();
        }
        long projectId = ProjectTraceHolder.getProId();
        //兼容serviceId逻辑，当pattern为空时，按照210430逻辑进行
        List<ServiceProxyInfo> serviceProxy =
                StringUtils.isBlank(pattern) ? serviceProxyService
                        .getEnvoyServiceProxy(gwId, serviceId, projectId, offset, limit)
                        : serviceProxyService.getEnvoyServiceProxy(gwId, pattern, projectId, offset, limit);
        List<ServiceProxyDto> serviceProxyDtos = new ArrayList<>();
        if (StringUtils.isNotBlank(querySource)) {
            for (ServiceProxyInfo serviceProxyInfo : serviceProxy) {
                ServiceProxyDto serviceProxyDto = serviceProxyService
                        .fromMetaWithStatus(serviceProxyInfo, querySource);
                if (serviceProxyDto != null) {
                    serviceProxyDtos.add(serviceProxyDto);
                }
            }
        } else {
            for (ServiceProxyInfo serviceProxyInfo : serviceProxy) {
                ServiceProxyDto serviceProxyDto = serviceProxyService
                        .fromMetaWithPort(serviceProxyInfo);
                if (serviceProxyDto != null) {
                    serviceProxyDtos.add(serviceProxyDto);
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        if (!StringUtils.equals("NSF", querySource)) {
            //兼容serviceId逻辑，当pattern为空时，兼容210430逻辑
            long count = StringUtils.isBlank(pattern) ?
                    serviceProxyService.getServiceProxyCount(gwId, serviceId)
                    : serviceProxyService.getServiceProxyCount(gwId, pattern, projectId);
            result.put(TOTAL_COUNT, count);
        }
        result.put("ServiceProxyList", serviceProxyDtos);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribeServiceProxyList"}, method = RequestMethod.GET)
    public Object serviceProxyList(@RequestParam(value = "GwId", required = false, defaultValue = "0") long gwId,
                                   @RequestParam(value = "ServiceId", required = false, defaultValue = "0") long serviceId,
                                   @RequestParam(value = "GwClusterName", required = false, defaultValue = "") String gwClusterName,
                                   @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                   @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                                   @RequestParam(value = "QuerySource", required = false) String querySource,
                                   @RequestParam(value = "Pattern", required = false) String pattern) {
        logger.info("分页查询envoy service proxy list,gatewayId:{}, pattern:{}", gwId, pattern);
        //offset,limit校验
        ErrorCode errorCode = CommonUtil.checkOffsetAndLimit(offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        if (StringUtils.isNotBlank(gwClusterName)) {
            GatewayInfo gatewayInfoInDb = gatewayInfoService.getGatewayInfoByGwClusterName(gwClusterName);
            if (gatewayInfoInDb == null || !gatewayInfoInDb.getGwType().equals(Const.ENVOY_GATEWAY_TYPE)) {
                logger.info("查询envoy网关已发布服务存在脏数据，出现异常。网关不存在或网关非envoy类型");
                return apiReturn(CommonErrorCode.NoSuchGateway);
            }
            gwId = gatewayInfoInDb.getId();
        }
        long projectId = ProjectTraceHolder.getProId();
        //兼容serviceId逻辑，当pattern为空时，按照210430逻辑进行
        List<ServiceProxyInfo> serviceProxy =
                StringUtils.isBlank(pattern) ? serviceProxyService
                        .getEnvoyServiceProxy(gwId, serviceId, projectId, offset, limit)
                        : serviceProxyService.getEnvoyServiceProxy(gwId, pattern, projectId, offset, limit);
        List<ServiceProxyDto> serviceProxyDtos = new ArrayList<>();
        if (StringUtils.isNotBlank(querySource)) {
            for (ServiceProxyInfo serviceProxyInfo : serviceProxy) {
                ServiceProxyDto serviceProxyDto = serviceProxyService
                        .fromMetaWithStatus(serviceProxyInfo, querySource);
                if (serviceProxyDto != null) {
                    serviceProxyDtos.add(serviceProxyDto);
                }
            }
        } else {
            for (ServiceProxyInfo serviceProxyInfo : serviceProxy) {
                ServiceProxyDto serviceProxyDto = serviceProxyService
                        .fromMetaWithPort(serviceProxyInfo);
                if (serviceProxyDto != null) {
                    serviceProxyDtos.add(serviceProxyDto);
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        if (!StringUtils.equals("NSF", querySource)) {
            //兼容serviceId逻辑，当pattern为空时，兼容210430逻辑
            long count = StringUtils.isBlank(pattern) ?
                    serviceProxyService.getServiceProxyCount(gwId, serviceId)
                    : serviceProxyService.getServiceProxyCount(gwId, pattern, projectId);
            result.put(TOTAL_COUNT, count);
        }
        result.put("ServiceProxyList", serviceProxyDtos);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribeServiceProxy"}, method = RequestMethod.GET)
    public Object describeServiceProxy(@Min(1) @RequestParam(value = "ServiceId") long serviceId,
                                       @Min(1) @RequestParam(value = "GwId") long gwId) {
        logger.info("根据服务id：{},网关id：{}，查询服务发布信息", serviceId, gwId);
        ServiceProxyInfo serviceProxyInDb = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, serviceId);
        Map<String, Object> result = new HashMap<>();
        if (serviceProxyInDb != null) {
            result.put("ServiceProxy", serviceProxyService.fromMetaWithStatus(serviceProxyInDb, null));
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=CopyServiceProxy"}, method = RequestMethod.GET)
    public Object copyServiceProxy(@RequestParam(value = "ServiceId", required = false) String serviceId,
                                   @Min(1) @RequestParam(value = "OriginGwId") long originGwId,
                                   @Min(1) @RequestParam(value = "DesGwId") long desGwId) {
        logger.info("一键复制服务，服务id，serviceId:{},originGwId:{},desGwId:{}", new Object[]{serviceId, originGwId, desGwId});
        Map<Long, List<Long>> failedRouteIdList = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        List<Long> serviceIds = new ArrayList<>();
        if (StringUtils.isBlank(serviceId)) {
            List<ServiceProxyInfo> serviceProxyList = serviceProxyService.getServiceProxyListByGwId(originGwId);
            if (!CollectionUtils.isEmpty(serviceProxyList)) {
                serviceIds = serviceProxyList.stream().map(ServiceProxyInfo::getServiceId).collect(Collectors.toList());
            }
        } else {
            serviceIds = Arrays.asList(serviceId.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        }
        ErrorCode checkResult = copyServiceProxy.checkCopyServiceProxy(serviceIds, originGwId, desGwId);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        failedRouteIdList = copyServiceProxy.copyServiceProxy(serviceIds, originGwId, desGwId);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("FailedRouteIdList", JSONObject.toJSONString(failedRouteIdList));
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    /**
     * 根据Service id查询服务发布详情
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
