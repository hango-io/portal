package org.hango.cloud.dashboard.apiserver.dto.authmanage;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class CreateAuthDto {
    @JSONField(name = "AuthId")
    private String authId;
    @JSONField(name = "AuthName")
    private String authName;
    @JSONField(name = "EnvId")
    @NotEmpty
    private String envId;
    @JSONField(name = "ApiAuthList")
    @NotEmpty
    private List<AuthInfoDto> authInfoDtoList;

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public List<AuthInfoDto> getAuthInfoDtoList() {
        return authInfoDtoList;
    }

    public void setAuthInfoDtoList(List<AuthInfoDto> authInfoDtoList) {
        this.authInfoDtoList = authInfoDtoList;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
