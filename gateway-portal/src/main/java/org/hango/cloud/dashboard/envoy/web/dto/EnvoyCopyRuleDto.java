package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 路由复制Dto
 *
 * @author hanjiahao
 */
public class EnvoyCopyRuleDto {
    /**
     * 路由规则id
     */
    @JSONField(name = "RouteRuleId")
    @Min(1)
    private long routeRuleId;
    /**
     * 复制目标服务id
     */
    @JSONField(name = "ServiceId")
    @Min(1)
    private long serviceId;
    /**
     * 复制后的路由规则优先级
     */
    @JSONField(name = "Priority")
    private long priority;
    /**
     * 复制后的路由规则名称
     */
    @JSONField(name = "RouteRuleName")
    @Pattern(regexp = "([\\s\\S]){1,254}")
    private String routeRuleName;
    /**
     * 复制后的路由规则描述
     */
    @JSONField(name = "Description")
    private String description;
    /**
     * 复制后的路由发布网关/端口信息
     */
    @JSONField(name = "DestinationPort")
    @Valid
    private List<EnvoyCopyRulePortDto> destinationPort;

    public long getRouteRuleId() {
        return routeRuleId;
    }

    public void setRouteRuleId(long routeRuleId) {
        this.routeRuleId = routeRuleId;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public String getRouteRuleName() {
        return routeRuleName;
    }

    public void setRouteRuleName(String routeRuleName) {
        this.routeRuleName = routeRuleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<EnvoyCopyRulePortDto> getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(List<EnvoyCopyRulePortDto> destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
