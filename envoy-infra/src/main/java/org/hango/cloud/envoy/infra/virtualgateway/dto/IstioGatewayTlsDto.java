package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 */
public class IstioGatewayTlsDto {
    /**
     * TLS认证方式 SIMPLE/MUTUAL
     */
    @JSONField(name = "Mode")
    private String mode;

    /**
     * secret名称
     */
    @JSONField(name = "CredentialName")
    private String credentialName;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }
}
