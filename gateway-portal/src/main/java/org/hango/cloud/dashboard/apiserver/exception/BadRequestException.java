package org.hango.cloud.dashboard.apiserver.exception;

/**
 * Created by Zhranklin zhangwu at 2017/9/28
 */
public class BadRequestException extends RuntimeException {

    private int code;

    public BadRequestException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
