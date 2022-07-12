package org.hango.cloud.dashboard.envoy.web.dto.auth;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class RouteAuthPermissionDto {
    @JSONField(name = "RouteRuleId")
    private long routeRuleId;
    @JSONField(name = "RouteRuleName")
    private String routeRuleName;
    @JSONField(name = "ServiceId")
    private long serviceId;
    @JSONField(name = "ServiceName")
    private String displayName;
    @JSONField(name = "AuthPermissionList")
    private List<AuthPermissionDto> authPermissionDtoList;

    public RouteAuthPermissionDto(final long routeRuleId, final String routeRuleName, final long serviceId,
                                  final String displayName, final List<AuthPermissionDto> authPermissionDtoList) {
        this.routeRuleId = routeRuleId;
        this.routeRuleName = routeRuleName;
        this.serviceId = serviceId;
        this.displayName = displayName;
        this.authPermissionDtoList = authPermissionDtoList;
    }

    public RouteAuthPermissionDto() {
    }

    public long getRouteRuleId() {
        return routeRuleId;
    }

    public void setRouteRuleId(final long routeRuleId) {
        this.routeRuleId = routeRuleId;
    }

    public String getRouteRuleName() {
        return routeRuleName;
    }

    public void setRouteRuleName(final String routeRuleName) {
        this.routeRuleName = routeRuleName;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(final long serviceId) {
        this.serviceId = serviceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public List<AuthPermissionDto> getAuthPermissionDtoList() {
        return authPermissionDtoList;
    }

    public void setAuthPermissionDtoList(final List<AuthPermissionDto> authPermissionDtoList) {
        this.authPermissionDtoList = authPermissionDtoList;
    }
}
