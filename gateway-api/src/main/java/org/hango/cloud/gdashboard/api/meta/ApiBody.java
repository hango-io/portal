package org.hango.cloud.gdashboard.api.meta;

import java.io.Serializable;

/**
 * API body
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/2 15:32.
 */
public class ApiBody implements Serializable {

    private static final long serialVersionUID = -7114518675486156556L;

    private long id;
    private long createDate;
    private long modifyDate;
    private long apiId;
    private String paramName;
    private String paramType;
    private long paramTypeId;
    private long arrayDataTypeId;
    private String arrayDataTypeName;
    private String required;
    private String defValue;
    private String description;
    private String type;
    private String associationType;

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

    public long getApiId() {
        return apiId;
    }

    public void setApiId(long apiId) {
        this.apiId = apiId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getArrayDataTypeName() {
        return arrayDataTypeName;
    }

    public void setArrayDataTypeName(String arrayDataTypeName) {
        this.arrayDataTypeName = arrayDataTypeName;
    }

    public String getAssociationType() {
        return associationType;
    }

    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }
}
