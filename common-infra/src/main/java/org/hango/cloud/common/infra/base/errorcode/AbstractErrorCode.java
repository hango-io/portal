//package org.hango.cloud.common.infra.base.errorcode;
//
//import org.hango.cloud.common.infra.base.holder.RequestContextHolder;
//import org.hango.cloud.common.infra.base.meta.BaseConst;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.http.HttpServletRequest;
//
//public class AbstractErrorCode {
//    public static final String SUCCESS = "Success";
//    private static Logger logger = LoggerFactory.getLogger(AbstractErrorCode.class);
//    public String code;
//    public String message;
//    public String enMessage;
//    public int statusCode;
//
//    public AbstractErrorCode() {
//    }
//
//    public String getCode() {
//        return code;
//    }
//
//    public void setCode(String code) {
//        this.code = code;
//    }
//
//    public String getMessage() {
//        HttpServletRequest request = RequestContextHolder.getRequest();
//        if (null != request && "zh".equals(request.getHeader(BaseConst.ACCEPT_LANGUAGE))) {
//            return message;
//        } else {
//            return enMessage;
//        }
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public int getStatusCode() {
//        return statusCode;
//    }
//
//    public void setStatusCode(int statusCode) {
//        this.statusCode = statusCode;
//    }
//}
