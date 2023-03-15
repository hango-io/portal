package org.hango.cloud.common.infra.base.exception;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/8/29
 */
public class ErrorCodeException extends RuntimeException {

    private ErrorCode errorCode;


    public ErrorCodeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
    }

    public static ErrorCodeException of(ErrorCode errorCode){
        return new ErrorCodeException(errorCode);
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
