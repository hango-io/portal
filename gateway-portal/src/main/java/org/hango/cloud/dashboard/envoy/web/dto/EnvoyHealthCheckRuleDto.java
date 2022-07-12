package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.List;

/**
 * 健康检查规则DTO
 *
 * @author TC_WANG
 * @date 2019/11/19 下午3:09.
 */
public class EnvoyHealthCheckRuleDto implements Serializable {

    /**
     * 服务id
     */
    @JSONField(name = "ServiceId")
    private long serviceId;

    /**
     * 网关id
     */
    @JSONField(name = "GwId")
    private long gwId;

    /**
     * 主动检查开关
     */
    @JSONField(name = "ActiveSwitch")
    private int activeSwitch;

    /**
     * 检查接口path，长度限制200
     */
    @JSONField(name = "Path")
    private String path;

    /**
     * 超时时间，单位ms
     */
    @JSONField(name = "Timeout")
    private int timeout;

    /**
     * 健康状态码集合，默认仅包含200
     */
    @JSONField(name = "ExpectedStatuses")
    private List<Integer> expectedStatuses;

    /**
     * 健康实例检查间隔，单位毫秒
     */
    @JSONField(name = "HealthyInterval")
    private int healthyInterval;

    /**
     * 健康阈值
     */
    @JSONField(name = "HealthyThreshold")
    private int healthyThreshold;

    /**
     * 异常实例检查间隔，单位毫秒
     */
    @JSONField(name = "UnhealthyInterval")
    private int unhealthyInterval;

    /**
     * 异常阈值
     */
    @JSONField(name = "UnhealthyThreshold")
    private int unhealthyThreshold;

    /**
     * 被动检查开关
     */
    @JSONField(name = "PassiveSwitch")
    private int passiveSwitch;

    /**
     * 连续网关失败次数	，统计返回code为502、503、504的情况
     */
    @JSONField(name = "ConsecutiveErrors")
    private int consecutiveErrors;

    /**
     * 驱逐时间，单位毫秒
     */
    @JSONField(name = "BaseEjectionTime")
    private int baseEjectionTime;

    /**
     * 最多可驱逐的实例比
     */
    @JSONField(name = "MaxEjectionPercent")
    private int maxEjectionPercent;

    /**
     * 最小健康实例比
     */
    @JSONField(name = "MinHealthPercent")
    @Min(value = 0, message = "MinHealthPercent Error")
    @Max(value = 100, message = "MinHealthPercent Error")
    private int minHealthPercent;

    public static EnvoyHealthCheckRuleInfo dtoToMeta(EnvoyHealthCheckRuleDto dto) {
        EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo = new EnvoyHealthCheckRuleInfo();
        envoyHealthCheckRuleInfo.setServiceId(dto.getServiceId());
        envoyHealthCheckRuleInfo.setGwId(dto.getGwId());
        envoyHealthCheckRuleInfo.setActiveSwitch(dto.getActiveSwitch());
        envoyHealthCheckRuleInfo.setPath(dto.getPath());
        envoyHealthCheckRuleInfo.setTimeout(dto.getTimeout());
        envoyHealthCheckRuleInfo.setExpectedStatuses(JSON.toJSONString(dto.getExpectedStatuses()));
        envoyHealthCheckRuleInfo.setHealthyInterval(dto.getHealthyInterval());
        envoyHealthCheckRuleInfo.setHealthyThreshold(dto.getHealthyThreshold());
        envoyHealthCheckRuleInfo.setUnhealthyInterval(dto.getUnhealthyInterval());
        envoyHealthCheckRuleInfo.setUnhealthyThreshold(dto.getUnhealthyThreshold());
        envoyHealthCheckRuleInfo.setPassiveSwitch(dto.getPassiveSwitch());
        envoyHealthCheckRuleInfo.setConsecutiveErrors(dto.getConsecutiveErrors());
        envoyHealthCheckRuleInfo.setBaseEjectionTime(dto.getBaseEjectionTime());
        envoyHealthCheckRuleInfo.setMaxEjectionPercent(dto.getMaxEjectionPercent());
        envoyHealthCheckRuleInfo.setMinHealthPercent(dto.getMinHealthPercent());
        return envoyHealthCheckRuleInfo;
    }

