package org.hango.cloud.envoy.infra.serviceproxy.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.hango.cloud.common.infra.base.dto.ResourceDTO;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.healthcheck.dto.ActiveHealthCheckRuleDto;
import org.hango.cloud.common.infra.healthcheck.dto.HealthCheckRuleDto;
import org.hango.cloud.common.infra.healthcheck.dto.PassiveHealthCheckRuleDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.convert.ServiceProxyConvert;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceTrafficPolicyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SubsetDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceType;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.serviceregistry.meta.RegistryCenterEnum;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.config.EnvoyConfig;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.meta.EnvoyErrorCode;
import org.hango.cloud.envoy.infra.base.service.VersionManagerService;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobufProxy;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufService;
import org.hango.cloud.envoy.infra.healthcheck.service.IEnvoyHealthCheckService;
import org.hango.cloud.envoy.infra.serviceproxy.dto.DpServiceProxyDto;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceProxyService;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceRefreshService;
import org.hango.cloud.envoy.infra.webservice.service.IEnvoyWebServiceService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.PLANE_PORTAL_PATH;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/7
 */
@Service
public class EnvoyServiceProxyServiceImpl implements IEnvoyServiceProxyService {
    private static Logger logger = LoggerFactory.getLogger(EnvoyServiceProxyServiceImpl.class);
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private EnvoyConfig envoyConfig;


    @Autowired
    private IEnvoyHealthCheckService envoyHealthCheckService;

    @Autowired
    private IEnvoyServiceProxyService envoyServiceProxyService;

    @Autowired
    private VersionManagerService versionManagerService;

    @Autowired
    private IEnvoyGrpcProtobufService envoyGrpcProtobufService;

    @Autowired
    private IEnvoyWebServiceService envoyWebServiceService;

    @Autowired
    private IEnvoyServiceRefreshService envoyServiceRefreshService;

