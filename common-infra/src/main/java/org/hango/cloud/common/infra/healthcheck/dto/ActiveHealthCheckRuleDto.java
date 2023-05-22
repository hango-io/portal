package org.hango.cloud.common.infra.healthcheck.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.healthcheck.meta.HealthCheckRuleInfo;

import java.io.Serializable;
import java.util.List;


/**
 * 更新主动健康检查规则，与api-plane进行通信
 *
 * @author TC_WANG
 */
public class ActiveHealthCheckRuleDto implements Serializable {
    /**
     * 检查接口path，长度限制200
     */
    @JSONField(name = "Path")
    private String path;

    /**
     * 超时时间，单位ms
     */
    @JSONField(name = "Timeout")
    private Integer timeout;

    /**
     * 健康状态码集合，默认仅包含200
     */
    @JSONField(name = "ExpectedStatuses")
    private List<Integer> expectedStatuses;

    /**
     * 健康实例检查间隔，单位毫秒
     */
    @JSONField(name = "HealthyInterval")
    private Integer healthyInterval;

    /**
     * 健康阈值
     */
    @JSONField(name = "HealthyThreshold")
    private Integer healthyThreshold;

    /**
     * 异常实例检查间隔，单位毫秒
     */
    @JSONField(name = "UnhealthyInterval")
    private Integer unhealthyInterval;

    /**
     * 异常阈值
     */
    @JSONField(name = "UnhealthyThreshold")
    private Integer unhealthyThreshold;

    public ActiveHealthCheckRuleDto() {
    }

    public ActiveHealthCheckRuleDto(HealthCheckRuleInfo healthCheckRuleInfo) {
        this.path = healthCheckRuleInfo.getPath();
        this.timeout = healthCheckRuleInfo.getTimeout();
        this.expectedStatuses = JSON.parseObject(healthCheckRuleInfo.getExpectedStatuses(), List.class);
        this.healthyInterval = healthCheckRuleInfo.getHealthyInterval();
        this.healthyThreshold = healthCheckRuleInfo.getHealthyThreshold();
        this.unhealthyInterval = healthCheckRuleInfo.getUnhealthyInterval();
        this.unhealthyThreshold = healthCheckRuleInfo.getUnhealthyThreshold();
    }

    public ActiveHealthCheckRuleDto(HealthCheckRuleDto healthCheckRuleDto) {
        this.path = healthCheckRuleDto.getPath();
        this.timeout = healthCheckRuleDto.getTimeout();
        this.expectedStatuses = healthCheckRuleDto.getExpectedStatuses();
        this.healthyInterval = healthCheckRuleDto.getHealthyInterval();
        this.healthyThreshold = healthCheckRuleDto.getHealthyThreshold();
        this.unhealthyInterval = healthCheckRuleDto.getUnhealthyInterval();
        this.unhealthyThreshold = healthCheckRuleDto.getUnhealthyThreshold();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public List<Integer> getExpectedStatuses() {
        return expectedStatuses;
    }

    public void setExpectedStatuses(List<Integer> expectedStatuses) {
        this.expectedStatuses = expectedStatuses;
    }

    public Integer getHealthyInterval() {
        return healthyInterval;
    }

    public void setHealthyInterval(Integer healthyInterval) {
        this.healthyInterval = healthyInterval;
    }

    public Integer getHealthyThreshold() {
        return healthyThreshold;
    }

    public void setHealthyThreshold(Integer healthyThreshold) {
        this.healthyThreshold = healthyThreshold;
    }

    public Integer getUnhealthyInterval() {
        return unhealthyInterval;
    }

    public void setUnhealthyInterval(Integer unhealthyInterval) {
        this.unhealthyInterval = unhealthyInterval;
    }

    public Integer getUnhealthyThreshold() {
        return unhealthyThreshold;
    }

    public void setUnhealthyThreshold(Integer unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

