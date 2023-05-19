package org.hango.cloud.common.infra.base.dto;


import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.base.meta.ResourceEnum;

import java.io.Serializable;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/22 15:29
 **/
public class ResourceDTO implements Serializable {

    private static final long serialVersionUID = 3338033352301212085L;

    @JSONField(name = "ResourceId")
    private Long resourceId;

    @JSONField(name = "ResourceType")
    private String resourceType;

    @JSONField(name = "ResourceName")
    private String resourceName;

    @JSONField(name = "ResourceVersion")
    private Long resourceVersion;


    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(Long resourceVersion) {
        this.resourceVersion = resourceVersion;
    }


    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public ResourceDTO(){

    }

    public ResourceDTO(Long resourceId, String resourceType, String resourceName, Long resourceVersion) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.resourceVersion = resourceVersion;
    }

    public static ResourceDTO of (String resourceType, Long resourceVersion, Long resourceId){
        return new ResourceDTO(resourceId, resourceType, null, resourceVersion);
    }

    public static ResourceDTO of (Long resourceVersion, Long resourceId, String resourceName){
        return new ResourceDTO(resourceId, null, resourceName, resourceVersion);
    }


    public ResourceEnum solveResourceType(){
        return ResourceEnum.getByName(resourceType);
    }

}


