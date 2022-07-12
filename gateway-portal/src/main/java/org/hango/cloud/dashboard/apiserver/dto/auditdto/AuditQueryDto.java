package org.hango.cloud.dashboard.apiserver.dto.auditdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.meta.BasePageInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.AuditQuery;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/11
 */
public class AuditQueryDto extends BasePageInfo {

    @JSONField(name = "ServiceName")
    private String serviceName;

    @JSONField(name = "RequestId")
    private String requestId;

    @JSONField(name = "StartTime")
    private long startTime;

    @JSONField(name = "EndTime")
    private long endTime;

    @JSONField(name = "ForceCount")
    private boolean forceCount;

    @JSONField(name = "ApiId")
    private long apiId;

    @JSONField(name = "ApiAction")
    private String apiAction;

    @JSONField(name = "ApiVersion")
    private String apiVersion;

    @JSONField(name = "ErrorType")
    private String errorType;

    @JSONField(name = "Account")
    private String account;

    @JSONField(name = "Duration")
    private int duration;

    @JSONField(name = "MinDuration")
    private int minDuration;

    @JSONField(name = "MaxDuration")
    private int maxDuration;

    @JSONField(name = "RespCode")
    private String respCode;

    @JSONField(name = "UpstreamHost")
    private String upstreamHost;

    @JSONField(name = "ScrollId")
    private String scrollId;

    @JSONField(name = "UserIp")
    private String userIp;

    /**
     * 统计趋势图显示间隔
     */
    private int step;

    @JSONField(name = "Uri")
    private String uri;

    @JSONField(name = "ResponseFlag")
    private String responseFlag;

    /**
     * 是否区分项目
     */
    @JSONField(name = "ProjectDivided")
    private boolean projectDivided = true;

    @JSONField(name = "OriginPath")
    private String originPath;

    @JSONField(name = "OriginHost")
    private String originHost;

    @JSONField(name = "HostName")
    private String hostName;

    @JSONField(name = "PodName")
    private String podName;


    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public boolean isProjectDivided() {
        return projectDivided;
    }

    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath;
    }

    public String getOriginHost() {
        return originHost;
    }

    public void setOriginHost(String originHost) {
        this.originHost = originHost;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public boolean isForceCount() {
        return forceCount;
    }

    public void setForceCount(boolean forceCount) {
        this.forceCount = forceCount;
    }

    public long getApiId() {
        return apiId;
    }

    public void setApiId(long apiId) {
        this.apiId = apiId;
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

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(int minDuration) {
        this.minDuration = minDuration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getUpstreamHost() {
        return upstreamHost;
    }

    public void setUpstreamHost(String upstreamHost) {
        this.upstreamHost = upstreamHost;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getResponseFlag() {
        return responseFlag;
    }

    public void setResponseFlag(String responseFlag) {
        this.responseFlag = responseFlag;
    }

    public boolean getProjectDivided() {
        return projectDivided;
    }

    public void setProjectDivided(boolean projectDivided) {
        this.projectDivided = projectDivided;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public AuditQuery castInfo() {
        AuditQuery auditQuery = new AuditQuery();
        auditQuery.setServiceTag(serviceName);
        auditQuery.setRequestId(requestId);
        auditQuery.setStartTime(startTime);
        auditQuery.setEndTime(endTime);
        auditQuery.setForceCount(forceCount);
        auditQuery.setApiId(apiId);
        auditQuery.setAction(apiAction);
        auditQuery.setVersion(apiVersion);
        auditQuery.setErrorType(errorType);
        auditQuery.setAccount(account);
        auditQuery.setDuration(duration);
        auditQuery.setRespCode(respCode);
        auditQuery.setLimit(limit);
        auditQuery.setOffset(offset);
        auditQuery.setStep(step);
        auditQuery.setUpstreamHost(upstreamHost);
        auditQuery.setMinDuration(minDuration);
        auditQuery.setMaxDuration(maxDuration);
        auditQuery.setScrollId(scrollId);
        auditQuery.setUri(uri);
        auditQuery.setResponseFlag(responseFlag);
        auditQuery.setProjectDivided(projectDivided);
        auditQuery.setUserIp(userIp);
        auditQuery.setHostName(hostName);
        auditQuery.setPodName(podName);
        auditQuery.setOriginHost(originHost);
        auditQuery.setOriginPath(originPath);
        return auditQuery;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
