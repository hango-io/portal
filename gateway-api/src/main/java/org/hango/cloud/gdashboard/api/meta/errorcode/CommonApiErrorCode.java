package org.hango.cloud.gdashboard.api.meta.errorcode;

public class CommonApiErrorCode extends ApiErrorCode {

    public static ApiErrorCode Success = new ApiErrorCode(ApiErrorCodeEnum.Success);
    public static ApiErrorCode Failed = new ApiErrorCode(ApiErrorCodeEnum.Failed);
    //FixMe wdc添加
    public static ApiErrorCode ParameterNull = new ApiErrorCode(ApiErrorCodeEnum.ParameterNull);
    public static ApiErrorCode InternalServerError = new ApiErrorCode(ApiErrorCodeEnum.InternalServerError);
    /**
     * API 数据模型相关
     */
    public static ApiErrorCode NoSuchApiInterface = new ApiErrorCode(ApiErrorCodeEnum.NoSuchInterface);
    public static ApiErrorCode NoSuchModel = new ApiErrorCode(ApiErrorCodeEnum.NoSuchModel);
    /**
     * api基本信息，包括requestHeader，response header等
     */
    public static ApiErrorCode RepeatedParamName = new ApiErrorCode(ApiErrorCodeEnum.DuplicateParamName);
    public static ApiErrorCode NoSuchParamType = new ApiErrorCode(ApiErrorCodeEnum.NoSuchParamType);
    public static ApiErrorCode NoSuchArrayDataType = new ApiErrorCode(ApiErrorCodeEnum.NoSuchArrayDataType);
    public static ApiErrorCode ModelNameAlreadyExist = new ApiErrorCode(ApiErrorCodeEnum.AlreadyExist, "ModelName");
    public static ApiErrorCode InvalidParamType = new ApiErrorCode(ApiErrorCodeEnum.InvalidParamType);
    /**
     * 导入swagger相关
     */
    public static ApiErrorCode FileIsEmpty = new ApiErrorCode(ApiErrorCodeEnum.FileIsEmpty);
    public static ApiErrorCode IllegalFileFormat = new ApiErrorCode(ApiErrorCodeEnum.IllegalFileFormat);
    public static ApiErrorCode ParseSwaggerFailure = new ApiErrorCode(ApiErrorCodeEnum.ParseSwaggerFailure);
    public static ApiErrorCode FileExpire = new ApiErrorCode(ApiErrorCodeEnum.FileExpired);
    public static ApiErrorCode ApiAlreadyExist = new ApiErrorCode(ApiErrorCodeEnum.ApiAlreadyExist);
    public static ApiErrorCode AlreadyExistAliasName = new ApiErrorCode(ApiErrorCodeEnum.AlreadyExistAliasName);
    public static ApiErrorCode DubboServiceParamLimit = new ApiErrorCode(ApiErrorCodeEnum.DubboServiceParamLimit);

    public CommonApiErrorCode(ApiErrorCodeEnum errorCodeEnum, String[] args) {
        super(errorCodeEnum, args);
    }

    public static ApiErrorCode InvalidParameterValueModelName(String modelName) {
        return new ApiErrorCode(ApiErrorCodeEnum.InvalidParameterValue, "ModelName", modelName);
    }

    public static ApiErrorCode InvalidParameterValue(Object value, String name) {
        return new ApiErrorCode(ApiErrorCodeEnum.InvalidParameterValue, name, value.toString());
    }

    public static ApiErrorCode InvalidConstBlank(String name) {
        return new ApiErrorCode(ApiErrorCodeEnum.InvalidConstBlank, name);
    }

    public static ApiErrorCode InvalidParameterValue(Object value, String name, String message) {
        return new ApiErrorCode(ApiErrorCodeEnum.InvalidParameterValue, name, String.valueOf(value), message);
    }

    public static ApiErrorCode MissingParameter(String paramName) {
        return new ApiErrorCode(ApiErrorCodeEnum.MissingParameter, paramName);
    }

    public static ApiErrorCode InvalidParameter(String object, String name) {
        return new ApiErrorCode(ApiErrorCodeEnum.InvalidParameterValue, name, object);
    }

}
