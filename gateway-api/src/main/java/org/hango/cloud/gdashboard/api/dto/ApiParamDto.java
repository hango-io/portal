package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.gdashboard.api.util.Const;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/29 下午5:51.
 */
public class ApiParamDto implements Serializable {

    private static final long serialVersionUID = 8533186516211605817L;

    @JSONField(name = "ParamName")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String paramName;

    @JSONField(name = "ParamTypeId")
    private long paramTypeId;

    @JSONField(name = "ParamTypeName")
    private String paramTypeName;

    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    @JSONField(name = "DefValue")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String defValue;

    @JSONField(name = "ObjectId")
    private long objectId;

    @JSONField(name = "ObjectParams")
    private List<ApiParamDto> objectParams;

    @JSONField(name = "ArrayDataTypeId")
    private long arrayDataTypeId;

    @JSONField(name = "ArrayDataTypeName")
    private String arrayDataTypeName;

    @JSONField(name = "Required")
    private String required;

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public long getParamTypeId() {
        return paramTypeId;
    }

    public void setParamTypeId(long paramTypeId) {
        this.paramTypeId = paramTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefValue() {
        return defValue;
    }

    public void setDefValue(String defValue) {
        this.defValue = defValue;
    }

    public List<ApiParamDto> getObjectParams() {
        return objectParams;
    }

    public void setObjectParams(List<ApiParamDto> objectParams) {
        this.objectParams = objectParams;
    }

    public long getArrayDataTypeId() {
        return arrayDataTypeId;
    }

    public void setArrayDataTypeId(long arrayDataTypeId) {
        this.arrayDataTypeId = arrayDataTypeId;
    }

    public String getParamTypeName() {
        return paramTypeName;
    }

    public void setParamTypeName(String paramTypeName) {
        this.paramTypeName = paramTypeName;
    }

    public String getArrayDataTypeName() {
        return arrayDataTypeName;
    }

    public void setArrayDataTypeName(String arrayDataTypeName) {
        this.arrayDataTypeName = arrayDataTypeName;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiParamDto that = (ApiParamDto) o;
        return Objects.equals(paramName, that.paramName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(paramName);
    }
}
