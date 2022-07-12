package org.hango.cloud.dashboard.envoy.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 路由规则Meta
 *
 * @author hzchenzhongyang 2019-09-11
 * @Modified hanjiahao
 */
public class RouteRuleInfo extends RouteRuleMatchInfo {

    private long id;

    /**
     * 路由规则所属服务id
     */
    private long serviceId;

    /**
     * 路由规则名称，控制台展示使用
     */
    private String routeRuleName;

    /**
     * 路由规则所属项目id
     */
    private long projectId;

    /**
     * 发布状态，0代表未发布，1代表已发布
     */
    private int publishStatus;

    /**
     * 路由规则创建时间
     */
    private long createTime;
    /**
     * 路由规则更新时间
     */
    private long updateTime;

    /**
     * 路由规则描述
     */
    private String description;


    /**
     * 路由来源
     */
    private String routeRuleSource;

    /**
     * 路由头操作 Request/Response Add & Remove
     */
    private String headerOperation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getRouteRuleName() {
        return routeRuleName;
    }

    public void setRouteRuleName(String routeRuleName) {
        this.routeRuleName = routeRuleName;
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


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public int getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(int publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRouteRuleSource() {
        return routeRuleSource;
    }

    public void setRouteRuleSource(String routeRuleSource) {
        this.routeRuleSource = routeRuleSource;
    }

    public String getHeaderOperation() {
        return headerOperation;
    }

    public void setHeaderOperation(String headerOperation) {
        this.headerOperation = headerOperation;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
