package org.hango.cloud.envoy.infra.trafficmark.meta;

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
    private long virtualGwId;
    /**
     * 网关名
     */
    private String virtualGwName;
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
     * 流量染色的路由规则id列表，英文逗号(,)分割
     */
    private String routeRuleIds;

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
     * 路由名称列表，英文逗号(,)分割
     */
    private String routeRuleNames;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 路由规则所属项目id
     */
    private long projectId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public String getVirtualGwName() {
        return virtualGwName;
    }

    public void setVirtualGwName(String virtualGwName) {
        this.virtualGwName = virtualGwName;
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

    public String getRouteRuleIds() {
        return routeRuleIds;
    }

    public void setRouteRuleIds(String routeRuleIds) {
        this.routeRuleIds = routeRuleIds;
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

    public String getRouteRuleNames() {
        return routeRuleNames;
    }

    public void setRouteRuleNames(String routeRuleNames) {
        this.routeRuleNames = routeRuleNames;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
