package org.hango.cloud.dashboard.envoy.meta;

/**
 * 集成info
 */
public class EnvoyIntegrationInfo {

    /**
     * 集成表的主键
     */
    private long id;

    /**
     * 集成名称
     */
    private String integrationName;

    /**
     * 集成模块发布状态，0未发布，1已下线，2已发布
     */
    private int publishStatus;

    /**
     * 发布时间，时间戳格式，精确到毫秒
     */
    private long publishTime;

    /**
     * 更新时间，时间戳格式，精确到毫秒
     */
    private long updateTime;

    /**
     * 创建时间，时间戳格式，精确到毫秒
     */
    private long createTime;

    /**
     * 集成所属项目id
     */
    private long projectId;

    /**
     * 集成规则描述信息
     */
    private String description;

    /**
     * json格式保存的集成规则
     */
    private String step;

    /**
     * 集成类型，sub表示子集成，main表示主集成
     */
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

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
