package org.hango.cloud.gdashboard.api.meta.errorcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractErrorCode {
    public static final String SUCCESS = "Success";
    private static Logger logger = LoggerFactory.getLogger(AbstractErrorCode.class);
    public String code;
    public String message;
    public String enMessage;
    public int statusCode;

    public AbstractErrorCode() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
//        HttpServletRequest request = RequestContextHolder.getRequest();
//        if (null != request && "zh".equals(request.getHeader("X-163-AcceptLanguage"))) {
        return message;
//        } else {
//            return enMessage;
//        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
