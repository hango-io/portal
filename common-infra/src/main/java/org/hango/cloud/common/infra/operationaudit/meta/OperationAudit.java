package org.hango.cloud.common.infra.operationaudit.meta;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

import java.util.List;

/**
 * @author zhangbaojun
 * @version $Id: AuditDto.java, v 1.0 2018年08月25日 14:39
 */
public class OperationAudit extends CommonExtensionDto {

    /**
     * 事件 UUID
     */
    @JSONField(name = "EventId")
    private String eventId;
    /**
     * 事件发生的时间戳
     */
    @JSONField(name = "EventTime")
    private Long eventTime;
    /**
     * 事件格式版本
     */
    @JSONField(name = "EventVersion")
    private String eventVersion;
    /**
     * 处理事件的服务
     */
    @JSONField(name = "EventSource")
    private String eventSource;
    /**
     * 请求的操作
     */
    @JSONField(name = "EventName")
    private String eventName;

    /**
     * 请求描述
     */
    @JSONField(name = "Description")
    private String description;
    /**
     * 源 IP
     */
    @JSONField(name = "SourceIpAddress")
    private String sourceIpAddress;
    /**
     * 请求发起方
     */
    @JSONField(name = "UserAgent")
    private String userAgent;


    /**
     * 请求ID
     */
    @JSONField(name = "RequestId")
    private String requestId;

    /**
     * 请求方式
     */
    @JSONField(name = "RequestMethod")
    private String requestMethod;
    /**
     * 请求参数
     */
    @JSONField(name = "RequestParameters")
    private String requestParameters;

    /**
     * 响应状态
     */
    @JSONField(name = "ResponseStatus")
    private Integer responseStatus;

    /**
     * 响应
     */
    @JSONField(name = "ResponseElements")
    private String responseElements;
    /**
     * 事件类型
     */
    @JSONField(name = "EventType")
    private String eventType;

    /**
     * 事件中访问的资源列表
     */
    @JSONField(name = "ResourceReports")
    private List<ResourceDataDto> resources;
    /**
     * 请求错误详述
     */
    @JSONField(name = "ApiErrorCode")
    private String errorCode;
    /**
     * 请求错误详述
     */
    @JSONField(name = "ErrorMessage")
    private String errorMessage;
    /**
     * 请求连接
     */
    @JSONField(name = "Url")
    private String url;
    /**
     * 用户身份
     */
    @JSONField(name = "UserIdentity")
    private UserIdentityEntity userIdentity;

    /**
     * 项目ID
     */
    @JSONField(name = "ProjectId")
    private String projectId;

    /**
     * 项目名称
     */
    @JSONField(name = "ProjectName")
    private String projectName;
    /**
     * 租户ID
     */
    @JSONField(name = "TenantId")
    private String tenantId;

    /**
     * 租户名称
     */
    @JSONField(name = "TenantName")
    private String tenantName;
    /**
     * OpenAPI操作
     */
    @JSONField(name = "ApiAction")
    private String apiAction;
    /**
     * OpenAPI 版本号
     */
    @JSONField(name = "ApiVersion")
    private String apiVersion;

    /**
     * 扩展信息
     */
    @JSONField(name = "Extension")
    private String extension;

    /**
     * 环境标识
     */
    private String envId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(String eventVersion) {
        this.eventVersion = eventVersion;
    }

    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getSourceIpAddress() {
        return sourceIpAddress;
    }

    public void setSourceIpAddress(String sourceIpAddress) {
        this.sourceIpAddress = sourceIpAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(String requestParameters) {
        this.requestParameters = requestParameters;
    }

    public String getResponseElements() {
        return responseElements;
    }

    public void setResponseElements(String responseElements) {
        this.responseElements = responseElements;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public List<ResourceDataDto> getResources() {
        return resources;
    }

    public void setResources(List<ResourceDataDto> resources) {
        this.resources = resources;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UserIdentityEntity getUserIdentity() {
        return userIdentity;
    }

    public void setUserIdentity(UserIdentityEntity userIdentity) {
        this.userIdentity = userIdentity;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
}
