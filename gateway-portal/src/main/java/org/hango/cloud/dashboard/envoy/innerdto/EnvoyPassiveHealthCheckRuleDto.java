package org.hango.cloud.dashboard.envoy.innerdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyHealthCheckRuleDto;

import java.io.Serializable;


/**
 * 更新被动健康检查规则，与api-plane进行通信
 *
 * @author TC_WANG
 */
public class EnvoyPassiveHealthCheckRuleDto extends EnvoyPublishServiceDto implements Serializable {
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

    public EnvoyPassiveHealthCheckRuleDto() {
    }

    public EnvoyPassiveHealthCheckRuleDto(EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo) {
        this.consecutiveErrors = envoyHealthCheckRuleInfo.getConsecutiveErrors();
        this.baseEjectionTime = envoyHealthCheckRuleInfo.getBaseEjectionTime();
        this.maxEjectionPercent = envoyHealthCheckRuleInfo.getMaxEjectionPercent();
        this.minHealthPercent = envoyHealthCheckRuleInfo.getMinHealthPercent();
    }

    public EnvoyPassiveHealthCheckRuleDto(EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto) {
        this.consecutiveErrors = envoyHealthCheckRuleDto.getConsecutiveErrors();
        this.baseEjectionTime = envoyHealthCheckRuleDto.getBaseEjectionTime();
        this.maxEjectionPercent = envoyHealthCheckRuleDto.getMaxEjectionPercent();
        this.minHealthPercent = envoyHealthCheckRuleDto.getMinHealthPercent();
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

