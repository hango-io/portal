package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * 路由规则Dto
 *
 * @author hzchenzhongyang 2019-09-11
 * @Modified hanjiahao
 */
public class RouteRuleDto extends RouteRuleMatchDto {

    /**
     * 路由规则id
     */
    @JSONField(name = "RouteRuleId")
    private long id;

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
    @Pattern(regexp = Const.REGEX_ROUTE_NAME, message = "参数 RouteRuleName 不能为空且长度不能超过200")
    private String routeRuleName;

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

    /**
     * 路由来源
     */
    @JSONField(name = "RouteRuleSource")
    @Pattern(regexp = "Gateway|NSF")
    private String routeRuleSource = "Gateway";

    /**
     * 路由头操作 Request/Response Add & Remove
     */
    @JSONField(name = "HeaderOperation")
    @Valid
    private EnvoyRouteRuleHeaderOperationDto headerOperation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getRouteRuleSource() {
        return routeRuleSource;
    }

    public void setRouteRuleSource(String routeRuleSource) {
        this.routeRuleSource = routeRuleSource;
    }

    public EnvoyRouteRuleHeaderOperationDto getHeaderOperation() {
        return headerOperation;
    }

    public void setHeaderOperation(EnvoyRouteRuleHeaderOperationDto headerOperation) {
        this.headerOperation = headerOperation;
    }

    public RouteRuleInfo toMeta() {
        RouteRuleInfo routeRuleInfo = new RouteRuleInfo();
        this.toRouteMeta(routeRuleInfo);
        routeRuleInfo.setId(this.getId());
        routeRuleInfo.setServiceId(this.serviceId);
        routeRuleInfo.setRouteRuleName(this.routeRuleName.trim());
        routeRuleInfo.setDescription(this.description);
        routeRuleInfo.setRouteRuleSource(this.routeRuleSource);

        if (this.headerOperation != null) {
            routeRuleInfo.setHeaderOperation(JSON.toJSONString(headerOperation));
        }
        return routeRuleInfo;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RouteRuleDto objDto = (RouteRuleDto) obj;
        //uri全部相同进行比较
        if (!this.uriMatchDto.equals(objDto.getUriMatchDto())) {
            return false;
        }
        //method全部相同进行比较，只要有一个不相同，返回false
        if (this.methodMatchDto != null && !this.methodMatchDto.equals(objDto.getMethodMatchDto())) {
            return false;
        }
        //host全部相同进行比较，只要有一个不相同，返回false
        if (this.hostMatchDto != null && !this.hostMatchDto.equals(objDto.getHostMatchDto())) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(this.headers) && (!this.headers.containsAll(objDto.getHeaders()) || !objDto.getHeaders().containsAll(this.headers))) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(this.queryParams) && (!this.queryParams.containsAll(objDto.getQueryParams()) || !objDto.getQueryParams().containsAll(this.queryParams))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUriMatchDto(), getMethodMatchDto(), getHostMatchDto(), getHeaders(), getQueryParams());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
