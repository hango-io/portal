package org.hango.cloud.dashboard.apiserver.util;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;

public class ResultActionWithMessage {
    private int statusCode;
    private String code;
    private String message;
    private Object result;

    public ResultActionWithMessage(int statusCode, String code, String message, Object result) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public ResultActionWithMessage(ErrorCode errorCode) {
        this.statusCode = errorCode.getStatusCode();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getResult() {
        return result;
    }
}
