package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationInfo;

/**
 * 集成Dto
 */
public class EnvoyIntegrationDto {

    /**
     * 集成ID
     */
    @JSONField(name = "IntegrationId")
    private long id;

    /**
     * 集成名称
     */
    @JSONField(name = "IntegrationName")
    private String integrationName;

    /**
     * 集成模块发布状态，0未发布，1已发布
     */
    @JSONField(name = "PublishStatus")
    private int publishStatus;

    /**
     * 创建时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "CreateTime")
    private long createTime;

    /**
     * 更新时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;

    /**
     * 发布时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "PublishTime")
    private long publishTime;

    /**
     * 集成具体的执行规则
     */
    @JSONField(name = "Step")
    private String step;

    /**
     * 集成描述
     */
    @JSONField(name = "IntegrationDescription")
    private String integrationDescription;

    /**
     * 集成类型，sub表示子集成，main表示主集成
     */
    @JSONField(name = "Type")
    private String type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public void setIntegrationName(String integrationName) {
        this.integrationName = integrationName;
    }

    public int getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(int publishStatus) {
        this.publishStatus = publishStatus;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getIntegrationDescription() {
        return integrationDescription;
    }

    public void setIntegrationDescription(String integrationDescription) {
        this.integrationDescription = integrationDescription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public EnvoyIntegrationInfo toMeta() {
        EnvoyIntegrationInfo info = new EnvoyIntegrationInfo();

        info.setId(this.id);
        info.setIntegrationName(this.integrationName);
        info.setPublishStatus(this.publishStatus);
        info.setPublishTime(this.publishTime);
        info.setUpdateTime(this.updateTime);
        info.setCreateTime(this.createTime);
        info.setDescription(this.integrationDescription);
        info.setStep(this.step);
        info.setType(this.type);

        return info;
    }
}
