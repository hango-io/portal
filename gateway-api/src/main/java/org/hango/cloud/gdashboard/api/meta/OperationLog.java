package org.hango.cloud.gdashboard.api.meta;

import java.io.Serializable;

/**
 * 用于记录对API的操作
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/4/24 19:47.
 */
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 713258457158224763L;

    private long id;
    private long createDate;
    private String email;
    private long ObjectId;
    private String type;
    private String operation;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getObjectId() {
        return ObjectId;
    }

    public void setObjectId(long objectId) {
        ObjectId = objectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