    @Autowired
    private IRouteService routeService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishToGateway(ServiceProxyDto serviceProxyDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyDto.getVirtualGwId());
        if (virtualGatewayDto == null) {
            logger.warn("网关信息为空");
            return false;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "PublishService");
        params.put("Version", "2019-07-25");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // TODO service 多集群适配
//        ResourceDTO resourceDTO = versionManagerService.getResourceDTO(serviceProxyDto.getVirtualGwId(), serviceProxyDto.getId(), ResourceEnum.Service);
        ResourceDTO resourceDTO = null;
        try {
            return versionManagerService.publishServiceWithVersionManager(virtualGatewayDto.getConfAddr() + PLANE_PORTAL_PATH, params, headers, toView(serviceProxyDto, virtualGatewayDto), resourceDTO);
        } catch (Exception e) {
            logger.error("调用api-plane发布接口异常", e);
            return false;
        }
    }



    public DpServiceProxyDto toView(ServiceProxyDto serviceProxyDto,VirtualGatewayDto virtualGatewayDto) {
        if (serviceProxyDto == null) {
            return null;
        }
        DpServiceProxyDto dpServiceProxyDto = new DpServiceProxyDto();
        dpServiceProxyDto.setCode(ServiceProxyConvert.getCode(serviceProxyDto));
        dpServiceProxyDto.setGateway(ServiceProxyConvert.getGateway(virtualGatewayDto.getGwClusterName() , virtualGatewayDto.getCode()));
        //check 原代码: getBackendServiceSendToApiPlane(serviceProxyDto)
        dpServiceProxyDto.setBackendService(getBackendServiceSendToApiPlane(serviceProxyDto));
        dpServiceProxyDto.setType(serviceProxyDto.getPublishType());
        //数据面仅感知gprc 和 http
        dpServiceProxyDto.setProtocol(ServiceType.grpc.name().equals(serviceProxyDto.getProtocol()) ? serviceProxyDto.getProtocol() : ServiceType.http.name());
        dpServiceProxyDto.setServiceTag(serviceProxyDto.getName());
        dpServiceProxyDto.setLoadBalancer(serviceProxyDto.getLoadBalancer());
        //check 原代码: dpServiceProxyDto.setSubsets(serviceProxyService.setSubsetForDtoWhenSendToDataPlane(serviceProxyDto, serviceProxyDto.getGwClusterName()));
        dpServiceProxyDto.setSubsets(ServiceProxyConvert.buildSubset(serviceProxyDto));
        HealthCheckRuleDto healthCheckRule = serviceProxyDto.getHealthCheckRule();
        ServiceTrafficPolicyDto trafficPolicy = serviceProxyDto.getTrafficPolicy();
        if (healthCheckRule != null){
            if (healthCheckRule.getPassiveSwitch() == 1){
                trafficPolicy.setPassiveHealthCheckRule( new PassiveHealthCheckRuleDto(healthCheckRule));
            }
            if (healthCheckRule.getActiveSwitch() == 1){
                trafficPolicy.setActiveHealthCheckRule(new ActiveHealthCheckRuleDto(healthCheckRule));
            }
        }

        dpServiceProxyDto.setTrafficPolicy(trafficPolicy);

        processServiceMetadata(virtualGatewayDto, serviceProxyDto, dpServiceProxyDto);
        return dpServiceProxyDto;
    }

    /**
     * 添加路由 metadata 数据
     */
    private void processServiceMetadata(VirtualGatewayDto virtualGatewayDto,
                                                     ServiceProxyDto serviceDto,DpServiceProxyDto dpServiceProxyDto) {
        Map<String, Map<String,String>> metaMap = dpServiceProxyDto.getMetaMap() == null ? Maps.newHashMap() : dpServiceProxyDto.getMetaMap();
        //处理服务指标Meta数据
        processServiceStatsMeta(virtualGatewayDto, serviceDto, metaMap);
        dpServiceProxyDto.setMetaMap(metaMap);
    }

    /**
     * 添加服务指标Meta数据
     */
    private void processServiceStatsMeta(VirtualGatewayDto virtualGatewayDto,ServiceProxyDto serviceProxy, Map<String, Map<String,String>>metaMap) {
        Map<String, String> stats = Maps.newHashMap();
        stats.put("service_name", serviceProxy.getName());
        stats.put("virtual_gateway_code", virtualGatewayDto.getCode());
        //此处用服务标识，一方面与服务告警模板保持一致，另一方面，服务标识不存在修改的情况。
        stats.put("project_id", String.valueOf(serviceProxy.getProjectId()));
        metaMap.put("StatsMeta",stats);
    }

    public HttpClientResponse proxyToApiPlane(String apiPlaneUrl, Map<String, Object> params, String body, HttpHeaders headers) {
        HttpClientResponse response = HttpClientUtil.postRequest(apiPlaneUrl, body, params, headers, EnvoyConst.MODULE_API_PLANE);
        //存在bak api server且和apiPlaneUrl不相同
        //todo  可以使用hooker将双发与正常业务隔离
        if (StringUtils.isNotBlank(envoyConfig.getBakApiPlaneAddr()) && !apiPlaneUrl.equals(envoyConfig.getBakApiPlaneAddr()) && null != response && HttpClientUtil.isNormalCode(response.getStatusCode())) {
            // 升级双发场景下，用户配置了新插件但老版本api-plane没有，删除旧版本的下发
//            String newBody = UpgradeUtil.dealMissingPluginInOldApiPlane(params, body);
//            if (StringUtils.isNotEmpty(newBody)) {
//                body = newBody;
//            }
//            response = HttpCommonUtil.getFromApiPlane(apiServerConfig.getBakApiPlaneAddr() + "/api/portal", params, body, headers, methodType);
        }
        return response;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateToGateway(ServiceProxyDto serviceProxyDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyDto.getVirtualGwId());
        //判断版本有没有删除的，如果有删除的，需要先调用APIPlane删除接口，然后进行新建
        ServiceProxyDto serviceProxyInfoInDB = serviceProxyService.get(serviceProxyDto.getId());
        List<SubsetDto> subsets = serviceProxyDto.getSubsets();
        List<SubsetDto> subsetsInDB = serviceProxyInfoInDB.getSubsets();
        if (!CollectionUtils.isEmpty(subsetsInDB)) {
            List<String> envoySubsetListInDB = subsetsInDB.stream().map(SubsetDto::getName).collect(Collectors.toList());
            List<String> subsetNameList = new ArrayList<>();
            if (serviceProxyDto.getSubsets() != null) {
                List<String> envoySubsetList = subsets.stream().map(SubsetDto::getName).collect(Collectors.toList());
                subsetNameList = envoySubsetListInDB.stream().filter(s -> !envoySubsetList.contains(s)).collect(Collectors.toList());
            }
            if (subsetNameList.size() > 0) {
                List<SubsetDto> needDeleteSubsetList = new ArrayList<>();
                for (String name : subsetNameList) {
                    SubsetDto subsetDto = new SubsetDto();
                    subsetDto.setName(name + "-" + serviceProxyDto.getId() + "-" + virtualGatewayDto.getGwClusterName());
                    needDeleteSubsetList.add(subsetDto);
                }
                serviceProxyDto.setSubsets(needDeleteSubsetList);
                if (!offlineToGateway(serviceProxyDto)) {
                    logger.error("[updateServiceSubsets] failed offlineToGateway, serviceProxyDto: {}", serviceProxyDto);
                    return false;
                }
            }
        }
        serviceProxyDto.setSubsets(subsets);
        if (serviceProxyDto.getHealthCheckRule() == null){
            HealthCheckRuleDto healthCheckRule = envoyHealthCheckService.getHealthCheckRule(serviceProxyDto.getId());
            serviceProxyDto.setHealthCheckRule(healthCheckRule);
        }
        if (!publishToGateway(serviceProxyDto)) {
            return false;
        }
        return true;
    }


    @Override
    public Boolean refreshRouteHost(Long vgId, Long serviceId, String hosts){
        ServiceProxyDto serviceProxyInfoInDB = serviceProxyService.get(serviceId);
        Set<String> dbHost = CommonUtil.splitStringToStringSet(serviceProxyInfoInDB.getHosts(), ",");
        Set<String> targetHost = CommonUtil.splitStringToStringSet(hosts, ",");

        //域名相同不需要刷新
        if (CommonUtil.equalSet(dbHost, targetHost)){
            return Boolean.TRUE;
        }
        return envoyServiceRefreshService.refreshRoute(vgId, serviceId, targetHost);
    }


    @Override
    public boolean offlineToGateway(ServiceProxyDto serviceProxyDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyDto.getVirtualGwId());
        if (virtualGatewayDto == null) {
            logger.warn("网关信息为空");
            return false;
        }

        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DeleteService");
        params.put("Version", "2019-07-25");
        DpServiceProxyDto service = toView(serviceProxyDto, virtualGatewayDto);
        service.setSubsets(Collections.emptyList());
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + PLANE_PORTAL_PATH, JSONObject.toJSONString(service), params, null, EnvoyConst.MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane删除服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

    @Override
    public void deleteService(ServiceProxyDto serviceProxyDto){
        envoyHealthCheckService.deleteByServiceId(serviceProxyDto.getId());
        envoyGrpcProtobufService.deleteServiceProtobuf(serviceProxyDto.getId());
        envoyWebServiceService.deleteServiceWsdlInfo(serviceProxyDto.getVirtualGwId(),serviceProxyDto.getId());
    }


    @Override
    public Map<String, String> getExtraServiceParams(String registry) {
        return Collections.emptyMap();
    }

    @Override
    public List<BackendServiceWithPortDto> getServiceListFromApiPlane(long virtualGwId, String name, String registryCenterType) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            return new ArrayList<>();
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "GetServiceAndPortList");
        params.put("Version", "2019-07-25");
        // 通过envoy-advanced hooker获取服务参数
        params.putAll(envoyServiceProxyService.getExtraServiceParams(registryCenterType));

        if (StringUtils.isNotBlank(name)) {
            params.put("Name", name);
        }
        //serviceFilters 暂时没有场景使用，先不传该值
        params.put("Type", registryCenterType);
        HttpClientResponse response = HttpClientUtil.getRequest(virtualGatewayDto.getConfAddr() + "/api", params, EnvoyConst.MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane查询已发布服务列表，返回http status code非2xx, httpStatusCode:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return Collections.emptyList();
        }
        JSONObject jsonResult = JSONObject.parseObject(response.getResponseBody());
        JSONArray services = jsonResult.getJSONArray("ServiceList");
        return JSONObject.parseArray(services.toJSONString(), BackendServiceWithPortDto.class);
    }

    @Override
    public List<Integer> getBackendServicePorts(ServiceProxyDto serviceProxyDto) {
        return null;
    }

    @Override
    public String getBackendServiceSendToApiPlane(ServiceProxyDto serviceProxyDto) {
        if (serviceProxyDto == null){
            return Strings.EMPTY;
        }
        String backendService = serviceProxyDto.getBackendService();
        if (!BaseConst.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            return backendService;
        }
        RegistryCenterEnum registryCenterEnum = RegistryCenterEnum.get(serviceProxyDto.getRegistryCenterType());
        if (registryCenterEnum == null) {
            logger.warn("错误的注册中心类型 {}", serviceProxyDto.getRegistryCenterType());
            return backendService;
        }

        return backendService.replace('_', '-');
    }

    @Override
    public ErrorCode checkDeleteParam(ServiceProxyDto serviceProxyDto) {
        EnvoyServiceProtobufProxy envoyServiceProtobufProxy = envoyGrpcProtobufService.getServiceProtobufProxy(serviceProxyDto.getId());
        if (envoyServiceProtobufProxy != null) {
            return EnvoyErrorCode.COULD_NOT_OFFLINE_SERVICE;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<String> getSubsetsName(ServiceProxyDto serviceProxyInfo) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyInfo.getVirtualGwId());
        if (virtualGatewayDto == null) {
            logger.error("获取subsetsname存在脏数据，virtualGwId:{}", serviceProxyInfo.getVirtualGwId());
            return Lists.newArrayList();
        }
        List<String> subsetNames = Lists.newArrayList();
        //默认subset
        subsetNames.add((ServiceProxyConvert.getCode(serviceProxyInfo) + "-" + ServiceProxyConvert.getGateway(virtualGatewayDto.getGwClusterName(), virtualGatewayDto.getCode())).toLowerCase());
        if (CollectionUtils.isEmpty(serviceProxyInfo.getSubsets())) {
            return subsetNames;
        }
        //用户自定义的版本
        List<SubsetDto> subsetDtos = ServiceProxyConvert.buildSubset(serviceProxyInfo);
        List<String> subSets = subsetDtos.stream().map(SubsetDto::getName).collect(Collectors.toList());
        subsetNames.addAll(subSets);
        return subsetNames;
    }


}

