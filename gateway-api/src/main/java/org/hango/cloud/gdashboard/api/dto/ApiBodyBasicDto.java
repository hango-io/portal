package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.gdashboard.api.util.Const;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * api body dto
 *
 * @author hanjiahao
 */
public class ApiBodyBasicDto {

    @JSONField(name = "Id")
    private long id;
    /**
     * 参数名称
     */
    @JSONField(name = "ParamName")
    @NotBlank
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String paramName;
    /**
     * 参数类型，作为非必填字段，前端创建不需要传此参数
     */
    @JSONField(name = "ParamType")
    private String paramType;
    /**
     * 参数是否必须
     */
    @JSONField(name = "Required")
    @NotEmpty
    @Pattern(regexp = "0|1")
    private String required;
    /**
     * 默认值
     */
    @JSONField(name = "Defvalue")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String defValue;
    /**
     * 描述
     */
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;
    /**
     * paramType id，创建或修改需要对paramTypeId进行判断
     * paramTypeName 非必填，并且也不会使用paramType进行创建，实际都是通过id获取真实name
     */
    @JSONField(name = "ParamTypeId")
    private long paramTypeId;
    /**
     * 需要对arrayDataTypeID进行有效性判断，参数校验
     */
    @JSONField(name = "ArrayDataTypeId")
    private long arrayDataTypeId;
    /**
     * 不校验前端传的此参数，以arrayDataTypeId为准
     */
    @JSONField(name = "ArrayDataTypeName")
    private String arrayDataTypeName;
    @JSONField(name = "AssociationType")
    private String associationType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public String getDefValue() {
        return defValue;
    }

    public void setDefValue(String defValue) {
        this.defValue = defValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getParamTypeId() {
        return paramTypeId;
    }

    public void setParamTypeId(long paramTypeId) {
        this.paramTypeId = paramTypeId;
    }

    public long getArrayDataTypeId() {
        return arrayDataTypeId;
    }

    public void setArrayDataTypeId(long arrayDataTypeId) {
        this.arrayDataTypeId = arrayDataTypeId;
    }

    public String getAssociationType() {
        return associationType;
    }

    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

    public String getArrayDataTypeName() {
        return arrayDataTypeName;
    }

    public void setArrayDataTypeName(String arrayDataTypeName) {
        this.arrayDataTypeName = arrayDataTypeName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
