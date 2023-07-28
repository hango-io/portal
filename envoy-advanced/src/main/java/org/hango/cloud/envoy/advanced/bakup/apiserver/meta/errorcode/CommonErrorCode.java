package org.hango.cloud.envoy.advanced.bakup.apiserver.meta.errorcode;


/**
 * 该 ApiErrorCode 类仅用于网关服务使用！！！
 * <p>
 * 若 ErrorCodeEnum 枚举格式化时需要的参数是固定的，则定义一个静态变量
 * 若 ErrorCodeEnum 枚举格式化时需要的参数必须为动态传入的，则定义一个静态方法
 * <p>
 * 该类中的静态变量、静态方法的命名格式为：ErrorCodeEnum 枚举名称 + 具体参数名称
 */
public class CommonErrorCode extends ErrorCode {

    /**
     * 服务相关
     */

    public static final ErrorCode CANNOT_DELETE_ONLINE_API = new ErrorCode(ErrorCodeEnum.CANNOT_DELETE_ONLINE_API);

    private CommonErrorCode(ErrorCodeEnum errorCodeEnum, String[] args) {
        super(errorCodeEnum, args);
    }
}
