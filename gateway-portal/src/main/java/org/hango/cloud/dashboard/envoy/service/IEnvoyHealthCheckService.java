package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyActiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPassiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceInstanceDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceTrafficPolicyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;

import java.util.List;

/**
 * 健康检查Service
 *
 * @author TC_WANG
 * @date 2019/11/19 下午3:29.
 */
public interface IEnvoyHealthCheckService {

    /**
     * 更新健康检查规则，如果不存在，则新建
     *
     * @param envoyHealthCheckRuleInfo
     * @return
     */
    ErrorCode updateHealthCheckRuleParam(EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo);

    /**
     * 当服务下线时，关闭健康检查功能（不用调用apiplane接口，因为服务下线会删除dr）
     *
     * @param serviceId
     * @param gwId
     */
    void shutdownHealthCheck(long serviceId, long gwId);

    /**
     * 当服务删除时，删除该服务对应的所有健康检查规则，防止数据残留
     *
     * @param serviceId
     */
    void deleteHealthCheckRule(long serviceId);

    /**
     * 查询健康检查规则
     *
     * @param serviceId
     * @param gwId
     * @return
     */
    EnvoyHealthCheckRuleDto getHealthCheckRule(long serviceId, long gwId);

    /**
     * 查询健康检查规则
     *
     * @param serviceId
     * @param gwId
     * @return
     */
    EnvoyHealthCheckRuleInfo getHealthCheckRuleInfo(long serviceId, long gwId);

    /**
     * 查询被动健康检查规则
     *
     * @param serviceId
     * @param gwId
     * @return
     */
    EnvoyPassiveHealthCheckRuleDto getPassiveHealthCheckRule(long serviceId, long gwId);

    /**
     * 查询主动健康检查规则
     *
     * @param serviceId
     * @param gwId
     * @return
     */
    EnvoyActiveHealthCheckRuleDto getActiveHealthCheckRule(long serviceId, long gwId);

    /**
     * 查询服务实例详情
     *
     * @param serviceInfo
     * @param gatewayInfo
     * @return
     */
    List<EnvoyServiceInstanceDto> getServiceInstanceList(ServiceInfo serviceInfo, GatewayInfo gatewayInfo);

    /**
     * 查询服务健康状态
     *
     * @param serviceInfo
     * @param gatewayInfo
     * @return
     */
    Integer getServiceHealthyStatus(ServiceInfo serviceInfo, GatewayInfo gatewayInfo);

    /**
     * 更新健康检查规则时进行参数校验
     *
     * @param dto
     * @return
     */
    ErrorCode checkUpdateHealthCheckRuleParam(EnvoyHealthCheckRuleDto dto);

    /**
     * 设置健康检查
     *
     * @param trafficPolicy
     * @param healthCheckRuleInfo
     * @return
     */
    EnvoyServiceTrafficPolicyDto setHealthCheck(EnvoyServiceTrafficPolicyDto trafficPolicy, EnvoyHealthCheckRuleInfo healthCheckRuleInfo);

    /**
     * 设置subset级别的健康检查
     *
     * @param subsetDtos
     * @param healthCheckRuleInfo
     * @return
     */
    List<EnvoySubsetDto> setSubsetHealthCheck(List<EnvoySubsetDto> subsetDtos, EnvoyHealthCheckRuleInfo healthCheckRuleInfo);

}
