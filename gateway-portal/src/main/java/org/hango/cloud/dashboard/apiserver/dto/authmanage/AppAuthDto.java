package org.hango.cloud.dashboard.apiserver.dto.authmanage;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

public class AppAuthDto {
    @JSONField(name = "AuthId")
    private String authId;

    @JSONField(name = "AuthName")
    private String authName;
    /**
     * 被授权的对象，网关API，或网关实例
     */
    @JSONField(name = "AppObjectIds")
    private List<AppObjectDto> appObjectDtos;

    /**
     * 授权类型，NSF，Gateway,GatewayApi
     */
    @JSONField(name = "AuthType")
    private String authType;

    @JSONField(name = "ProjectId")
    private String projectId;
    /**
     * 更新授权信息需要删除的serviceIds，对应网关ApiIds
     */
    @JSONField(name = "ServiceDeleteIds")
    private List<String> serviceDeleteIds;

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public List<AppObjectDto> getAppObjectDtos() {
        return appObjectDtos;
    }

    public void setAppObjectDtos(List<AppObjectDto> appObjectDtos) {
        this.appObjectDtos = appObjectDtos;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public List<String> getServiceDeleteIds() {
        return serviceDeleteIds;
    }

    public void setServiceDeleteIds(List<String> serviceDeleteIds) {
        this.serviceDeleteIds = serviceDeleteIds;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
