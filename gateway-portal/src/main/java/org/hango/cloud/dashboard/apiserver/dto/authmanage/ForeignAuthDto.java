package org.hango.cloud.dashboard.apiserver.dto.authmanage;

import com.alibaba.fastjson.annotation.JSONField;

public class ForeignAuthDto {
    @JSONField(name = "AuthId")
    private String authId;
    @JSONField(name = "AuthName")
    private String authName;
    @JSONField(name = "ServiceId")
    private String serviceId;
    @JSONField(name = "CreateTime")
    private long createTime;

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
