package org.hango.cloud.common.infra.base.dto;

/**
 * @Author zhufengwei
 * @Date 2023/1/17
 */
/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:03
 **/
public class ResourceCheckResultDTO {
    private Long resourceId;

    private String resourceName;

    private String dbResourceInfo;

    private String crResourceInfo;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getDbResourceInfo() {
        return dbResourceInfo;
    }

    public void setDbResourceInfo(String dbResourceInfo) {
        this.dbResourceInfo = dbResourceInfo;
    }

    public String getCrResourceInfo() {
        return crResourceInfo;
    }

    public void setCrResourceInfo(String crResourceInfo) {
        this.crResourceInfo = crResourceInfo;
    }

    public ResourceCheckResultDTO(Long resourceId, String resourceName, String dbResourceInfo, String crResourceInfo) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.dbResourceInfo = dbResourceInfo;
        this.crResourceInfo = crResourceInfo;
    }

    public ResourceCheckResultDTO() {
    }

    public static ResourceCheckResultDTO of(Long resourceId, String resourceName, String dbResourceInfo, String crResourceInfo){
        return new ResourceCheckResultDTO(resourceId, resourceName, dbResourceInfo, crResourceInfo);
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}
