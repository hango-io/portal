package org.hango.cloud.common.infra.base.errorcode;


import org.hango.cloud.common.infra.base.holder.RequestContextHolder;
import org.hango.cloud.gdashboard.api.meta.errorcode.AbstractErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 新版 OpenAPI 后使用的 ApiErrorCode，该 ApiErrorCode 是对基础枚举 ErrorCodeEnum 的封装
 * 由于 ErrorCodeEnum 的 message 进行格式化时需要的参数个数不确定，为了防止少传、多传、误传参数，则使用 *ApiErrorCode 进行再一次封装
 * <p>
 * 不同的服务使用不同的 *ApiErrorCode，如：用户网关G0使用的是 CommonApiErrorCode
 *
 * @see CommonErrorCode
 */
public class ErrorCode extends AbstractErrorCode {
    private static Logger logger = LoggerFactory.getLogger(ErrorCode.class);

    public ErrorCode() {
        super();
    }

    public ErrorCode(ErrorCodeEnum errorCodeEnum, String... args) {
        try {
            this.statusCode = errorCodeEnum.getStatusCode();
            this.code = errorCodeEnum.getCode();
            this.message = String.format(errorCodeEnum.getMsg(), args);
            this.enMessage = String.format(errorCodeEnum.getEnMsg(), args);
        } catch (Exception e) {
            logger.error("ApiErrorCode 中string.format异常, 请立即检查!", e);
            this.message = errorCodeEnum.getEnMsg();
        }
    }

    public ErrorCode(int statusCode, String code, String message, String enMessage) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
        this.enMessage = enMessage;
    }

    @Override
    public String getMessage() {
        HttpServletRequest request = RequestContextHolder.getRequest();
        if (null != request && "zh".equals(request.getHeader("X-163-AcceptLanguage"))) {
            return message;
        } else {
            return enMessage;
        }
    }

    public boolean isSuccess() {
        return statusCode == 200;
    }
}