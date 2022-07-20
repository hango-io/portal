package org.hango.cloud.dashboard.apiserver.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 创建API时使用，提供给从excel导入和nsf agent调用创建API接口使用
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/8/15 上午11:04.
 */
public class CreateApiDto implements Serializable {

    @JSONField(name = "ApiName")
    private String apiName;

    @JSONField(name = "ServiceName")
    private String serviceName;

    @JSONField(name = "Method")
    private String method;

    @JSONField(name = "Path")
    private String path;

    @JSONField(name = "Type")
    private String type;

    @JSONField(name = "Desc")
    private String desc;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
