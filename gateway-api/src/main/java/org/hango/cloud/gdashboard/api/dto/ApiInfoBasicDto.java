package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.gdashboard.api.util.Const;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author hanjiahao
 * api基本信息
 */
public class ApiInfoBasicDto {

    @JSONField(name = "ApiId")
    private long id;

    /**
     * serviceId 服务id
     */
    @NotNull
    @JSONField(name = "ServiceId")
    private long serviceId;

    /**
     * apiName api名称
     */
    @NotEmpty
    @JSONField(name = "ApiName")
    @Pattern(regexp = Const.REGEX_API_NAME)
    private String apiName;

    /**
     * apiMethod api方法类型
     */
    @JSONField(name = "ApiMethod")
    @NotEmpty
    @Pattern(regexp = Const.REGEX_API_METHOD)
    private String apiMethod;

    /**
     * apiPath api path信息
     */
    @JSONField(name = "ApiPath")
    @NotEmpty
    @Pattern(regexp = Const.REGEX_API_PATH)
    private String apiPath;

    /**
     * api类型，RESTFUL
     */
    @JSONField(name = "Type")
    @NotEmpty
    @Pattern(regexp = Const.REGEX_API_TYPE)
    private String type;

    /**
     * aliasName API标识
     */
    @JSONField(name = "AliasName")
    @Pattern(regexp = Const.REGEX_API_ALIASNAME)
    private String aliasName;

    /**
     * description API描述信息
     */
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    @JSONField(name = "DocumentStatusId")
    @NotNull
    private long documentStatusId;

    /**
     * 发布状态，0未发布，1已发布
     */
    @JSONField(name = "PublishedStatus")
    private String status;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDocumentStatusId() {
        return documentStatusId;
    }

    public void setDocumentStatusId(long documentStatusId) {
        this.documentStatusId = documentStatusId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
