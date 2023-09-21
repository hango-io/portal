package org.hango.cloud.common.advanced.audit.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/12
 */
public class AuditDto {
    /**
     * 请求参数
     **/
    @JSONField(name = "Id")
    private String id;

    /**
     * 如果用户请求的是真实的服务方，则uri与serviceName是一致的，否则serviceName为空
     **/
    @JSONField(name = "Uri")
    private String uri;

    /**
     * serviceName为空的时候则证明用户请求的是一个不存在的服务方
     **/
    @JSONField(name = "ServiceName")
    private String serviceName;

    @JSONField(name = "Method")
    private String method;

    @JSONField(name = "ReqHeaders")
    private String reqHeaders;

    @JSONField(name = "ReqBody")
    private String reqBody;

    @JSONField(name = "QueryString")
    private String queryString;

    /**
     * 响应参数
     **/
    @JSONField(name = "RespBody")
    private String respBody;

    @JSONField(name = "RespHeaders")
    private String respHeaders;

    @JSONField(name = "RespCode")
    private int respCode;

    /**
     * 用户信息
     **/
    @JSONField(name = "TenantId")
    private String tenantId;


    @JSONField(name = "UserIp")
    private String userIp;


    @JSONField(name = "Duration")
    private long duration;

    @JSONField(name = "RequestId")
    private String requestId;


    @JSONField(name = "Time")
    private long time;

    @JSONField(name = "ApiName")
    private String apiName;

    @JSONField(name = "UpstreamHost")
    private String upstreamHost;

    @JSONField(name = "UpstreamServiceTime")
    private String upstreamServiceTime;

    @JSONField(name = "UserAgent")
    private String userAgent;

    @JSONField(name = "RemoteUser")
    private String remoteUser;

    @JSONField(name = "BodyBytesSent")
    private String bodyBytesSent;

    @JSONField(name = "HttpReferer")
    private String httpReferer;

    @JSONField(name = "UpstreamStatus")
    private String upstreamStatus;

    @JSONField(name = "Detail")
    private String detail;

    /**
     * 审计数据所在索引
     */
    @JSONField(name = "AuditIndex")
    private String auditIndex;

    /**
     * 响应标识
     */
    @JSONField(name = "ResponseFlag")
    private String responseFlag;

    @JSONField(name = "OriginPath")
    private String originPath;

    @JSONField(name = "OriginHost")
    private String originHost;

    @JSONField(name = "HostName")
    private String hostName;

    @JSONField(name = "PodName")
    private String podName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getReqHeaders() {
        return reqHeaders;
    }

    public void setReqHeaders(String reqHeaders) {
        this.reqHeaders = reqHeaders;
    }

    public String getReqBody() {
        return reqBody;
    }

    public void setReqBody(String reqBody) {
        this.reqBody = reqBody;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRespBody() {
        return respBody;
    }

    public void setRespBody(String respBody) {
        this.respBody = respBody;
    }

    public String getRespHeaders() {
        return respHeaders;
    }

    public void setRespHeaders(String respHeaders) {
        this.respHeaders = respHeaders;
    }

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getUpstreamHost() {
        return upstreamHost;
    }

    public void setUpstreamHost(String upstreamHost) {
        this.upstreamHost = upstreamHost;
    }

    public String getUpstreamServiceTime() {
        return upstreamServiceTime;
    }

    public void setUpstreamServiceTime(String upstreamServiceTime) {
        this.upstreamServiceTime = upstreamServiceTime;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String getBodyBytesSent() {
        return bodyBytesSent;
    }

    public void setBodyBytesSent(String bodyBytesSent) {
        this.bodyBytesSent = bodyBytesSent;
    }

    public String getHttpReferer() {
        return httpReferer;
    }

    public void setHttpReferer(String httpReferer) {
        this.httpReferer = httpReferer;
    }

    public String getUpstreamStatus() {
        return upstreamStatus;
    }

    public void setUpstreamStatus(String upstreamStatus) {
        this.upstreamStatus = upstreamStatus;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getResponseFlag() {
        return responseFlag;
    }

    public void setResponseFlag(String responseFlag) {
        this.responseFlag = responseFlag;
    }

    public String getAuditIndex() {
        return auditIndex;
    }

    public void setAuditIndex(String auditIndex) {
        this.auditIndex = auditIndex;
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
}
