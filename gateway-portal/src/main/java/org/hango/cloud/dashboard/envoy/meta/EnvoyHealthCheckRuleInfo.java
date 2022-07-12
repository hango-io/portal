package org.hango.cloud.dashboard.envoy.meta;

import java.io.Serializable;

/**
 * 健康检查Meta
 *
 * @author TC_WANG
 * @date 2019/11/19 下午4:10.
 */
public class EnvoyHealthCheckRuleInfo implements Serializable {

    /**
     * 主键
     */
    private long id;

    /**
     * 创建时间
     */
    private long createTime;

    /**
     * 修改时间
     */
    private long updateTime;

    /**
     * 服务id
     */
    private long serviceId;

    /**
     * 网关id
     */
    private long gwId;

    /**
     * 主动检查开关
     */
    private int activeSwitch;

    /**
     * 检查接口path，长度限制200
     */
    private String path;

    /**
     * 超时时间，单位ms
     */
    private int timeout;

    /**
     * 健康状态码集合，默认仅包含200
     */
    private String expectedStatuses;

    /**
     * 健康实例检查间隔，单位毫秒
     */
    private int healthyInterval;

    /**
     * 健康阈值
     */
    private int healthyThreshold;

    /**
     * 异常实例检查间隔，单位毫秒
     */
    private int unhealthyInterval;

    /**
     * 异常阈值
     */
    private int unhealthyThreshold;

    /**
     * 被动检查开关
     */
    private int passiveSwitch;

    /**
     * 连续网关失败次数	，统计返回code为502、503、504的情况
     */
    private int consecutiveErrors;

    /**
     * 驱逐时间，单位毫秒
     */
    private int baseEjectionTime;

    /**
     * 最多可驱逐的实例比
     */
    private int maxEjectionPercent;

    /**
     * 最小健康实例比
     */
    private int minHealthPercent;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
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

    public String getExpectedStatuses() {
        return expectedStatuses;
    }

    public void setExpectedStatuses(String expectedStatuses) {
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
}
