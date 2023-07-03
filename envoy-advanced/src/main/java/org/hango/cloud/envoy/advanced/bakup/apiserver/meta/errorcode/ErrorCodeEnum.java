package org.hango.cloud.envoy.advanced.bakup.apiserver.meta.errorcode;


/**
 * 新版 OpenAPI 后使用的 ApiErrorCode 枚举
 * <p>
 * 该枚举为基础枚举，不能直接用于返回，需要使用 ApiErrorCode 进行封装
 *
 * @see ErrorCodeEnum
 * <p>
 * After 2017.09.20
 */
public enum ErrorCodeEnum {
    SUCCESS("Success", "Success", "处理成功", 200),
    INVALID_PARAMETER_VALUE("InvalidParameterValue", "The parameter %s cannot accept value %s.", "参数 %s 的值 %s 非法", 400),
    API_ALREADY_EXIST("ApiAlreadyExist", "This api is already exist.", "API已存在", 400),
    EMPTY_SERVICE_TAG("EmptyServiceTag", "The serviceTag couldn't be null", "服务标识不能为空", 400),
    //服务相关

    CANNOT_DOWNLOAD_SERVICE_SDK("CannotDownloadServiceSDK", "You can't download sdk cause of no APIs", "服务下不存在API，不允许下载SDK", 400),
    CANNOT_DELETE_ONLINE_API("CannotDeleteOnlineApi", "You can't delete api until offline the api", "在下线接口之前，不能删除接口", 400),
    BACKEND_SERVICE_DIFFERENT("BackendServiceDifferent", "Backend services are different.", "发布服务，指定后端服务不同不允许创建", 400),
    ;

    private String code;
    private String enMsg;
    private String msg;
    private int statusCode;

    private ErrorCodeEnum(String code, String enMsg, String msg, int statusCode) {
        this.code = code;
        this.enMsg = enMsg;
        this.msg = msg;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getEnMsg() {
        return enMsg;
    }

    public void setEnMsg(String enMsg) {
        this.enMsg = enMsg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
