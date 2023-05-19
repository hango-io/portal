package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.healthcheck.dto.ActiveHealthCheckRuleDto;
import org.hango.cloud.common.infra.healthcheck.dto.PassiveHealthCheckRuleDto;


import javax.validation.Valid;
import java.io.Serializable;

/**
 * 高级配置，包含负载均衡策略及连接池配置
 *
 * @author TC_WANG
 * @date 2020/2/3 上午10:34.
 */
public class ServiceTrafficPolicyDto implements Serializable {

    /**
     * 负载均衡策略
     */
    @Valid
    @JSONField(name = "LoadBalancer")
    private ServiceLoadBalancerDto loadBalancer;

    /**
     * 主动健康检查
     */
    @JSONField(name = "HealthCheck")
    private ActiveHealthCheckRuleDto activeHealthCheckRule;

    /**
     * 被动健康检查
     */
    @JSONField(name = "OutlierDetection")
    private PassiveHealthCheckRuleDto passiveHealthCheckRule;

    /**
     * 连接池
     */
    @Valid
    @JSONField(name = "ConnectionPool")
    private ServiceConnectionPoolDto connectionPoolDto;

    public ServiceLoadBalancerDto getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(ServiceLoadBalancerDto loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public ServiceConnectionPoolDto getConnectionPoolDto() {
        return connectionPoolDto;
    }

    public void setConnectionPoolDto(ServiceConnectionPoolDto connectionPoolDto) {
        this.connectionPoolDto = connectionPoolDto;
    }

    public ActiveHealthCheckRuleDto getActiveHealthCheckRule() {
        return activeHealthCheckRule;
    }

    public void setActiveHealthCheckRule(ActiveHealthCheckRuleDto activeHealthCheckRule) {
        this.activeHealthCheckRule = activeHealthCheckRule;
    }

    public PassiveHealthCheckRuleDto getPassiveHealthCheckRule() {
        return passiveHealthCheckRule;
    }

    public void setPassiveHealthCheckRule(PassiveHealthCheckRuleDto passiveHealthCheckRule) {
        this.passiveHealthCheckRule = passiveHealthCheckRule;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
