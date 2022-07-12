package org.hango.cloud.dashboard.apiserver.meta.audit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "G0CallStatisticsInfo")
@CompoundIndexes({@CompoundIndex(name = "time_service_tag", def = "{'startTime':1, 'endTime':1,'serviceTag': 1}")})
public class CallStatisticsInfo implements Comparable<CallStatisticsInfo> {

    private String serviceTag;
    private long totalCount;
    private long successCount;
    private long badRequestCount;
    // TODO G0网关暂未区分网络调用失败
    private long networkFailedCount;
    private long failedCount;
    private int maxDuration;
    private int averageDuration;
    private long duration95;
    private long duration99;
    private long startTime;
    private long endTime;
    private long time;
    private long slowRespCount;
    private long userCount;
    private long apiId;
    private String apiName;
    private String apiAction;
    private String apiVersion;
    private String tenantId;
    private String account;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public int compareTo(CallStatisticsInfo o) {
        long compare = this.getFailedCount() - o.getFailedCount();
        if (0 == compare) {
            return (int) (this.getSlowRespCount() - o.getSlowRespCount());
        }
        return (int) compare;
    }
}
