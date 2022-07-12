package org.hango.cloud.dashboard.apiserver.dto.authmanage;

import com.alibaba.fastjson.annotation.JSONField;

import javax.validation.constraints.NotNull;

public class AppObjectDto {
    @JSONField(name = "ObjectId")
    @NotNull
    private String[] objectId;
    @JSONField(name = "AppId")
    @NotNull
    private String appId;

    public String[] getObjectId() {
        return objectId;
    }

    public void setObjectId(String[] objectId) {
        this.objectId = objectId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
