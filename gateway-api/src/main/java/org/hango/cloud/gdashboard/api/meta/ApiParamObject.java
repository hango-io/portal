package org.hango.cloud.gdashboard.api.meta;

import java.io.Serializable;

/**
 * 类型为Object的参数，其具体的value值
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/29 下午3:57.
 */
public class ApiParamObject implements Serializable {

    private static final long serialVersionUID = -8942686927933225087L;

    private long id;
    private long createDate;
    private long modifyDate;
    private String objectValue;

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

    public String getObjectValue() {
        return objectValue;
    }

    public void setObjectValue(String objectValue) {
        this.objectValue = objectValue;
    }
}
