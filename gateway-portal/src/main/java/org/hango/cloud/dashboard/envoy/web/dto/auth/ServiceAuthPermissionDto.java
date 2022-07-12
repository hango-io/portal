package org.hango.cloud.dashboard.envoy.web.dto.auth;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class ServiceAuthPermissionDto {
    @JSONField(name = "ServiceId")
    private long serviceId;
    @JSONField(name = "ServiceName")
    private String displayName;
    @JSONField(name = "ServiceTag")
    private String serviceName;
    @JSONField(name = "AuthPermissionList")
    private List<AuthPermissionDto> authPermissionDtoList;

    public ServiceAuthPermissionDto(final long serviceId, final String displayName, final String serviceName,
                                    final List<AuthPermissionDto> authPermissionDtoList) {
        this.serviceId = serviceId;
        this.displayName = displayName;
        this.serviceName = serviceName;
        this.authPermissionDtoList = authPermissionDtoList;
    }

    public ServiceAuthPermissionDto() {
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(final long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
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
