package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 集成执行历史Dto
 */
public class EnvoyIntegrationExecutionHistoryDto {

    /**
     * 执行号
     */
    @JSONField(name = "ExecutionId")
    private String executionId;

    /**
     * 集成名称
     */
    @JSONField(name = "IntegrationName")
    private String integrationName;

    /**
     * 集成ID
     */
    @JSONField(name = "IntegrationId")
    private long integrationId;

    /**
     * 执行状态，0不成功，1成功
     */
    @JSONField(name = "ExecutionStatus")
    private int executionStatus;

    /**
     * 执行时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "ExecutionTime")
    private long executionTime;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public void setIntegrationName(String integrationName) {
        this.integrationName = integrationName;
    }

    public long getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(long integrationId) {
        this.integrationId = integrationId;
    }

    public int getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(int executionStatus) {
        this.executionStatus = executionStatus;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}
