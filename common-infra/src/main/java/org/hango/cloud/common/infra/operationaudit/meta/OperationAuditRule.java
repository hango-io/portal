package org.hango.cloud.common.infra.operationaudit.meta;

import org.hango.cloud.common.infra.base.meta.HttpElement;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/25
 */
public class OperationAuditRule {

    /**
     * 事件版本
     */
    private String eventVersion = "V2.1";

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 操作审计描述
     */
    private String description;

    /**
     * UA
     */
    private String userAgent = "http";

    /**
     * 事件类型
     */
    private String envType = "userwrite";

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源ID
     */
    private ResourceInfoLocation jsonPathForResourceId;

    /**
     * 资源名称
     */
    private ResourceInfoLocation jsonPathForResourceName;


    public OperationAuditRule() {
    }

    public OperationAuditRule(String eventName, String description) {
        this.eventName = eventName;
        this.description = description;
    }

    public static OperationAuditRule get() {
        return new OperationAuditRule();
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(String eventVersion) {
        this.eventVersion = eventVersion;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getEnvType() {
        return envType;
    }

    public void setEnvType(String envType) {
        this.envType = envType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public ResourceInfoLocation getJsonPathForResourceId() {
        return jsonPathForResourceId;
    }

    public void setJsonPathForResourceId(ResourceInfoLocation jsonPathForResourceId) {
        this.jsonPathForResourceId = jsonPathForResourceId;
    }

    public ResourceInfoLocation getJsonPathForResourceName() {
        return jsonPathForResourceName;
    }

    public void setJsonPathForResourceName(ResourceInfoLocation jsonPathForResourceName) {
        this.jsonPathForResourceName = jsonPathForResourceName;
    }

    public OperationAuditRule eventVersion(String eventVersion) {
        this.eventVersion = eventVersion;
        return this;
    }

    public OperationAuditRule eventName(String eventName) {
        this.eventName = eventName;
        return this;
    }

    public OperationAuditRule description(String description) {
        this.description = description;
        return this;
    }

    public OperationAuditRule userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public OperationAuditRule envType(String envType) {
        this.envType = envType;
        return this;
    }

    public OperationAuditRule resourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    /**
     * 设置操作审计资源ID读取位置
     *
     * @param jsonPathForRSI
     * @return
     */
    public OperationAuditRule jsonPathForRSI(ResourceInfoLocation jsonPathForRSI) {
        this.jsonPathForResourceId = jsonPathForRSI;
        return this;
    }

    /**
     * 设置操作审计资源名称读取位置
     *
     * @param jsonPathForRSN
     * @return
     */
    public OperationAuditRule jsonPathForRSN(ResourceInfoLocation jsonPathForRSN) {
        this.jsonPathForResourceName = jsonPathForRSN;
        return this;
    }

    /**
     * 设置操作审计资源ID读取位置
     *
     * @param location
     * @param jsonPath
     * @return
     */
    public OperationAuditRule jsonPathForRSI(HttpElement location, String jsonPath) {
        return jsonPathForRSI(new ResourceInfoLocation(location, jsonPath));
    }

    /**
     * 设置操作审计资源名称读取位置
     *
     * @param location
     * @param jsonPath
     * @return
     */
    public OperationAuditRule jsonPathForRSN(HttpElement location, String jsonPath) {
        return jsonPathForRSN(new ResourceInfoLocation(location, jsonPath));
    }

    /**
     * 设置从请求体中读取操作审计资源ID的JSON位置
     *
     * @param jsonPath
     * @return
     */
    public OperationAuditRule jsonPathForRSIReq(String jsonPath) {
        return jsonPathForRSI(HttpElement.REQUEST, jsonPath);
    }

    /**
     * 设置从请求体中读取操作审计资源名称的JSON位置
     *
     * @param jsonPath
     * @return
     */
    public OperationAuditRule jsonPathForRSNReq(String jsonPath) {
        return jsonPathForRSN(HttpElement.REQUEST, jsonPath);
    }

    /**
     * 设置从响应体中读取操作审计资源ID的JSON位置
     *
     * @param jsonPath
     * @return
     */
    public OperationAuditRule jsonPathForRSIResp(String jsonPath) {
        return jsonPathForRSI(HttpElement.RESPONSE, jsonPath);
    }

    /**
     * 设置从响应体中读取操作审计资源名称的JSON位置
     *
     * @param jsonPath
     * @return
     */
    public OperationAuditRule jsonPathForRSNResp(String jsonPath) {
        return jsonPathForRSN(HttpElement.RESPONSE, jsonPath);
    }

    /**
     * 设置从QueryString中读取操作审计资源名称的JSON位置
     *
     * @param path
     * @return
     */
    public OperationAuditRule readRSIFromQuery(String path) {
        return jsonPathForRSI(HttpElement.QUERY_STRING, path);
    }

    /**
     * 设置从QueryString中读取操作审计资源名称的JSON位置
     *
     * @param path
     * @return
     */
    public OperationAuditRule readRSNFromQuery(String path) {
        return jsonPathForRSN(HttpElement.QUERY_STRING, path);
    }
}
