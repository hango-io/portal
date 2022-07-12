package org.hango.cloud.dashboard.apiserver.exception;

import javax.servlet.http.HttpServletResponse;

public class AbnormalStatusCodeException extends RuntimeException {
    private int code = HttpServletResponse.SC_BAD_REQUEST;

    private AbnormalStatusCodeException(String message, int code) {
        super(message);
        this.code = code;
    }

    public static RuntimeException createAbnormalStatusCodeException(String message, int code) {
        if (code < 500) {
            return new BadRequestException(message, code);
        }
        return new AbnormalStatusCodeException(message, code);
    }

    public int getCode() {
        return code;
    }

}
