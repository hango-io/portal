package org.hango.cloud.common.infra.healthcheck.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.healthcheck.meta.HealthCheckRuleInfo;



import java.io.Serializable;


/**
 * 更新被动健康检查规则，与api-plane进行通信
 *
 * @author TC_WANG
 */
public class PassiveHealthCheckRuleDto implements Serializable {
    /**
     * 连续网关失败次数	，统计返回code为502、503、504的情况
     */
    @JSONField(name = "ConsecutiveErrors")
    private Integer consecutiveErrors;

    /**
     * 驱逐时间，单位毫秒
     */
    @JSONField(name = "BaseEjectionTime")
    private Integer baseEjectionTime;

    /**
     * 最多可驱逐的实例比
     */
    @JSONField(name = "MaxEjectionPercent")
    private Integer maxEjectionPercent;

    /**
     * 最小健康实例比
     */
    @JSONField(name = "MinHealthPercent")
    private Integer minHealthPercent;

    public PassiveHealthCheckRuleDto() {
    }

    public PassiveHealthCheckRuleDto(HealthCheckRuleInfo healthCheckRuleInfo) {
        this.consecutiveErrors = healthCheckRuleInfo.getConsecutiveErrors();
        this.baseEjectionTime = healthCheckRuleInfo.getBaseEjectionTime();
        this.maxEjectionPercent = healthCheckRuleInfo.getMaxEjectionPercent();
        this.minHealthPercent = healthCheckRuleInfo.getMinHealthPercent();
    }

    public PassiveHealthCheckRuleDto(HealthCheckRuleDto healthCheckRuleDto) {
        this.consecutiveErrors = healthCheckRuleDto.getConsecutiveErrors();
        this.baseEjectionTime = healthCheckRuleDto.getBaseEjectionTime();
        this.maxEjectionPercent = healthCheckRuleDto.getMaxEjectionPercent();
        this.minHealthPercent = healthCheckRuleDto.getMinHealthPercent();
    }

    public Integer getConsecutiveErrors() {
        return consecutiveErrors;
    }

    public void setConsecutiveErrors(Integer consecutiveErrors) {
        this.consecutiveErrors = consecutiveErrors;
    }

    public Integer getBaseEjectionTime() {
        return baseEjectionTime;
    }

    public void setBaseEjectionTime(Integer baseEjectionTime) {
        this.baseEjectionTime = baseEjectionTime;
    }

    public Integer getMaxEjectionPercent() {
        return maxEjectionPercent;
    }

    public void setMaxEjectionPercent(Integer maxEjectionPercent) {
        this.maxEjectionPercent = maxEjectionPercent;
    }

    public Integer getMinHealthPercent() {
        return minHealthPercent;
    }

    public void setMinHealthPercent(Integer minHealthPercent) {
        this.minHealthPercent = minHealthPercent;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

