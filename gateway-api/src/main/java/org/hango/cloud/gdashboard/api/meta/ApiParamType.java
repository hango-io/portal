package org.hango.cloud.gdashboard.api.meta;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * API 参数类型
 *
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 15:36.
 */
public class ApiParamType implements Serializable {

    private static final long serialVersionUID = 4979535318010773176L;

    private long id;

    @JSONField(serialize = false)
    private long createDate;

    @JSONField(serialize = false)
    private long modifyDate;

    private String paramType;

    @JSONField(serialize = false)
    private String location;

    @JSONField(serialize = false)
    private long modelId;

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

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getModelId() {
        return modelId;
    }

    public void setModelId(long modelId) {
        this.modelId = modelId;
    }
}
