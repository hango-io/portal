package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.dto.DubboMetaDto;
import org.hango.cloud.dashboard.apiserver.dto.RegistryCenterDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceType;
import org.hango.cloud.dashboard.apiserver.service.IDubboService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRegistryCenterService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.service.impl.DubboServiceImpl;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ActionInfoHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.audit.meta.AuditMetaData;
import org.hango.cloud.dashboard.audit.service.IAuditConfigService;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPublishServiceDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyServiceWithPortDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyStringMatchDto;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyHealthCheckService;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleHeaderOperationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleMapMatchDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceTrafficPolicyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;
import org.hango.cloud.dashboard.envoy.web.util.HttpCommonUtil;
import org.hango.cloud.dashboard.upgrade.UpgradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ApiPlane通信，相关方法，包括service、route等
 *
 * @author hanjiahao
 */
@Service
public class GetFromApiPlaneServiceImpl implements IGetFromApiPlaneService {


    private static final Logger logger = LoggerFactory.getLogger(EnvoyServiceProxyServiceImpl.class);

    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IEnvoyHealthCheckService envoyHealthCheckService;
    @Autowired
    private IRegistryCenterService registryCenterService;
    @Autowired
    private IAuditConfigService auditConfigService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IRouteRuleProxyService envoyRouteRuleProxyService;
    @Autowired
    private ApiServerConfig apiServerConfig;
    @Autowired
    private IDubboService dubboService;

