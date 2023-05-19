package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;

public class ApiListDto {
    /**
     * 创建时间
     */
    @JSONField(name = "CreateDate")
    private long createDate;

    /**
     * 更新时间
     */
    @JSONField(name = "ModifyDate")
    private long modifyDate;

    /**
     * 发布状态
     */
    @JSONField(name = "PublishedStatus")
    private String status;

    @JSONField(name = "PublishedCount")
    private long publishedCount;

    /**
     * 同步状态
     */
    @JSONField(name = "SyncStatus")
    private int syncStatus;

    /**
     * swagger同步状态 0：本地，1：同步，2：失步
     */
    @JSONField(name = "SwaggerSync")
    private int swaggerSync;

    @JSONField(name = "ApiInfoBasic")
    private ApiInfoBasicDto apiInfoBasicDto;

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getPublishedCount() {
        return publishedCount;
    }

    public void setPublishedCount(long publishedCount) {
        this.publishedCount = publishedCount;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public int getSwaggerSync() {
        return swaggerSync;
    }

    public void setSwaggerSync(int swaggerSync) {
        this.swaggerSync = swaggerSync;
    }

    public ApiInfoBasicDto getApiInfoBasicDto() {
        return apiInfoBasicDto;
    }

    public void setApiInfoBasicDto(ApiInfoBasicDto apiInfoBasicDto) {
        this.apiInfoBasicDto = apiInfoBasicDto;
    }
}
