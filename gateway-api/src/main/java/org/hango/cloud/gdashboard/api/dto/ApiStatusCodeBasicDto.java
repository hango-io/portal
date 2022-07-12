package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.gdashboard.api.util.Const;

import javax.validation.constraints.Pattern;

/**
 * api 返回码基本信息
 */
public class ApiStatusCodeBasicDto {

    @JSONField(name = "Id")
    private long id;
    @JSONField(name = "StatusCode")
    private long statusCode;
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;
    @JSONField(name = "ApiErrorCode")
    private String errorCode;
    @JSONField(name = "Message")
    private String message;

    public long getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(long statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
