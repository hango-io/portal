package org.hango.cloud.envoy.infra.healthcheck.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.healthcheck.dto.HealthCheckRuleDto;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.serviceregistry.service.IRegistryCenterService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.mapper.HealthCheckRuleMapper;
import org.hango.cloud.envoy.infra.healthcheck.dto.EnvoyServiceInstanceDto;
import org.hango.cloud.envoy.infra.healthcheck.dto.HealthStatusEnum;
import org.hango.cloud.envoy.infra.healthcheck.pojo.HealthCheckRulePO;
import org.hango.cloud.envoy.infra.healthcheck.service.IEnvoyHealthCheckService;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hango.cloud.common.infra.base.meta.BaseConst.PLANE_PORTAL_PATH;
import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.MODULE_API_PLANE;

/**
 * 健康检查Service
 *
 * @author TC_WANG
 * @date 2019/11/19 下午3:30.
 */
@Service
@SuppressWarnings({"java:S3776"})
public class EnvoyHealthCheckServiceImpl implements IEnvoyHealthCheckService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyHealthCheckServiceImpl.class);

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IRegistryCenterService registryCenterService;

    @Autowired
    private HealthCheckRuleMapper healthCheckRuleMapper;

    @Autowired
    private IEnvoyServiceProxyService envoyServiceProxyService;

    @Override
    public ErrorCode updateHealthCheckRuleParam(HealthCheckRuleDto healthCheckRuleDto) {
        ServiceProxyDto serviceProxyDto = serviceProxyService.getServiceProxyByServiceIdAndGwId(healthCheckRuleDto.getVirtualGwId(), healthCheckRuleDto.getServiceId());
        serviceProxyDto.setHealthCheckRule(healthCheckRuleDto);
        //调用APIPlane更新接口
        if (!envoyServiceProxyService.updateToGateway(serviceProxyDto)) {
            logger.warn("调用APIPlane创建健康检查配置失败");
            return CommonErrorCode.INTERNAL_SERVER_ERROR;
        }
        HealthCheckRuleDto dbHealthCheckRuleInfo = getHealthCheckRule(healthCheckRuleDto.getServiceId(), healthCheckRuleDto.getVirtualGwId());
        HealthCheckRulePO healthCheckRulePO = toMeta(healthCheckRuleDto);
        if (dbHealthCheckRuleInfo == null){
            healthCheckRuleMapper.insert(healthCheckRulePO);
        }else {
            healthCheckRulePO.setId(dbHealthCheckRuleInfo.getId());
            healthCheckRuleMapper.updateById(healthCheckRulePO);
        }

        return CommonErrorCode.SUCCESS;
    }

    private HealthCheckRulePO toMeta(HealthCheckRuleDto healthCheckRuleDto){
        return HealthCheckRulePO.builder()
                .serviceId(healthCheckRuleDto.getServiceId())
                .virtualGwId(healthCheckRuleDto.getVirtualGwId())
                .activeSwitch(healthCheckRuleDto.getActiveSwitch())
                .path(healthCheckRuleDto.getPath())
                .timeout(healthCheckRuleDto.getTimeout())
                .expectedStatuses(JSON.toJSONString(healthCheckRuleDto.getExpectedStatuses()))
                .healthyInterval(healthCheckRuleDto.getHealthyInterval())
                .healthyThreshold(healthCheckRuleDto.getHealthyThreshold())
                .unhealthyInterval(healthCheckRuleDto.getUnhealthyInterval())
                .unhealthyThreshold(healthCheckRuleDto.getUnhealthyThreshold())
                .passiveSwitch(healthCheckRuleDto.getPassiveSwitch())
                .consecutiveErrors(healthCheckRuleDto.getConsecutiveErrors())
                .baseEjectionTime(healthCheckRuleDto.getBaseEjectionTime())
                .maxEjectionPercent(healthCheckRuleDto.getMaxEjectionPercent())
                .minHealthPercent(healthCheckRuleDto.getMinHealthPercent())
                .build();
    }


    @Override
    public void deleteHealthCheck(long serviceId, long virtualGwId) {
        HealthCheckRuleDto healthCheckRule = getHealthCheckRule(serviceId, virtualGwId);
        if (healthCheckRule != null){
            healthCheckRuleMapper.deleteById(healthCheckRule.getId());
        }
    }

    @Override
    public HealthCheckRuleDto getHealthCheckRule(long serviceId, long virtualGwId) {
        HealthCheckRulePO query = HealthCheckRulePO.builder().serviceId(serviceId).virtualGwId(virtualGwId).build();
        HealthCheckRulePO healthCheckRulePO = healthCheckRuleMapper.selectOne(new QueryWrapper<>(query));
        if (healthCheckRulePO != null){
            return metaToDto(healthCheckRulePO);
        }
        return null;
    }


    @Override
    public List<EnvoyServiceInstanceDto> getServiceInstanceList(long serviceId, long virtualGwId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null){
            logger.info("未查询到虚拟网关");
            return Collections.emptyList();
        }
        ServiceProxyDto serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(virtualGwId, serviceId);
        if (serviceProxyInfo == null) {
            logger.info("未查询到具体的服务发布信息");
            return Collections.emptyList();
        }

        //调用APIPlane接口查询该服务下的所有实例
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "GetServiceHealthList");
        params.put("Version", "2019-07-25");
        params.put("Code", serviceProxyInfo.getCode());
        //获取服务实例时，区分subset
        params.put("Gateway", virtualGatewayDto.getGwClusterName());
        params.put("Subsets", String.join(",", serviceProxyService.getSubsetsName(serviceProxyInfo)));
        //静态方式发布的服务不需要组装Host
        //静态方式发布的服务的Host 在 Api-plane 进行拼接
        if (BaseConst.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyInfo.getPublishType())) {
            params.put("Host", envoyServiceProxyService.getBackendServiceSendToApiPlane(serviceProxyInfo));
        }
        HttpClientResponse response = HttpClientUtil.getRequest(virtualGatewayDto.getConfAddr()  + PLANE_PORTAL_PATH, params, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.info("调用api-plane查询服务实例列表失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return Collections.emptyList();
        }
        EnvoyServiceInstanceListObject envoyServiceInstanceListObject;
        try {
            envoyServiceInstanceListObject = JSONObject.parseObject(response.getResponseBody(), EnvoyServiceInstanceListObject.class);
        } catch (Exception e) {
            logger.info("调用api-plane查询服务实例列表成功后，解析body异常 ", e);
            return Collections.emptyList();
        }
        if (envoyServiceInstanceListObject.getServiceInstanceList() == null){
            return Collections.emptyList();
        }
        List<EnvoyServiceInstanceDto> envoyServiceInstanceDtoList = new ArrayList<>();
        for (InnerEnvoyServiceInstance innerEnvoyServiceInstance : envoyServiceInstanceListObject.getServiceInstanceList()) {
            //集合大小仅为1
            for (EnvoyEndPoint envoyEndPoint : innerEnvoyServiceInstance.getEnvoyEndPointList()) {
                EnvoyServiceInstanceDto envoyServiceInstanceDto = new EnvoyServiceInstanceDto();
                envoyServiceInstanceDto.setInstanceAddr(envoyEndPoint.getAddress());
                envoyServiceInstanceDto.setStatus(HealthStatusEnum.getValueByName(envoyEndPoint.getStatus()));

                envoyServiceInstanceDtoList.add(envoyServiceInstanceDto);
            }
        }

        return envoyServiceInstanceDtoList;
    }



    @Override
    public ErrorCode checkUpdateHealthCheckRuleParam(HealthCheckRuleDto dto) {
        long serviceId = dto.getServiceId();
        long virtualGwId = dto.getVirtualGwId();

        if (!virtualGatewayInfoService.isGwExists(virtualGwId)) {
            return CommonErrorCode.invalidParameterGwId(String.valueOf(virtualGwId));
        }

        if (!serviceInfoService.isServiceExists(serviceId)) {
            return CommonErrorCode.invalidParameterServiceId(String.valueOf(serviceId));
        }
        if (!dto.getPath().startsWith("/")){
            return CommonErrorCode.INVALID_API_PATH;
        }
        List<Integer> expectedStatuses = dto.getExpectedStatuses();
        if (!CollectionUtils.isEmpty(expectedStatuses)){
            for (int expectStatus : expectedStatuses) {
                if (expectStatus <= 0 || expectStatus >= 1000) {
                    //健康状态码不合法
                    return CommonErrorCode.INVALID_HTTP_STATUS_CODE;
                }
            }
        }
        if (dto.getPassiveSwitch() == 1) {
            long consecutiveErrors = dto.getConsecutiveErrors();
            if (consecutiveErrors <= 0 || consecutiveErrors > 1000000000) {
                //连续失败次数不合法
                return CommonErrorCode.INVALID_CONSECUTIVE_ERRORS;
            }

            long baseEjectionTime = dto.getBaseEjectionTime();
            if (baseEjectionTime <= 0 || baseEjectionTime > 1000000000) {
                //驱逐时间不合法
                return CommonErrorCode.INVALID_BASE_EJECTION_TIME;
            }

            long maxEjectionPercent = dto.getMaxEjectionPercent();
            if (maxEjectionPercent <= 0 || maxEjectionPercent > 100) {
                //最多可驱逐的实例比
                return CommonErrorCode.INVALID_MAX_EJECTION_PRECENT;
            }
        }
        return CommonErrorCode.SUCCESS;
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
         * 如果是动态发布则为service域名，如果是静态发布则为com.netease.static-{serviceId}
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


    public static HealthCheckRuleDto metaToDto(HealthCheckRulePO meta) {
        HealthCheckRuleDto healthCheckRuleDto = new HealthCheckRuleDto();
        healthCheckRuleDto.setId(meta.getId());
        healthCheckRuleDto.setServiceId(meta.getServiceId());
        healthCheckRuleDto.setVirtualGwId(meta.getVirtualGwId());
        healthCheckRuleDto.setActiveSwitch(meta.getActiveSwitch());
        healthCheckRuleDto.setPath(meta.getPath());
        healthCheckRuleDto.setTimeout(meta.getTimeout());
        healthCheckRuleDto.setExpectedStatuses(JSON.parseObject(meta.getExpectedStatuses(), List.class));
        healthCheckRuleDto.setHealthyInterval(meta.getHealthyInterval());
        healthCheckRuleDto.setHealthyThreshold(meta.getHealthyThreshold());
        healthCheckRuleDto.setUnhealthyInterval(meta.getUnhealthyInterval());
        healthCheckRuleDto.setUnhealthyThreshold(meta.getUnhealthyThreshold());
        healthCheckRuleDto.setPassiveSwitch(meta.getPassiveSwitch());
        healthCheckRuleDto.setConsecutiveErrors(meta.getConsecutiveErrors());
        healthCheckRuleDto.setBaseEjectionTime(meta.getBaseEjectionTime());
        healthCheckRuleDto.setMaxEjectionPercent(meta.getMaxEjectionPercent());
        healthCheckRuleDto.setMinHealthPercent(meta.getMinHealthPercent());
        return healthCheckRuleDto;
    }

}
