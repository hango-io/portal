package org.hango.cloud.gdashboard.api.meta;

import java.io.Serializable;

/**
 * 录入Api返回码，
 * 如http status code 为400
 * 描述可填：Code: MissingParameter Message: 参数缺失
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/4/24 14:43.
 */
public class ApiStatusCode implements Serializable {

    private static final long serialVersionUID = -7294250024810080852L;

    private long id;
    private long createDate;
    private long modifyDate;
    private String errorCode;
    private String message;
    private long statusCode;
    private long objectId;
    private String type;
    private String description;

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

    public long getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(long statusCode) {
        this.statusCode = statusCode;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
