package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRegistryCenterService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ActionInfoHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.audit.meta.AuditMetaData;
import org.hango.cloud.dashboard.audit.service.IAuditConfigService;
import org.hango.cloud.dashboard.envoy.dao.EnvoyHealthCheckRuleDao;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyActiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPassiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPublishServiceDto;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyHealthCheckService;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceInstanceDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceTrafficPolicyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;
import org.hango.cloud.dashboard.envoy.web.util.HttpCommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康检查Service
 *
 * @author TC_WANG
 * @date 2019/11/19 下午3:30.
 */
@Service
public class EnvoyHealthCheckServiceImpl implements IEnvoyHealthCheckService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyHealthCheckServiceImpl.class);
    private final String HEALTHY = "HEALTHY";
    private final String UNHEALTHY = "UNHEALTHY";
    @Autowired
    private EnvoyHealthCheckRuleDao envoyHealthCheckRuleDao;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IRegistryCenterService registryCenterService;
    @Autowired
    private IAuditConfigService auditConfigService;
    @Autowired
    private IGetFromApiPlaneService getFromApiPlaneService;

    @Override
    public ErrorCode updateHealthCheckRuleParam(EnvoyHealthCheckRuleInfo newEnvoyHealthCheckRuleInfo) {
        long serviceId = newEnvoyHealthCheckRuleInfo.getServiceId();
        long gwId = newEnvoyHealthCheckRuleInfo.getGwId();
        ErrorCode errorCode = CommonErrorCode.Success;

        Map<String, Object> params = new HashMap<>(2);
        params.put("serviceId", serviceId);
        params.put("gwId", gwId);
        List<EnvoyHealthCheckRuleInfo> envoyHealthCheckRuleInfoList = envoyHealthCheckRuleDao.getRecordsByField(params);
        if (envoyHealthCheckRuleInfoList.size() == 0) {
            //判断是否已存在，如果不存在则创建
            newEnvoyHealthCheckRuleInfo.setCreateTime(System.currentTimeMillis());
            newEnvoyHealthCheckRuleInfo.setUpdateTime(System.currentTimeMillis());

            //调用APIPlane更新接口
            if (!getFromApiPlaneService.publishServiceByApiPlane(null, newEnvoyHealthCheckRuleInfo)) {
                logger.warn("调用APIPlane创建健康检查配置失败");
                errorCode = CommonErrorCode.InternalServerError;
                return errorCode;
            }
            //调用成功，更新G-Portal库
            envoyHealthCheckRuleDao.add(newEnvoyHealthCheckRuleInfo);
        } else {
            //数据库有唯一键，不会存在重复的记录，直接更新
            EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfoBefore = envoyHealthCheckRuleInfoList.get(0);
            envoyHealthCheckRuleInfoBefore.setUpdateTime(System.currentTimeMillis());
            envoyHealthCheckRuleInfoBefore.setActiveSwitch(newEnvoyHealthCheckRuleInfo.getActiveSwitch());
            envoyHealthCheckRuleInfoBefore.setPassiveSwitch(newEnvoyHealthCheckRuleInfo.getPassiveSwitch());

            //开关如果是开启才执行全部更新操作，开关如果是关闭，则仅更新开关这个字段
            if (newEnvoyHealthCheckRuleInfo.getActiveSwitch() == 1) {
                envoyHealthCheckRuleInfoBefore.setPath(newEnvoyHealthCheckRuleInfo.getPath());
                envoyHealthCheckRuleInfoBefore.setTimeout(newEnvoyHealthCheckRuleInfo.getTimeout());
                envoyHealthCheckRuleInfoBefore.setExpectedStatuses(newEnvoyHealthCheckRuleInfo.getExpectedStatuses());
                envoyHealthCheckRuleInfoBefore.setHealthyInterval(newEnvoyHealthCheckRuleInfo.getHealthyInterval());
                envoyHealthCheckRuleInfoBefore.setHealthyThreshold(newEnvoyHealthCheckRuleInfo.getHealthyThreshold());
                envoyHealthCheckRuleInfoBefore.setUnhealthyInterval(newEnvoyHealthCheckRuleInfo.getUnhealthyInterval());
                envoyHealthCheckRuleInfoBefore.setUnhealthyThreshold(newEnvoyHealthCheckRuleInfo.getUnhealthyThreshold());
            }

            if (newEnvoyHealthCheckRuleInfo.getPassiveSwitch() == 1) {
                envoyHealthCheckRuleInfoBefore.setConsecutiveErrors(newEnvoyHealthCheckRuleInfo.getConsecutiveErrors());
                envoyHealthCheckRuleInfoBefore.setBaseEjectionTime(newEnvoyHealthCheckRuleInfo.getBaseEjectionTime());
                envoyHealthCheckRuleInfoBefore.setMaxEjectionPercent(newEnvoyHealthCheckRuleInfo.getMaxEjectionPercent());
                envoyHealthCheckRuleInfoBefore.setMinHealthPercent(newEnvoyHealthCheckRuleInfo.getMinHealthPercent());
            }

            //调用APIPlane更新接口
            if (!getFromApiPlaneService.publishServiceByApiPlane(null, envoyHealthCheckRuleInfoBefore)) {
                logger.warn("调用APIPlane更新健康检查配置失败");
                errorCode = CommonErrorCode.InternalServerError;
                return errorCode;
            }
            //落库
            envoyHealthCheckRuleDao.update(envoyHealthCheckRuleInfoBefore);
        }
        return CommonErrorCode.Success;
    }

    /**
     * 查询被动健康检查规则
     *
     * @param serviceId
     * @param gwId
     * @return
     */
    @Override
    public EnvoyPassiveHealthCheckRuleDto getPassiveHealthCheckRule(long serviceId, long gwId) {
        EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto = getHealthCheckRule(serviceId, gwId);
        if (envoyHealthCheckRuleDto == null || envoyHealthCheckRuleDto.getPassiveSwitch() == 0) {
            return null;
        }
        return new EnvoyPassiveHealthCheckRuleDto(envoyHealthCheckRuleDto);
    }

    /**
     * 查询主动健康检查规则
     *
     * @param serviceId
     * @param gwId
     * @return
     */
    @Override
    public EnvoyActiveHealthCheckRuleDto getActiveHealthCheckRule(long serviceId, long gwId) {
        EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto = getHealthCheckRule(serviceId, gwId);
        if (envoyHealthCheckRuleDto == null || envoyHealthCheckRuleDto.getActiveSwitch() == 0) {
            return null;
        }
        return new EnvoyActiveHealthCheckRuleDto(envoyHealthCheckRuleDto);
    }


    @Override
    public void shutdownHealthCheck(long serviceId, long gwId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("serviceId", serviceId);
        params.put("gwId", gwId);
        List<EnvoyHealthCheckRuleInfo> envoyHealthCheckRuleInfoList = envoyHealthCheckRuleDao.getRecordsByField(params);
        if (envoyHealthCheckRuleInfoList.size() != 0) {
            EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfoBefore = envoyHealthCheckRuleInfoList.get(0);
            envoyHealthCheckRuleInfoBefore.setActiveSwitch(0);
            envoyHealthCheckRuleInfoBefore.setPassiveSwitch(0);
            //落库
            envoyHealthCheckRuleDao.update(envoyHealthCheckRuleInfoBefore);
        }
    }

    @Override
    public void deleteHealthCheckRule(long serviceId) {
        envoyHealthCheckRuleDao.deleteByServiceId(serviceId);
    }

    @Override
    public EnvoyHealthCheckRuleDto getHealthCheckRule(long serviceId, long gwId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("serviceId", serviceId);
        params.put("gwId", gwId);
        List<EnvoyHealthCheckRuleInfo> envoyHealthCheckRuleInfoList = envoyHealthCheckRuleDao.getRecordsByField(params);
        if (envoyHealthCheckRuleInfoList.size() != 0) {
            return EnvoyHealthCheckRuleDto.metaToDto(envoyHealthCheckRuleInfoList.get(0));
        }
        return null;
    }

    @Override
    public EnvoyHealthCheckRuleInfo getHealthCheckRuleInfo(long serviceId, long gwId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("serviceId", serviceId);
        params.put("gwId", gwId);
        List<EnvoyHealthCheckRuleInfo> envoyHealthCheckRuleInfoList = envoyHealthCheckRuleDao.getRecordsByField(params);
        if (envoyHealthCheckRuleInfoList.size() != 0) {
            return envoyHealthCheckRuleInfoList.get(0);
        }
        return null;
    }

    @Override
    public List<EnvoyServiceInstanceDto> getServiceInstanceList(ServiceInfo serviceInfo, GatewayInfo gatewayInfo) {
        long serviceId = serviceInfo.getId();
        long gwId = gatewayInfo.getId();

        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, serviceId);
        if (serviceProxyInfo == null) {
            logger.info("未查询到具体的服务发布信息");
            return Collections.emptyList();
        }

        //调用APIPlane接口查询该服务下的所有实例
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "GetServiceHealthList");
        params.put("Version", "2019-07-25");
        params.put("Code", serviceProxyInfo.getCode());
        //获取服务实例时，区分subset
        params.put("Gateway", gatewayInfo.getGwClusterName());
        params.put("Subsets", serviceProxyService.getSubsetsName(serviceProxyInfo).stream().collect(Collectors.joining(",")));
        //静态方式发布的服务不需要组装Host
        //静态方式发布的服务的Host 在 Api-plane 进行拼接
        if (Const.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyInfo.getPublishType())) {
            params.put("Host", serviceProxyService.getBackendServiceSendToApiPlane(ServiceProxyDto.toDto(serviceProxyInfo)));
        }
        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params, null, null, HttpMethod.GET.name());
        if (response == null) {
            logger.info("调用api-plane查询服务实例列表失败，返回的Response为空！serviceId:{}和gwId:{}", serviceId, gwId);
            return Collections.emptyList();
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.info("调用api-plane查询服务实例列表失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return Collections.emptyList();
        }

        List<EnvoyServiceInstanceDto> envoyServiceInstanceDtoList = new ArrayList<>();

        try {
            EnvoyServiceInstanceListObject envoyServiceInstanceListObject = JSONObject.parseObject(response.getResponseBody(), EnvoyServiceInstanceListObject.class);
            for (InnerEnvoyServiceInstance innerEnvoyServiceInstance : envoyServiceInstanceListObject.getServiceInstanceList()) {
                //集合大小仅为1
                for (EnvoyEndPoint envoyEndPoint : innerEnvoyServiceInstance.getEnvoyEndPointList()) {
                    EnvoyServiceInstanceDto envoyServiceInstanceDto = new EnvoyServiceInstanceDto();
                    envoyServiceInstanceDto.setInstanceAddr(envoyEndPoint.getAddress());
                    envoyServiceInstanceDto.setStatus(HEALTHY.equalsIgnoreCase(envoyEndPoint.getStatus()) ? 1 : 0);
                    envoyServiceInstanceDtoList.add(envoyServiceInstanceDto);
                }
            }
        } catch (Exception e) {
            logger.info("调用api-plane查询服务实例列表成功后，解析body异常 ", e);
            return Collections.emptyList();
        }
        return envoyServiceInstanceDtoList;
    }


    @Override
    public Integer getServiceHealthyStatus(ServiceInfo serviceInfo, GatewayInfo gatewayInfo) {
        long serviceId = serviceInfo.getId();
        long gwId = gatewayInfo.getId();

        //如果未开启健康检查，则默认全部健康
        EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto = getHealthCheckRule(serviceInfo.getId(), gatewayInfo.getId());
        boolean healthCheckSwitch = envoyHealthCheckRuleDto == null || (envoyHealthCheckRuleDto.getActiveSwitch() == 0 && envoyHealthCheckRuleDto.getPassiveSwitch() == 0);
        if (healthCheckSwitch) {
            logger.info("该服务未开启健康健康配置，serviceId:{}, gwId:{}", serviceId, gwId);
            return 1;
        }

        //调用APIPlane接口查询该服务下的所有实例
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "GetServiceHealthList");
        params.put("Version", "2019-07-25");
        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, serviceId);
        if (serviceProxyInfo == null) {
            logger.info("未查询到具体的服务发布信息, serviceId:{}, gwId:{}", serviceId, gwId);
            return 1;
        }
        //获取服务实例时，区分subset
        params.put("Gateway", gatewayInfo.getGwClusterName());
        params.put("Code", serviceProxyInfo.getCode());
        params.put("Subsets", serviceProxyService.getSubsetsName(serviceProxyInfo).stream().collect(Collectors.joining(",")));
        //静态方式发布的服务不需要组装Host
        //静态方式发布的服务的Host 在 Api-plane 进行拼接
        if (Const.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyInfo.getPublishType())) {
            params.put("Host", serviceProxyService.getBackendServiceSendToApiPlane(ServiceProxyDto.toDto(serviceProxyInfo)));
        }
        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params, null, null, HttpMethod.GET.name());
        if (response == null) {
            logger.info("调用api-plane查询服务实例健康状态失败，返回的Response为空！serviceId:{}和gwId:{}", serviceId, gwId);
            return -1;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.info("调用api-plane查询服务实例健康状态失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return -1;
        }

        try {
            EnvoyServiceInstanceListObject envoyServiceInstanceListObject = JSONObject.parseObject(response.getResponseBody(), EnvoyServiceInstanceListObject.class);
            for (InnerEnvoyServiceInstance innerEnvoyServiceInstance : envoyServiceInstanceListObject.getServiceInstanceList()) {
                //集合大小仅为1
                int healthNum = 0;
                int unHealthNum = 0;
                for (EnvoyEndPoint envoyEndPoint : innerEnvoyServiceInstance.getEnvoyEndPointList()) {
                    if (HEALTHY.equalsIgnoreCase(envoyEndPoint.getStatus())) {
                        healthNum++;
                    } else {
                        unHealthNum++;
                    }
                }

                if (healthNum == innerEnvoyServiceInstance.getEnvoyEndPointList().size()) {
                    //全部健康返回1
                    return 1;
                } else if (unHealthNum == innerEnvoyServiceInstance.getEnvoyEndPointList().size()) {
                    //全部不健康返回0
                    return 0;
                } else {
                    //部分健康返回2
                    return 2;
                }
            }
        } catch (Exception e) {
            logger.info("调用api-plane查询服务实例健康状态成功后，解析body异常 ", e);
            return -1;
        }

        return 1;
    }

    @Override
    public ErrorCode checkUpdateHealthCheckRuleParam(EnvoyHealthCheckRuleDto dto) {
        long serviceId = dto.getServiceId();
        long gwId = dto.getGwId();

        if (!gatewayInfoService.isGwExists(gwId)) {
            return CommonErrorCode.InvalidParameterGwId(String.valueOf(gwId));
        }

        if (!serviceInfoService.isServiceExists(serviceId)) {
            return CommonErrorCode.InvalidParameterServiceId(String.valueOf(serviceId));
        }

        long activeSwitch = dto.getActiveSwitch();
        if (activeSwitch != 0 && activeSwitch != 1) {
            return CommonErrorCode.InvalidActiveSwitch;
        }

        if (activeSwitch == 1) {
            //先限制长度和以/开头
            String path = dto.getPath();
            if (StringUtils.isBlank(path) || path.length() > 200 || !path.startsWith("/")) {
                return CommonErrorCode.InvalidApiPath;
            }

            long timeout = dto.getTimeout();
            if (timeout <= 0) {
                //超时时间不合法
                return CommonErrorCode.InvalidTimeout;
            }

            List<Integer> expectedStatuses = dto.getExpectedStatuses();
            if (expectedStatuses.size() > 10 || expectedStatuses.size() == 0) {
                //健康状态码不合法
                return CommonErrorCode.InvalidHttpStatusCode;
            }
            for (int expectStatus : expectedStatuses) {
                if (expectStatus <= 0 || expectStatus >= 1000) {
                    //健康状态码不合法
                    return CommonErrorCode.InvalidHttpStatusCode;
                }
            }

            long healthyInterval = dto.getHealthyInterval();
            if (healthyInterval <= 0 || healthyInterval > 1000000000) {
                //健康实例检查间隔不合法
                return CommonErrorCode.InvalidHealthyInterval;
            }

            long healthyThreshold = dto.getHealthyThreshold();
            if (healthyThreshold <= 0 || healthyThreshold > 1000000000) {
                //健康阈值不合法
                return CommonErrorCode.InvalidHealthyThreshold;
            }

            long unHealthyInterval = dto.getUnhealthyInterval();
            if (unHealthyInterval <= 0 || unHealthyInterval > 1000000000) {
                //异常实例检查间隔不合法
                return CommonErrorCode.InvalidUnHealthyInterval;
            }

            long unHealthyThreshold = dto.getUnhealthyThreshold();
            if (unHealthyThreshold <= 0 || unHealthyThreshold > 1000000000) {
                //异常阈值不合法
                return CommonErrorCode.InvalidUnHealthyThreshold;
            }
        }

        long passiveSwitch = dto.getPassiveSwitch();
        if (passiveSwitch != 0 && passiveSwitch != 1) {
            //被动检查开关不合法
            return CommonErrorCode.InvalidPassiveSwitch;
        }

        if (passiveSwitch == 1) {
            long consecutiveErrors = dto.getConsecutiveErrors();
            if (consecutiveErrors <= 0 || consecutiveErrors > 1000000000) {
                //连续失败次数不合法
                return CommonErrorCode.InvalidConsecutiveErrors;
            }

            long baseEjectionTime = dto.getBaseEjectionTime();
            if (baseEjectionTime <= 0 || baseEjectionTime > 1000000000) {
                //驱逐时间不合法
                return CommonErrorCode.InvalidBaseEjectionTime;
            }

            long maxEjectionPercent = dto.getMaxEjectionPercent();
            if (maxEjectionPercent <= 0 || maxEjectionPercent > 100) {
                //最多可驱逐的实例比
                return CommonErrorCode.InvalidMaxEjectionPercent;
            }
        }
        return CommonErrorCode.Success;
    }

    /**
     * 调用APIplane接口更新健康检查规则，类似于服务发布是全量更新
     *
     * @param envoyHealthCheckRuleInfo
     * @return
     */
    public ErrorCode publishHealthCheckRuleByApiPlane(EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo) {
        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(envoyHealthCheckRuleInfo.getGwId(), envoyHealthCheckRuleInfo.getServiceId());
        ServiceProxyDto serviceProxyDto = ServiceProxyDto.toDto(serviceProxyInfo);

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
            //网关集群名称
            envoyPublishServiceDto.setGateway(gatewayInfo.getGwClusterName());
        }
        String backendService = serviceProxyService.getBackendServiceSendToApiPlane(serviceProxyDto);
        envoyPublishServiceDto.setBackendService(backendService);
        envoyPublishServiceDto.setType(serviceProxyDto.getPublishType());
        envoyPublishServiceDto.setProtocol(serviceProxyDto.getPublishProtocol());

        //发布服务增加服务标识
        envoyPublishServiceDto.setServiceTag(serviceInfoService.getServiceByServiceId(serviceProxyDto.getServiceId()).getServiceName());
        envoyPublishServiceDto.setLoadBalancer(serviceProxyDto.getLoadBalancer());

        //增加高级配置
        EnvoyServiceTrafficPolicyDto envoyServiceTrafficPolicyDto = serviceProxyDto.getTrafficPolicy() == null
                ? new EnvoyServiceTrafficPolicyDto() : serviceProxyDto.getTrafficPolicy();
        //增加健康检查
        envoyServiceTrafficPolicyDto = setHealthCheck(envoyServiceTrafficPolicyDto, envoyHealthCheckRuleInfo);

        //获取subsets，配置健康检查
        List<EnvoySubsetDto> envoySubsetDtos = serviceProxyService.setSubsetForDtoWhenSendToAPIPlane(serviceProxyDto, gatewayInfo.getGwClusterName());
        //增加版本信息
        envoyPublishServiceDto.setSubsets(setSubsetHealthCheck(envoySubsetDtos, envoyHealthCheckRuleInfo));

        //增加连接池和负载均衡策略
        envoyPublishServiceDto.setTrafficPolicy(envoyServiceTrafficPolicyDto);

        try {
            //配置审计
            auditConfigService.record(new AuditMetaData(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                    ActionInfoHolder.getAction(), JSONObject.parseObject(JSON.toJSONString(envoyPublishServiceDto))));
            HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params, JSONObject.toJSONString(envoyPublishServiceDto), null, HttpMethod.POST.name());
            if (response == null) {
                return CommonErrorCode.InternalServerError;
            }
            if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
                logger.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return CommonErrorCode.InternalServerError;
            }
        } catch (Exception e) {
            logger.error("调用api-plane发布接口异常，e:{}", e);
            return CommonErrorCode.InternalServerError;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public EnvoyServiceTrafficPolicyDto setHealthCheck(EnvoyServiceTrafficPolicyDto trafficPolicy, EnvoyHealthCheckRuleInfo healthCheckRuleInfo) {
        trafficPolicy = trafficPolicy == null ? new EnvoyServiceTrafficPolicyDto() : trafficPolicy;
        trafficPolicy.setPassiveHealthCheckRule(healthCheckRuleInfo.getPassiveSwitch() == 1 ? new EnvoyPassiveHealthCheckRuleDto(healthCheckRuleInfo) : null);
        trafficPolicy.setActiveHealthCheckRule(healthCheckRuleInfo.getActiveSwitch() == 1 ? new EnvoyActiveHealthCheckRuleDto(healthCheckRuleInfo) : null);
        return trafficPolicy;
    }

    @Override
    public List<EnvoySubsetDto> setSubsetHealthCheck(List<EnvoySubsetDto> subsetDtos, EnvoyHealthCheckRuleInfo healthCheckRuleInfo) {
        if (CollectionUtils.isEmpty(subsetDtos)) {
            return Lists.newArrayList();
        }
        return subsetDtos.stream().map(item -> {
            item.setTrafficPolicy(setHealthCheck(item.getTrafficPolicy(), healthCheckRuleInfo));
            return item;
        }).collect(Collectors.toList());
    }

    static class EnvoyServiceInstanceListObject implements Serializable {

        @JSONField(name = "RequestId")
        String requestId;

        /**
         * 实例列表
         */
        @JSONField(name = "List")
        List<InnerEnvoyServiceInstance> serviceInstanceList;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public List<InnerEnvoyServiceInstance> getServiceInstanceList() {
            return serviceInstanceList;
        }

        public void setServiceInstanceList(List<InnerEnvoyServiceInstance> serviceInstanceList) {
            this.serviceInstanceList = serviceInstanceList;
        }
    }

    static class InnerEnvoyServiceInstance implements Serializable {
        /**
         * 如果是动态发布则为service域名
         */
        @JSONField(name = "Name")
        private String Name;

        /**
         * 实例集合
         */
        @JSONField(name = "Endpoints")
        private List<EnvoyEndPoint> envoyEndPointList;

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public List<EnvoyEndPoint> getEnvoyEndPointList() {
            return envoyEndPointList;
        }

        public void setEnvoyEndPointList(List<EnvoyEndPoint> envoyEndPointList) {
            this.envoyEndPointList = envoyEndPointList;
        }
    }

    static class EnvoyEndPoint implements Serializable {
        /**
         * ip + port
         */
        @JSONField(name = "address")
        private String address;

        /**
         * 状态包含两种：HEALTHY表示健康；UNHEALTHY标识不健康
         */
        @JSONField(name = "Status")
        private String status;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

}
