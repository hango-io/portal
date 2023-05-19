package org.hango.cloud.common.infra.operationaudit.meta;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;


/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/5/7
 */
public class ResourceDataDto extends CommonExtensionDto {

    /**
     * 资源类型
     */
    @JSONField(name = "ResourceType")
    private String resourceType;

    /**
     * 资源ID
     */
    @JSONField(name = "ResourceId")
    private String resourceId;

    /**
     * 资源名称
     */
    @JSONField(name = "ResourceName")
    private String resourceName;

    public ResourceDataDto(String resourceType, Object resourceId, String resourceName) {
        this.resourceType = resourceType;
        this.resourceId = resourceId == null ? null : String.valueOf(resourceId);
        this.resourceName = resourceName;
    }

    public ResourceDataDto() {
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(Object resourceId) {
        this.resourceId = resourceId == null ? null : String.valueOf(resourceId);
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
}
