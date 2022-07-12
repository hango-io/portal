package org.hango.cloud.dashboard.apiserver.meta.audit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 网关审计信息，主要包括：请求、响应、用户信息三部分
 *
 * @author hzchenzhongyang
 */

@CompoundIndexes({@CompoundIndex(name = "time_idx", def = "{'time':-1}")
        , @CompoundIndex(name = "service_name_idx", def = "{'serviceName':-1}")
        , @CompoundIndex(name = "time_service_name_idx", def = "{'time':-1,'serviceName':1}")
        , @CompoundIndex(name = "time_service_duration_idx", def = "{'time':-1,'serviceName':1,'duration':-1}")})
@Document(collection = "G0AuditInfo")
public class AuditInfo {
    /**
     * 请求参数
     **/
    @Id
    private String id;
    /**
     * 如果用户请求的是真实的服务方，则uri与serviceName是一致的，否则serviceName为空
     **/
    private String uri;
    /**
     * serviceName为空的时候则证明用户请求的是一个不存在的服务方
     **/
    private String serviceName;
    private String method;
    private String action;
    private String version;
    private String reqHeaders;
    private String reqBody;
    private String queryString;

    /**
     * 响应参数
     **/
    private String respBody;
    private String respHeaders;
    private int respCode;
    /**
     * 后端服务的响应是否为json格式
     **/
    private boolean jsonResponse;

    /**
     * 用户信息
     **/
    private String tenantId;
    private String account;
    private String accessKey;
    private String userName;
    private String userIp;

    private long duration;
    private String requestId;
    private long time;
    /**
     * 请求响应来源服务方，
     **/
    private String responseService;
    private String errMsg;
    private long apiId;
    private String apiName;

    private String upstreamHost;
    private String upstreamServiceTime;

    /**
     * 代理
     */
    private String userAgent;

    /**
     * 服务消费方
     */
    private String remoteUser;

    /**
     * 返回字节长度
     */
    private String bodyBytesSent;

    /**
     * 请求头中http_referer
     */
    private String httpReferer;

    /**
     * 后端实例状态
     */
    private String upstreamStatus;

    /**
     * 响应标识
     */
    private String responseFlag;

    /**
     * 审计数据所在索引
     */
    private String auditIndex;


    private String originPath;

    private String originHost;

    private String hostName;

    private String podName;

    public AuditInfo() {
        super();
    }

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public boolean isJsonResponse() {
        return jsonResponse;
    }

    public void setJsonResponse(boolean jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getResponseService() {
        return responseService;
    }

    public void setResponseService(String responseService) {
        this.responseService = responseService;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
