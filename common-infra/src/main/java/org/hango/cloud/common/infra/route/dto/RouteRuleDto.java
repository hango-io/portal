package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.common.RouteRuleMatchDto;
import org.hango.cloud.gdashboard.api.util.Const;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author xin li
 * @date 2022/9/6 14:21
 */
public class RouteRuleDto extends RouteRuleMatchDto {

    /**
     * 路由规则id
     */
    @JSONField(name = "RouteRuleId")
    private Long id;

    /**
     * 路由规则所属服务id,路由规则从属于服务
     */
    @JSONField(name = "ServiceId")
    @Min(1)
    private long serviceId;

    /**
     * 路由规则所属服务，服务名称，用于前端展示
     */
    @JSONField(name = "ServiceName")
    private String serviceName;

    /**
     * 路由规则所属服务type，用于前端展示
     */
    @JSONField(name = "ServiceType")
    private String serviceType;


    /**
     * 路由规则名称，用于控制台展示使用
     */
    @JSONField(name = "RouteRuleName")
    @NotNull(message = "参数 RouteRuleName 缺失")
    @Pattern(regexp = BaseConst.REGEX_ROUTE_NAME, message = "参数 RouteRuleName 不能为空且长度不能超过200")
    private String routeRuleName;

    /**
     * 路由规则所属项目id
     */
    @JSONField(name = "ProjectId")
    private long projectId;

    /**
     * 路由规则发布状态 0代表未发布，1代表已发布
     */
    @JSONField(name = "PublishStatus")
    private int publishStatus;

    /**
     * 规则创建时间
     */
    @JSONField(name = "CreateTime")
    private long createTime;

    /**
     * 规则更新时间
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;

    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getRouteRuleName() {
        return routeRuleName;
    }

    public void setRouteRuleName(String routeRuleName) {
        this.routeRuleName = routeRuleName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);

    }
}
