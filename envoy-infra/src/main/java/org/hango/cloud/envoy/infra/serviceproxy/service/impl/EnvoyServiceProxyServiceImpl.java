package org.hango.cloud.envoy.infra.serviceproxy.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.hango.cloud.common.infra.base.dto.ResourceDTO;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.meta.ResourceEnum;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.healthcheck.dto.ActiveHealthCheckRuleDto;
import org.hango.cloud.common.infra.healthcheck.dto.HealthCheckRuleDto;
import org.hango.cloud.common.infra.healthcheck.dto.PassiveHealthCheckRuleDto;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceTrafficPolicyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SubsetDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.serviceregistry.meta.RegistryCenterEnum;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.config.EnvoyConfig;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.service.VersionManagerService;
import org.hango.cloud.envoy.infra.healthcheck.dto.EnvoyServiceInstanceDto;
import org.hango.cloud.envoy.infra.healthcheck.dto.HealthStatusEnum;
import org.hango.cloud.envoy.infra.healthcheck.service.IEnvoyHealthCheckService;
import org.hango.cloud.common.infra.serviceproxy.convert.ServiceProxyConvert;
import org.hango.cloud.envoy.infra.serviceproxy.dto.DpServiceProxyDto;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceProxyService;
import org.hango.cloud.envoy.infra.serviceregistry.service.IEnvoyServiceRegistryService;
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
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IEnvoyServiceRegistryService registryService;

    @Autowired
    private EnvoyConfig envoyConfig;

    @Autowired
    private IEnvoyServiceRegistryService envoyServiceRegistryService;

    @Autowired
    private IEnvoyHealthCheckService envoyHealthCheckService;

    @Autowired
    private IEnvoyServiceProxyService envoyServiceProxyService;
    @Autowired
    private VersionManagerService versionManagerService;



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
        ResourceDTO resourceDTO = versionManagerService.getResourceDTO(serviceProxyDto.getVirtualGwId(), serviceProxyDto.getServiceId(), ResourceEnum.Service);
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
        dpServiceProxyDto.setCode(ServiceProxyConvert.getCode(serviceProxyDto.getPublishType(), serviceProxyDto.getServiceId()));
        dpServiceProxyDto.setGateway(ServiceProxyConvert.getGateway(virtualGatewayDto.getGwClusterName() , virtualGatewayDto.getCode()));
        //check 原代码: getBackendServiceSendToApiPlane(serviceProxyDto)
        dpServiceProxyDto.setBackendService(getBackendServiceSendToApiPlane(serviceProxyDto));
        dpServiceProxyDto.setType(serviceProxyDto.getPublishType());
        dpServiceProxyDto.setProtocol(serviceProxyDto.getPublishProtocol());
        dpServiceProxyDto.setServiceTag(serviceProxyDto.getCode());
        dpServiceProxyDto.setLoadBalancer(serviceProxyDto.getLoadBalancer());
        //check 原代码: dpServiceProxyDto.setSubsets(serviceProxyService.setSubsetForDtoWhenSendToDataPlane(serviceProxyDto, serviceProxyDto.getGwClusterName()));
        dpServiceProxyDto.setSubsets(ServiceProxyConvert.buildSubset(serviceProxyDto, virtualGatewayDto));
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
        return dpServiceProxyDto;
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
        ServiceProxyDto serviceProxyInfoInDB = serviceProxyService.getServiceProxyByServiceIdAndGwId(serviceProxyDto.getVirtualGwId(), serviceProxyDto.getServiceId());
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
                    subsetDto.setName(name + "-" + serviceProxyDto.getServiceId() + "-" + virtualGatewayDto.getGwClusterName());
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
            HealthCheckRuleDto healthCheckRule = envoyHealthCheckService.getHealthCheckRule(serviceProxyDto.getServiceId(), serviceProxyDto.getVirtualGwId());
            serviceProxyDto.setHealthCheckRule(healthCheckRule);
        }
        if (!publishToGateway(serviceProxyDto)) {
            return false;
        }
        return true;
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
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + PLANE_PORTAL_PATH, JSONObject.toJSONString(toView(serviceProxyDto,virtualGatewayDto)), params, null, EnvoyConst.MODULE_API_PLANE);
        if (response == null) {
            return false;
        }
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane删除服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }


    /**
     * 用于仅删除服务版本，服务更新时
     *
     * @param serviceProxyDto
     * @return
     */
    DpServiceProxyDto deleteSomeSubset(ServiceProxyDto serviceProxyDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyDto.getVirtualGwId());
        if (virtualGatewayDto == null) {
            return null;
        }
        ServiceDto serviceDto = serviceInfoService.get(serviceProxyDto.getServiceId());
        if (serviceDto == null) {
            return null;
        }
        DpServiceProxyDto envoyDpServiceProxyDto = new DpServiceProxyDto();
        envoyDpServiceProxyDto.setCode(serviceProxyDto.getCode());
        //网关集群名称
        envoyDpServiceProxyDto.setGateway(virtualGatewayDto.getGwClusterName());
        envoyDpServiceProxyDto.setBackendService(serviceProxyDto.getBackendService());
        envoyDpServiceProxyDto.setType(serviceProxyDto.getPublishType());
        envoyDpServiceProxyDto.setServiceTag(serviceDto.getServiceName());
        envoyDpServiceProxyDto.setProtocol(serviceProxyDto.getPublishProtocol());
        envoyDpServiceProxyDto.setSubsets(serviceProxyDto.getSubsets());
        //网关集群名称
        envoyDpServiceProxyDto.setGateway(virtualGatewayDto.getGwClusterName());
        return envoyDpServiceProxyDto;
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
    public List<ServiceProxyDto> getServiceWithHealthStatus(Long virtualGwId, Long serviceId) {
        List<ServiceProxyDto> serviceProxyDtos = serviceProxyService.getServiceProxy(virtualGwId, serviceId);
        if (CollectionUtils.isEmpty(serviceProxyDtos)){
            return new ArrayList<>();
        }
        for (ServiceProxyDto serviceProxyDto : serviceProxyDtos) {
            serviceProxyDto.setHealthyStatus(HealthStatusEnum.HEALTHY.getValue());
            List<EnvoyServiceInstanceDto> serviceInstanceDtos = envoyHealthCheckService.getServiceInstanceList(serviceId, serviceProxyDto.getVirtualGwId());
            for (EnvoyServiceInstanceDto serviceInstanceDto : serviceInstanceDtos) {
                if (HealthStatusEnum.UNHEALTHY.getValue().equals(serviceInstanceDto.getStatus())){
                    serviceProxyDto.setHealthyStatus(HealthStatusEnum.UNHEALTHY.getValue());
                    break;
                }
            }
        }
        return serviceProxyDtos;
    }
}

