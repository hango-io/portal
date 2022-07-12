package org.hango.cloud.dashboard.apiserver.dto.auditdto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/12
 */
public class CallStatisticsInfoDto {

    @JSONField(name = "ServiceTag")
    private String serviceTag;

    @JSONField(name = "TotalCount")
    private long totalCount;

    @JSONField(name = "SuccessCount")
    private long successCount;

    @JSONField(name = "BadRequestCount")
    private long badRequestCount;

    // TODO G0网关暂未区分网络调用失败

    @JSONField(name = "NetworkFailedCount")
    private long networkFailedCount;

    @JSONField(name = "FailedCount")
    private long failedCount;

    @JSONField(name = "MaxDuration")
    private int maxDuration;

    @JSONField(name = "AverageDuration")
    private int averageDuration;

    @JSONField(name = "Duration95")
    private long duration95;

    @JSONField(name = "Duration99")
    private long duration99;

    @JSONField(name = "StartTime")
    private long startTime;

    @JSONField(name = "EndTime")
    private long endTime;

    @JSONField(name = "Time")
    private long time;

    @JSONField(name = "SlowRespCount")
    private long slowRespCount;

    @JSONField(name = "UserCount")
    private long userCount;

    @JSONField(name = "ApiId")
    private long apiId;

    @JSONField(name = "ApiName")
    private String apiName;

    @JSONField(name = "ApiAction")
    private String apiAction;

    @JSONField(name = "ApiVersion")
    private String apiVersion;

    @JSONField(name = "TenantId")
    private String tenantId;

    @JSONField(name = "Account")
    private String account;

    @JSONField(name = "Id")
    private String id;

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getBadRequestCount() {
        return badRequestCount;
    }

    public void setBadRequestCount(long badRequestCount) {
        this.badRequestCount = badRequestCount;
    }

    public long getNetworkFailedCount() {
        return networkFailedCount;
    }

    public void setNetworkFailedCount(long networkFailedCount) {
        this.networkFailedCount = networkFailedCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(long failedCount) {
        this.failedCount = failedCount;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public int getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(int averageDuration) {
        this.averageDuration = averageDuration;
    }

    public long getDuration95() {
        return duration95;
    }

    public void setDuration95(long duration95) {
        this.duration95 = duration95;
    }

    public long getDuration99() {
        return duration99;
    }

    public void setDuration99(long duration99) {
        this.duration99 = duration99;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSlowRespCount() {
        return slowRespCount;
    }

    public void setSlowRespCount(long slowRespCount) {
        this.slowRespCount = slowRespCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getApiId() {
        return apiId;
    }

    public void setApiId(long apiId) {
        this.apiId = apiId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiAction() {
        return apiAction;
    }

    public void setApiAction(String apiAction) {
        this.apiAction = apiAction;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