    @Override
    public List<EnvoyServiceWithPortDto> getServiceListFromApiPlane(long gwId, String name, String registryCenterType, String registryCenterAlias, Map<String, String> serviceFilters) {
        GatewayInfo gatewayById = gatewayInfoService.get(gwId);
        if (gatewayById == null) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "GetServiceAndPortList");
        params.put("Version", "2019-07-25");
        if (StringUtils.isNotBlank(name)) {
            params.put("Name", name);
        }
        params.put("Type", registryCenterType);
        params.put("Registry", registryCenterAlias);
        params.putAll(serviceFilters);
        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayById.getApiPlaneAddr() + "/api", params, StringUtils.EMPTY, null, HttpMethod.GET.name());
        if (null == response) {
            return null;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane查询已发布服务列表，返回http status code非2xx, httpStatuCode:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return null;
        }
        JSONObject jsonResult = JSONObject.parseObject(response.getResponseBody());
        JSONArray services = jsonResult.getJSONArray("ServiceList");
        List<EnvoyServiceWithPortDto> serviceNameList = JSONObject.parseArray(services.toJSONString(), EnvoyServiceWithPortDto.class);
        if (CollectionUtils.isEmpty(serviceNameList) || RegistryCenterEnum.Kubernetes.getType().equals(registryCenterType)) {
            return serviceNameList;
        }
        for (EnvoyServiceWithPortDto envoyServiceWithPortDto : serviceNameList) {
            envoyServiceWithPortDto.setName(StringUtils.substring(envoyServiceWithPortDto.getName(), 0,
                    StringUtils.lastIndexOf(envoyServiceWithPortDto.getName(), ".")));
        }
        return serviceNameList;
    }

    /**
     * 重要：如果增加了某些字段，健康检查的publishHealthCheckRuleByApiPlane方法也需要修改，后续健康检查功能做到版本粒度，再进行优化
     *
     * @param serviceProxyDto 发布服务相关dto
     * @return
     */
    @Override
    public boolean publishServiceByApiPlane(ServiceProxyDto serviceProxyDto, EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo) {
        if (serviceProxyDto == null) {
            //更新健康检查规则流程: 传入的envoyServiceProxyDto为null
            ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(envoyHealthCheckRuleInfo.getGwId(), envoyHealthCheckRuleInfo.getServiceId());
            serviceProxyDto = ServiceProxyDto.toDto(serviceProxyInfo);
        }
        long gwId = serviceProxyDto.getGwId();
        long serviceId = serviceProxyDto.getServiceId();

        String code = new StringBuilder().append(serviceProxyDto.getPublishType()).append("-")
                .append(serviceProxyDto.getServiceId()).toString();

        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "PublishService");
        params.put("Version", "2019-07-25");

        EnvoyPublishServiceDto envoyPublishServiceDto = new EnvoyPublishServiceDto();
        envoyPublishServiceDto.setCode(code);
        if (gatewayInfo != null) {
            //网关集群名称RegistryCenterEnumRegistryCenterEnum
            envoyPublishServiceDto.setGateway(gatewayInfo.getGwClusterName());
        }
        boolean b = checkRegistryCenter(serviceProxyDto);
        if (!b) {
            return false;
        }
        String backendService;
        try {
            backendService = serviceProxyService.getBackendServiceSendToApiPlane(serviceProxyDto);
        } catch (RuntimeException e) {
            logger.error("[publish service] after deal with backendService, can not get service projectCode," +
                    "failed to publish service, serviceProxyDto: {}, e: {}", serviceProxyDto, e);
            return false;
        }

        envoyPublishServiceDto.setBackendService(backendService);
        envoyPublishServiceDto.setType(serviceProxyDto.getPublishType());
        envoyPublishServiceDto.setProtocol(serviceProxyDto.getPublishProtocol());

        //发布服务增加服务标识
        envoyPublishServiceDto.setServiceTag(serviceInfoService.getServiceByServiceId(serviceProxyDto.getServiceId()).getServiceName());
        envoyPublishServiceDto.setLoadBalancer(serviceProxyDto.getLoadBalancer());

        //增加版本信息
        envoyPublishServiceDto.setSubsets(serviceProxyService.setSubsetForDtoWhenSendToAPIPlane(serviceProxyDto, gatewayInfo.getGwClusterName()));

        //增加高级配置
        EnvoyServiceTrafficPolicyDto envoyServiceTrafficPolicyDto = serviceProxyDto.getTrafficPolicy() == null
                ? new EnvoyServiceTrafficPolicyDto() : serviceProxyDto.getTrafficPolicy();
        //增加健康检查
        envoyHealthCheckRuleInfo = envoyHealthCheckRuleInfo != null ? envoyHealthCheckRuleInfo :
                envoyHealthCheckService.getHealthCheckRuleInfo(serviceId, gwId);

        if (envoyHealthCheckRuleInfo != null) {
            //更新健康检查规则流程: 传入的envoyHealthCheckRuleInfo不为null
            envoyServiceTrafficPolicyDto = envoyHealthCheckService.setHealthCheck(envoyServiceTrafficPolicyDto, envoyHealthCheckRuleInfo);
            //设置subset的健康检查
            List<EnvoySubsetDto> envoySubsetDtos = serviceProxyService.setSubsetForDtoWhenSendToAPIPlane(serviceProxyDto, gatewayInfo.getGwClusterName());
            envoyPublishServiceDto.setSubsets(envoyHealthCheckService.setSubsetHealthCheck(envoySubsetDtos, envoyHealthCheckRuleInfo));

        }

        //增加连接池和负载均衡策略
        envoyPublishServiceDto.setTrafficPolicy(envoyServiceTrafficPolicyDto);

        try {
            //配置审计
            auditConfigService.record(new AuditMetaData(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                    ActionInfoHolder.getAction(), JSONObject.parseObject(JSON.toJSONString(envoyPublishServiceDto))));
            HttpClientResponse response = publishProxyToApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params, JSONObject.toJSONString(envoyPublishServiceDto), null, HttpMethod.POST.name());
            if (response == null) {
                return false;
            }
            if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
                logger.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return false;
            }
        } catch (Exception e) {
            logger.error("调用api-plane发布接口异常，e:{}", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean offlineServiceByApiPlane(String apiPlaneAddr, EnvoyPublishServiceDto envoyPublishServiceDto) {
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DeleteService");
        params.put("Version", "2019-07-25");
        //配置审计
        auditConfigService.record(new AuditMetaData(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                ActionInfoHolder.getAction(), JSONObject.parseObject(JSON.toJSONString(envoyPublishServiceDto))));
        HttpClientResponse response = publishProxyToApiPlane(apiPlaneAddr + "/api/portal", params, JSONObject.toJSONString(envoyPublishServiceDto), null, HttpMethod.POST.name());
        if (response == null) {
            return false;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane删除服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

    @Override
    public boolean publishRouteRuleByApiPlane(RouteRuleProxyInfo routeRuleProxyInfo, List<String> pluginConfigurations) {
        if (null == routeRuleProxyInfo) {
            logger.error("发布路由规则时指定的已发布路由规则信息为空!");
            return false;
        }
        GatewayInfo gatewayInfo = gatewayInfoService.get(routeRuleProxyInfo.getGwId());
        if (null == gatewayInfo) {
            logger.error("发布路由规则时指定的网关不存在! gwId:{}", routeRuleProxyInfo.getGwId());
            return false;
        }

        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(routeRuleProxyInfo.getRouteRuleId());
        if (null == routeRuleInfo) {
            logger.error("发布路由规则时指定的路由规则不存在! routeRuleId:{}", routeRuleProxyInfo.getRouteRuleId());
            return false;
        }

        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "PublishAPI");
        params.put("Version", "2019-07-25");

        Map<String, String> headers = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        headers.put("Content-type", Const.DEFAULT_CONTENT_TYPE);

        JSONObject body = generateBodyForPublishOrDelete(gatewayInfo, routeRuleInfo, routeRuleProxyInfo, pluginConfigurations, routeRuleProxyInfo.getHosts());
        try {
            //配置审计
            auditConfigService.record(new AuditMetaData(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                    ActionInfoHolder.getAction(), body));
            HttpClientResponse response = publishProxyToApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params, body.toJSONString(), headers, HttpMethod.POST.name());
            if (null == response) {
                return false;
            }

            if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
                logger.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return false;
            }
        } catch (Exception e) {
            logger.error("调用API-plane发布API接口出现异常,e{:}", e);
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteRouteRuleByApiPlane(RouteRuleProxyInfo routeRuleProxyInfo) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(routeRuleProxyInfo.getGwId());
        if (null == gatewayInfo) {
            logger.error("发布路由规则时指定的网关不存在! gwId:{}", routeRuleProxyInfo.getGwId());
            return false;
        }

        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(routeRuleProxyInfo.getRouteRuleId());
        if (null == routeRuleInfo) {
            logger.error("发布路由规则时指定的路由规则不存在! routeRuleId:{}", routeRuleProxyInfo.getRouteRuleId());
            return false;
        }

        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DeleteAPI");
        params.put("Version", "2019-07-25");

        Map<String, String> headers = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Content-type", Const.DEFAULT_CONTENT_TYPE);

        JSONObject body = generateBodyForPublishOrDelete(gatewayInfo, routeRuleInfo, routeRuleProxyInfo, Lists.newArrayList(), routeRuleProxyInfo.getHosts());
        //配置审计
        auditConfigService.record(new AuditMetaData(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                ActionInfoHolder.getAction(), body));
        HttpClientResponse response = publishProxyToApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params, body.toJSONString(), headers, HttpMethod.POST.name());
        if (null == response) {
            return false;
        }

        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane删除服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }

        return true;
    }

    public HttpClientResponse publishProxyToApiPlane(String apiPlaneUrl, Map<String, String> params, String body, Map<String, String> headers, String methodType) {
        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(apiPlaneUrl, params, body, headers, methodType);
        //存在bak api server且和apiPlaneUrl不相同
        if (StringUtils.isNotBlank(apiServerConfig.getBakApiPlaneAddr()) && !apiPlaneUrl.equals(apiServerConfig.getBakApiPlaneAddr()) && null != response && HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            // 升级双发场景下，用户配置了新插件但老版本api-plane没有，删除旧版本的下发
            String newBody = UpgradeUtil.dealMissingPluginInOldApiPlane(params, body);
            if (StringUtils.isNotEmpty(newBody)) {
                body = newBody;
            }
            response = HttpCommonUtil.getFromApiPlane(apiServerConfig.getBakApiPlaneAddr() + "/api/portal", params, body, headers, methodType);
        }
        return response;
    }

    public JSONObject generateBodyForPublishOrDelete(GatewayInfo gatewayInfo, RouteRuleInfo routeRuleInfo, RouteRuleProxyInfo routeRuleProxyInfo,

                                                     List<String> pluginConfigurations, String hosts) {
        RouteRuleProxyDto routeRuleProxyDto = envoyRouteRuleProxyService.fromMeta(routeRuleProxyInfo);
        JSONObject body = new JSONObject();
        body.put("Gateway", gatewayInfo.getGwClusterName());
        body.put("Code", String.valueOf(routeRuleInfo.getId()));
        body.put("Hosts", JSON.parseArray(hosts, String.class));

        body.put("RequestUris", routeRuleProxyDto.getUriMatchDto().getValue());
        body.put("UriMatch", routeRuleProxyDto.getUriMatchDto().getType());
        body.put("Plugins", pluginConfigurations);
        body.put("Order", routeRuleProxyInfo.getOrders());
        body.put("ProjectId", routeRuleInfo.getProjectId());
        EnvoyRouteRuleHeaderOperationDto headerOperation = JSON.parseObject(routeRuleInfo.getHeaderOperation(), EnvoyRouteRuleHeaderOperationDto.class);
        headerOperation = processDubboInfoForPublishOrDelete(routeRuleInfo, routeRuleProxyInfo, headerOperation);
        if (headerOperation != null) {
            body.put("RequestOperation", headerOperation.getRequestOperation());
        }

        //默认为*
        if (routeRuleProxyDto.getMethodMatchDto() == null) {
            body.put("Methods", new JSONArray().fluentAddAll(Lists.newArrayList("*")));
        } else {
            body.put("Methods", routeRuleProxyDto.getMethodMatchDto().getValue());
        }

        // EnvoyRouteRuleDto 中的host实际上是key为 :authority 的header match
        if (null != routeRuleProxyDto.getHostMatchDto()) {
            EnvoyRouteRuleMapMatchDto hostHeader = new EnvoyRouteRuleMapMatchDto();
            hostHeader.setKey(":authority");
            hostHeader.setType(routeRuleProxyDto.getHostMatchDto().getType());
            hostHeader.setValue(routeRuleProxyDto.getHostMatchDto().getValue());
            routeRuleProxyDto.getHeaders().add(hostHeader);
        }

        if (CollectionUtils.isNotEmpty(routeRuleProxyDto.getHeaders())) {
            body.put("Headers", EnvoyStringMatchDto.generateDtoFromRouteRuleDto(routeRuleProxyDto.getHeaders()));
        }

        if (CollectionUtils.isNotEmpty(routeRuleProxyDto.getQueryParams())) {
            body.put("QueryParams", EnvoyStringMatchDto.generateDtoFromRouteRuleDto(routeRuleProxyDto.getQueryParams()));
        }

        body.put("ServiceTag", serviceInfoService.getServiceByServiceId(routeRuleInfo.getServiceId()).getServiceName());
        body.put("RouteId", routeRuleInfo.getId());
        body.put("RouteName", routeRuleInfo.getRouteRuleName());
        if (routeRuleProxyDto.getTimeout() > 0) {
            body.put("Timeout", routeRuleProxyDto.getTimeout());
        }

        List<JSONObject> proxyServices = routeRuleProxyInfo.getDestinationServiceList().stream().map(destinationInfo -> {
            JSONObject proxyService = new JSONObject();
            ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gatewayInfo.getId(), destinationInfo.getServiceId());
            proxyService.put("Code", serviceProxyInfo.getCode());
            proxyService.put("Weight", destinationInfo.getWeight());
            proxyService.put("Port", destinationInfo.getPort());

            //静态地址发布和注册中心方式发布，其端口对应为80
            if (Const.STATIC_PUBLISH_TYPE.equals(serviceProxyInfo.getPublishType())) {
                proxyService.put("Port", 80);
            }
            proxyService.put("BackendService", serviceProxyService.getBackendServiceSendToApiPlane(ServiceProxyDto.toDto(serviceProxyInfo)));
            proxyService.put("Type", serviceProxyInfo.getPublishType());
            //增加版本信息
            if (StringUtils.isNotBlank(destinationInfo.getSubsetName())) {
                proxyService.put("Subset", (destinationInfo.getSubsetName() + "-" + serviceProxyInfo.getServiceId() + "-" + gatewayInfo.getGwClusterName()).toLowerCase());
            }
            return proxyService;
        }).collect(Collectors.toList());

        body.put("ProxyServices", new JSONArray().fluentAddAll(proxyServices));
        body.put("HttpRetry", JSONObject.parseObject(routeRuleProxyInfo.getHttpRetry()));
        if (routeRuleProxyInfo.getNeedRouteMetric()) {
            body.put("StatsMeta", apiServerConfig.getRouteMetricPathStats() ? Lists.newArrayList(routeRuleInfo.getUri())
                    : Lists.newArrayList(routeRuleProxyInfo.getRouteRuleId()));
        }

        //设置流量镜像
        if (routeRuleProxyDto.getMirrorTraffic() != null && routeRuleProxyDto.getMirrorSwitch() == 1) {
            JSONObject mirrorTraffic = new JSONObject();
            ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gatewayInfo.getId(),
                    routeRuleProxyDto.getMirrorTraffic().getServiceId());
            mirrorTraffic.put("BackendService", serviceProxyService.getBackendServiceSendToApiPlane(ServiceProxyDto.toDto(serviceProxyInfo)));
            mirrorTraffic.put("MirrorPercent", routeRuleProxyDto.getMirrorTraffic().getWeight());
            if (Const.STATIC_PUBLISH_TYPE.equals(serviceProxyInfo.getPublishType())) {
                mirrorTraffic.put("Port", 80);
            } else {
                mirrorTraffic.put("Port", routeRuleProxyDto.getMirrorTraffic().getPort());
            }
            if (StringUtils.isNotBlank(routeRuleProxyDto.getMirrorTraffic().getSubsetName())) {
                mirrorTraffic.put("Subset", routeRuleProxyDto.getMirrorTraffic().getSubsetName());
            }
            body.put("MirrorTraffic", mirrorTraffic);
        }

        return body;
    }

    /**
     * 处理Dubbo路由额外信息
     * <p>
     * 优先使用RouteRuleProxyInfo中的路由头信息
     * 该属性仅在单独处理Dubbo转换信息时，才被填充{@link DubboServiceImpl}
     *
     * @param routeRuleInfo
     * @param routeRuleProxyInfo
     * @param headerOperation
     * @return
     */
    private EnvoyRouteRuleHeaderOperationDto processDubboInfoForPublishOrDelete(RouteRuleInfo routeRuleInfo, RouteRuleProxyInfo routeRuleProxyInfo, EnvoyRouteRuleHeaderOperationDto headerOperation) {
        //
        //
        if (routeRuleProxyInfo.getHeaderOperation() != null) {
            return routeRuleProxyInfo.getHeaderOperation();
        }
        //dubbo 路由Head-To-Add组装
        ServiceInfo serviceInfo = serviceInfoService.getServiceById(String.valueOf(routeRuleInfo.getServiceId()));
        if (serviceInfo == null) {
            return headerOperation;
        }
        if (!ServiceType.dubbo.name().equals(serviceInfo.getServiceType())) {
            return headerOperation;
        }
        return dubboService.getDubboHeaderOperation(dubboService.getDubboDto(routeRuleProxyInfo.getId(), Const.ROUTE), headerOperation);
    }

    /**
     * 检测服务发布时，注册中心的合法性
     *
     * @param serviceProxyDto
     * @return
     */
    private boolean checkRegistryCenter(ServiceProxyDto serviceProxyDto) {
        if (!Const.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            return true;
        }
        RegistryCenterEnum registryCenterEnum = RegistryCenterEnum.get(serviceProxyDto.getRegistryCenterType());
        if (registryCenterEnum == null) {
            logger.warn("错误的注册中心类型 {}", serviceProxyDto.getRegistryCenterType());
            return false;
        }
        //Kubernetes注册中心前端不传注册中心地址
        if (RegistryCenterEnum.Kubernetes.equals(registryCenterEnum)) {
            return true;
        }
        return true;
    }


    @Override
    public List<DubboMetaDto> getDubboMetaListByApIPlane(long gwId, String igv, String applicationName, String method) {
        GatewayInfo gatewayById = gatewayInfoService.get(gwId);
        if (gatewayById == null) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "GetDubboMeta");
        params.put("Version", "2019-07-25");

        params.put("Igv", igv);
        params.put("ApplicationName", applicationName);
        params.put("Method", method);
        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayById.getApiPlaneAddr() + "/api", params, StringUtils.EMPTY, null, HttpMethod.GET.name());
        if (null == response) {
            return null;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane查询dubbo Meta元信息，返回http status code非2xx, httpStatuCode:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return null;
        }
        JSONObject jsonResult = JSONObject.parseObject(response.getResponseBody());
        JSONArray services = jsonResult.getJSONArray("Result");
        List<DubboMetaDto> dubboMetaList = JSONObject.parseArray(services.toJSONString(), DubboMetaDto.class);
        if (CollectionUtils.isEmpty(dubboMetaList)) {
            return dubboMetaList;
        }
        dubboMetaList.stream().forEach(meta -> meta.setGwId(gwId));
        return dubboMetaList;
    }
}
