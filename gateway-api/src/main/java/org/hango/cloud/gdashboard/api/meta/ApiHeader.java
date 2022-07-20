package org.hango.cloud.gdashboard.api.meta;

import java.io.Serializable;

/**
 * API Header
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/2 15:18.
 */
public class ApiHeader implements Serializable {

    private static final long serialVersionUID = 6607329792465877814L;

    private long id;
    private long createDate;
    private long modifyDate;
    private long apiId;
    private String paramName;
    private String paramValue;
    private String description;
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
