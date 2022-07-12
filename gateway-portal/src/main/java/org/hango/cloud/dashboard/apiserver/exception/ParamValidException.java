package org.hango.cloud.dashboard.apiserver.exception;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;

/**
 * @author zhangbaojun
 * @version $Id: ParamValidException.java, v 1.0 2018年07月26日 10:17
 */
public class ParamValidException extends RuntimeException {


    private static final long serialVersionUID = 7502581117241462842L;

    public ParamValidException(ErrorCode errorCode) {
        super(errorCode.getCode() + "," + errorCode.getMessage() + "," + errorCode.getStatusCode());
    }
}
