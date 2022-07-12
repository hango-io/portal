package org.hango.cloud.dashboard.apiserver.dto.grpcdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;

/**
 * 用于发布pb中定义的API到网关
 *
 * @author TC_WANG
 * @date 2019/7/8
 */
public class PublishedRestfulApiDto {
    @JSONField(name = "ApiId")
    private long apiId;

    @JSONField(name = "Method")
    private String method;

    @JSONField(name = "ApiPath")
    private String path;

    @JSONField(name = "ApiName")
    private String apiName;

    @JSONField(name = "Type")
    private String type;

    @JSONField(name = "PbPackageName")
    private String pbPackageName;

    @JSONField(name = "PbServiceName")
    private String pbServiceName;

    @JSONField(name = "PbMethodName")
    private String pbMethodName;

    public PublishedRestfulApiDto(ApiInfo apiInfo, String pbPackageName, String pbServiceName, String pbMethodName) {
        this.apiId = apiInfo.getId();
        this.method = apiInfo.getApiMethod();
        this.path = apiInfo.getApiPath();
        this.apiName = apiInfo.getApiName();
        this.type = apiInfo.getType();
        this.pbPackageName = pbPackageName;
        this.pbServiceName = pbServiceName;
        this.pbMethodName = pbMethodName;
    }

    public long getApiId() {
        return apiId;
    }

    public void setApiId(long apiId) {
        this.apiId = apiId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPbPackageName() {
        return pbPackageName;
    }

    public void setPbPackageName(String pbPackageName) {
        this.pbPackageName = pbPackageName;
    }

    public String getPbServiceName() {
        return pbServiceName;
    }

    public void setPbServiceName(String pbServiceName) {
        this.pbServiceName = pbServiceName;
    }

    public String getPbMethodName() {
        return pbMethodName;
    }

    public void setPbMethodName(String pbMethodName) {
        this.pbMethodName = pbMethodName;
    }
}
