package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyActiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPassiveHealthCheckRuleDto;

import javax.validation.Valid;
import java.io.Serializable;

/**
 * 高级配置，包含负载均衡策略及连接池配置
 *
 * @author TC_WANG
 * @date 2020/2/3 上午10:34.
 */
public class EnvoyServiceTrafficPolicyDto implements Serializable {

    /**
     * 负载均衡策略
     */
    @Valid
    @JSONField(name = "LoadBalancer")
    private EnvoyServiceLoadBalancerDto loadBalancer;

    /**
     * 主动健康检查
     */
    @JSONField(name = "HealthCheck")
    private EnvoyActiveHealthCheckRuleDto activeHealthCheckRule;

    /**
     * 被动健康检查
     */
    @JSONField(name = "OutlierDetection")
    private EnvoyPassiveHealthCheckRuleDto passiveHealthCheckRule;

    /**
     * 连接池
     */
    @Valid
    @JSONField(name = "ConnectionPool")
    private EnvoyServiceConnectionPoolDto connectionPoolDto;

    public EnvoyServiceLoadBalancerDto getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(EnvoyServiceLoadBalancerDto loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public EnvoyServiceConnectionPoolDto getConnectionPoolDto() {
        return connectionPoolDto;
    }

    public void setConnectionPoolDto(EnvoyServiceConnectionPoolDto connectionPoolDto) {
        this.connectionPoolDto = connectionPoolDto;
    }

    public EnvoyActiveHealthCheckRuleDto getActiveHealthCheckRule() {
        return activeHealthCheckRule;
    }

    public void setActiveHealthCheckRule(EnvoyActiveHealthCheckRuleDto activeHealthCheckRule) {
        this.activeHealthCheckRule = activeHealthCheckRule;
    }

    public EnvoyPassiveHealthCheckRuleDto getPassiveHealthCheckRule() {
        return passiveHealthCheckRule;
    }

    public void setPassiveHealthCheckRule(EnvoyPassiveHealthCheckRuleDto passiveHealthCheckRule) {
        this.passiveHealthCheckRule = passiveHealthCheckRule;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
