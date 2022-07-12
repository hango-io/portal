package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.gdashboard.api.util.Const;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * API header param dto
 *
 * @author hanjiahao
 */
public class ApiHeaderBasicDto {

    @JSONField(name = "Id")
    private long id;

    /**
     * 参数名称
     */
    @JSONField(name = "ParamName")
    @NotBlank(message = "ParamName为空")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String paramName;

    /**
     * 参数值
     */
    @JSONField(name = "ParamValue")
    private String paramValue;

    /**
     * 参数描述
     */
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public String getParamName() {
        return paramName;
    }

    public void setParamName(@NotNull String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