    public static EnvoyHealthCheckRuleDto metaToDto(EnvoyHealthCheckRuleInfo meta) {
        EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto = new EnvoyHealthCheckRuleDto();
        envoyHealthCheckRuleDto.setServiceId(meta.getServiceId());
        envoyHealthCheckRuleDto.setGwId(meta.getGwId());
        envoyHealthCheckRuleDto.setActiveSwitch(meta.getActiveSwitch());
        envoyHealthCheckRuleDto.setPath(meta.getPath());
        envoyHealthCheckRuleDto.setTimeout(meta.getTimeout());
        envoyHealthCheckRuleDto.setExpectedStatuses(JSON.parseObject(meta.getExpectedStatuses(), List.class));
        envoyHealthCheckRuleDto.setHealthyInterval(meta.getHealthyInterval());
        envoyHealthCheckRuleDto.setHealthyThreshold(meta.getHealthyThreshold());
        envoyHealthCheckRuleDto.setUnhealthyInterval(meta.getUnhealthyInterval());
        envoyHealthCheckRuleDto.setUnhealthyThreshold(meta.getUnhealthyThreshold());
        envoyHealthCheckRuleDto.setPassiveSwitch(meta.getPassiveSwitch());
        envoyHealthCheckRuleDto.setConsecutiveErrors(meta.getConsecutiveErrors());
        envoyHealthCheckRuleDto.setBaseEjectionTime(meta.getBaseEjectionTime());
        envoyHealthCheckRuleDto.setMaxEjectionPercent(meta.getMaxEjectionPercent());
        envoyHealthCheckRuleDto.setMinHealthPercent(meta.getMinHealthPercent());
        return envoyHealthCheckRuleDto;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public int getActiveSwitch() {
        return activeSwitch;
    }

    public void setActiveSwitch(int activeSwitch) {
        this.activeSwitch = activeSwitch;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<Integer> getExpectedStatuses() {
        return expectedStatuses;
    }

    public void setExpectedStatuses(List<Integer> expectedStatuses) {
        this.expectedStatuses = expectedStatuses;
    }

    public int getHealthyInterval() {
        return healthyInterval;
    }

    public void setHealthyInterval(int healthyInterval) {
        this.healthyInterval = healthyInterval;
    }

    public int getHealthyThreshold() {
        return healthyThreshold;
    }

    public void setHealthyThreshold(int healthyThreshold) {
        this.healthyThreshold = healthyThreshold;
    }

    public int getUnhealthyInterval() {
        return unhealthyInterval;
    }

    public void setUnhealthyInterval(int unhealthyInterval) {
        this.unhealthyInterval = unhealthyInterval;
    }

    public int getUnhealthyThreshold() {
        return unhealthyThreshold;
    }

    public void setUnhealthyThreshold(int unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
    }

    public int getPassiveSwitch() {
        return passiveSwitch;
    }

    public void setPassiveSwitch(int passiveSwitch) {
        this.passiveSwitch = passiveSwitch;
    }

    public int getConsecutiveErrors() {
        return consecutiveErrors;
    }

    public void setConsecutiveErrors(int consecutiveErrors) {
        this.consecutiveErrors = consecutiveErrors;
    }

    public int getBaseEjectionTime() {
        return baseEjectionTime;
    }

    public void setBaseEjectionTime(int baseEjectionTime) {
        this.baseEjectionTime = baseEjectionTime;
    }

    public int getMaxEjectionPercent() {
        return maxEjectionPercent;
    }

    public void setMaxEjectionPercent(int maxEjectionPercent) {
        this.maxEjectionPercent = maxEjectionPercent;
    }

    public int getMinHealthPercent() {
        return minHealthPercent;
    }

    public void setMinHealthPercent(int minHealthPercent) {
        this.minHealthPercent = minHealthPercent;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
