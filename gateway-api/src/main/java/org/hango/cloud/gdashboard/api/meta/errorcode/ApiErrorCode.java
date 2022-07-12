package org.hango.cloud.gdashboard.api.meta.errorcode;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 新版 OpenAPI 后使用的 ApiErrorCode，该 ApiErrorCode 是对基础枚举 ErrorCodeEnum 的封装
 * 由于 ErrorCodeEnum 的 message 进行格式化时需要的参数个数不确定，为了防止少传、多传、误传参数，则使用 *ApiErrorCode 进行再一次封装
 * <p>
 * 不同的服务使用不同的 *ApiErrorCode，如：用户网关G0使用的是 CommonApiErrorCode
 *
 * @see CommonApiErrorCode
 */
public class ApiErrorCode extends AbstractErrorCode {
    private static Logger logger = LoggerFactory.getLogger(ApiErrorCode.class);

    public ApiErrorCode(ApiErrorCodeEnum errorCodeEnum, String... args) {
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
}