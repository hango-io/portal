package org.hango.cloud.dashboard.apiserver.exception;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/15
 */
public class AuditDataSourceException extends RuntimeException {

    private int code;

    public AuditDataSourceException(String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
