package org.hango.cloud.dashboard.envoy.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 流量染色相关元数据
 *
 * @author qilu
 */
public class TrafficMarkInfo {

    /**
     * 数据库主键自增id
     */
    private long id;
    /**
     * 路由规则发布指定的网关id
     */
    private long gwId;
    /**
     * 网关名
     */
    private String gwName;
    /**
     * 流量染色规则名称
     */
    private String trafficColorName;
    /**
     * 创建时间
     */
    private long createTime;

    /**
     * 更新时间
     */
    private long updateTime;

    /**
     * 流量染色的路由规则id
     */
    private long routeRuleId;

    /**
     * 流量染色开启状态：0表示关闭；1表示开启
     */
    private int enableStatus;

    /**
     * 流量匹配 当前仅支持Header匹配
     */
    private String trafficMatch;

    /**
     * 染色标识
     */
    private String colorTag;

    /**
     * 流量染色参数
     */
    private String trafficParam;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 路由名称
     */
    private String routeRuleName;

    /**
     * 协议
     */
    private String protocol;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
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

    public long getRouteRuleId() {
        return routeRuleId;
    }

    public void setRouteRuleId(long routeRuleId) {
        this.routeRuleId = routeRuleId;
    }

    public int getEnableStatus() {
        return enableStatus;
    }

    public void setEnableStatus(int enableStatus) {
        this.enableStatus = enableStatus;
    }

    public String getTrafficMatch() {
        return trafficMatch;
    }

    public void setTrafficMatch(String trafficMatch) {
        this.trafficMatch = trafficMatch;
    }

    public String getColorTag() {
        return colorTag;
    }

    public void setColorTag(String colorTag) {
        this.colorTag = colorTag;
    }

    public String getTrafficParam() {
        return trafficParam;
    }

    public void setTrafficParam(String trafficParam) {
        this.trafficParam = trafficParam;
    }

    public String getTrafficColorName() {
        return trafficColorName;
    }

    public void setTrafficColorName(String trafficColorName) {
        this.trafficColorName = trafficColorName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRouteRuleName() {
        return routeRuleName;
    }

    public void setRouteRuleName(String routeRuleName) {
        this.routeRuleName = routeRuleName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
