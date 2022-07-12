package org.hango.cloud.dashboard.envoy.web.dto.auth;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class AuthPermissionObjectDto {
    @JSONField(name = "AuthorizationObjectId")
    private String authorizationObjectId;
    @JSONField(name = "AuthPermissionList")
    private List<AuthPermissionDto> authPermissionDtoList;

    public AuthPermissionObjectDto(final String authorizationObjectId,
                                   final List<AuthPermissionDto> authPermissionDtoList) {
        this.authorizationObjectId = authorizationObjectId;
        this.authPermissionDtoList = authPermissionDtoList;
    }

    public AuthPermissionObjectDto() {
    }

    public String getAuthorizationObjectId() {
        return authorizationObjectId;
    }

    public void setAuthorizationObjectId(final String authorizationObjectId) {
        this.authorizationObjectId = authorizationObjectId;
    }

    public List<AuthPermissionDto> getAuthPermissionDtoList() {
        return authPermissionDtoList;
    }

    public void setAuthPermissionDtoList(final List<AuthPermissionDto> authPermissionDtoList) {
        this.authPermissionDtoList = authPermissionDtoList;
    }
}
