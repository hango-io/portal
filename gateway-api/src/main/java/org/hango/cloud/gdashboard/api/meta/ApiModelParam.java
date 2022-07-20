package org.hango.cloud.gdashboard.api.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * API 数据模型参数
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/2 16:02.
 */
public class ApiModelParam implements Serializable {

    private static final long serialVersionUID = -2865607406484845259L;

    private long id;
    private long createDate;
    private long modifyDate;
    private long modelId;
    private String paramName;
    private long paramTypeId;
    private long objectId;
    private long arrayDataTypeId;
    private String defValue;
    private String description;
    private String required;

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

    public long getModelId() {
        return modelId;
    }

    public void setModelId(long modelId) {
        this.modelId = modelId;
    }

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

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public long getArrayDataTypeId() {
        return arrayDataTypeId;
    }

    public void setArrayDataTypeId(long arrayDataTypeId) {
        this.arrayDataTypeId = arrayDataTypeId;
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
}
