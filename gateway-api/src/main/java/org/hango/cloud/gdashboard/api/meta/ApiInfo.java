package org.hango.cloud.gdashboard.api.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * API 基本信息
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2017/12/28 19:16.
 */
public class ApiInfo implements Serializable {

    /**
     * 接口已发布
     */
    public static final String STATUS_PUBLISHED = "1";
    /**
     * 接口未发布
     */
    public static final String STATUS_UNPUBLISHED = "0";
    public static final String API_RESTFUL_TYPE = "RESTFUL";
    private static final long serialVersionUID = -2119035619361221553L;
    private final String split = "_";
    private long id;
    private long createDate;
    private long modifyDate;
    private String apiName;
    private String apiPath;
    private String apiMethod;
    /**
     * RESTFUL或ACTION
     */
    private String type;
    private long serviceId;
    /**
     * 状态，status
     */
    private String status;
    /**
     * 发布数量，发布到网关的数量
     */
    private long publishedCount = 0;
    private String description;
    private String regex;
    private long documentStatusId;
    private String requestExampleValue;
    private String responseExampleValue;
    /**
     * api 英文别名
     */
    private String aliasName;
    /**
     * api所属服务项目id
     */
    private long projectId;
    /**
     * 同步状态 0-本地数据 1-同步 2-失步
     */
    private int syncStatus;
    /**
     * swagger导入同步状态，0-本地数据 1-同步 2-失步
     */
    private int swaggerSync;

    /**
     * 外部apiId
     */
    private long extApiId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public long getPublishedCount() {
        return publishedCount;
    }

    public void setPublishedCount(long publishedCount) {
        this.publishedCount = publishedCount;
    }

    public long getDocumentStatusId() {
        return documentStatusId;
    }

    public void setDocumentStatusId(long documentStatusId) {
        this.documentStatusId = documentStatusId;
    }

    public String getRequestExampleValue() {
        return requestExampleValue;
    }

    public void setRequestExampleValue(String requestExampleValue) {
        this.requestExampleValue = requestExampleValue;
    }

    public String getResponseExampleValue() {
        return responseExampleValue;
    }

    public void setResponseExampleValue(String responseExampleValue) {
        this.responseExampleValue = responseExampleValue;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public long getExtApiId() {
        return extApiId;
    }

    public void setExtApiId(long extApiId) {
        this.extApiId = extApiId;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public int getSwaggerSync() {
        return swaggerSync;
    }

    public void setSwaggerSync(int swaggerSync) {
        this.swaggerSync = swaggerSync;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


    public String getValidString(Long serviceId) {
        return new StringBuilder().append(apiPath).append(split).append(apiMethod).append(split)
                .append(serviceId).toString();
    }
}
