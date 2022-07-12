package org.hango.cloud.dashboard.envoy.innerdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * spec_resource
 *
 * @author
 */
public class SpecResourceDto {

    /**
     * 服务模块
     */
    @JSONField(name = "ServiceModule")
    private String serviceModule;

    /**
     * 资源类型
     */
    @JSONField(name = "ResourceType")
    private String resourceType;

    /**
     * 具体资源标识
     */
    @JSONField(name = "SpecResourceId")
    private String specResourceId;

    /**
     * 具体资源名称
     */
    @JSONField(name = "SpecResourceName")
    private String specResourceName;

    public String getServiceModule() {
        return serviceModule;
    }

    public void setServiceModule(String serviceModule) {
        this.serviceModule = serviceModule;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getSpecResourceId() {
        return specResourceId;
    }

    public void setSpecResourceId(String specResourceId) {
        this.specResourceId = specResourceId;
    }

    public String getSpecResourceName() {
        return specResourceName;
    }

    public void setSpecResourceName(String specResourceName) {
        this.specResourceName = specResourceName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}