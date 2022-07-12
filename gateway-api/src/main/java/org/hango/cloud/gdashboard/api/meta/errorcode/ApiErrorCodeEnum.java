package org.hango.cloud.gdashboard.api.meta.errorcode;

/**
 * ApiErrorCodeEnum，用于API ErrorCode的定义
 *
 * @author hanjiahao
 */
public enum ApiErrorCodeEnum {
    Success("Success", "Success", "处理成功", 200),
    Failed("Failed", "Failed", "操作失败", 400),

    InvalidFormat("InvalidFormat", "The format of the input parameter %s is illegal.", "参数 %s 的格式非法", 400),
    InvalidBodyFormat("InvalidFormat", "The format of the request body is illegal.", "请求体格式非法", 400),
    RequestExpired("RequestExpired", "Request has expired.", "请求已过期", 400),
    MissingParameter("MissingParameter", "The required input parameter %s for processing this request is not supplied.", "参数  %s 缺失", 400),
    InternalServerError("InternalServerError", "Internal server error.", "服务器内部错误", 500),
    InvalidParameterValue("InvalidParameterValue", "The parameter %s cannot accept value %s.", "参数 %s 的值 %s 非法", 400),
    InvalidConstBlank("InvalidConstBlank", "The parameterName cannot accept value %s.", "参数名称为%s时候，类型有误", 400),
    OutOfBounds("OutOfBounds", "%s is out of bounds", "%s超过限制", 400),

    RepeatValue("RepeatValue", "%s value is repeat", "%s值重复", 400),
    InvalidParamType("InvalidParamType", "ParamType is invalid", "参数类型取值不合法", 400),


    // 如下几个枚举用于异常处理
    MethodNotAllowed("MethodNotAllow", "Http method not allowed.", "http 方法不支持", 405),
    InvalidParameters("InvalidParameters", "Invalid parameters.", "参数无效", 400),
    MissingParameters("MissingParameters", "Missing parameters.", "参数缺失", 400),

    NoSuchAPI("NoSuchApi", "No such api.", "没有请求的API", 404),
    NoSuchService("NoSuchService", "No such service", "没有请求的服务", 400),
    NoSuchInterface("NoSuchApiId", "No such api id", "请求api Id不存在", 400),

    AlreadyExist("AlreadyExist", "Parameter %s you provided is already exist.", "%s名称已存在", 400),
    CanNotDelete("CanNotDelete", "This %s can't be deleted.", "此%s不能删除", 400),
    CanNotFound("ResourceNotFound", "Can't found %s", "找不到对应%s", 400),
    ParameterNull("ParameterNull", "Parameter null", "参数为空", 400),
    ParameterError("ParameterError", "Parameter %s error.", "参数%s错误", 400),

    DuplicateNameError("DuplicateNameError", "The value for %s is Duplicated", "%s设置重复", 400),
    NotMatchBounds("NotMatchBounds", "%s is not  match the bounds", "%s不符合限制", 400),

    CannotUpdate("CannotUpdate", "%s can't update", "%s 不能更新", 400),
    NoPermission("NoPermission", "You don't have permission to access the specified interface.", "对不起，您没有权限访问该接口。", 401),

    ParamNotMatch("ParamNotMatch", "Param not match", "参数不匹配", 400),
    JsonParseException("JSONParseException", "The json parse has exception %s.", "Json解析异常:%s", 400),


    //供G-Dashboard
    InterfaceNotFound("InterfaceNotFound", "We can't found the interface.", "您指定的API不存在!", 400),
    ApiAlreadyExist("ApiAlreadyExist", "This api is already exist.", "API已存在", 400),

    //webservice
    InvalidWebServiceClassName("InvalidClassName", "ClassName couldn't be null", "类名不能为空", 400),
    InvalidMethodName("InvalidMethodName", "MethodName couldn't be null", "方法名不能为空", 400),
    InvalidParamSort("InvalidParamSort", "ParamSort couldn't be null", "参数序号必须为正整数", 400),

    DubboServicePublishWayLimit("DubboServicePublishWayLimit", "dubbo service can not support custom input way", "Dubbo服务发布暂不支持手动填写服务地址方式!", 400),
    DubboServiceParamLimit("DubboServiceParamLimit", "You can't create dubbo param while the service is not a dubbo service", "非Dubbo服务无法创建Dubbo参数!", 400),


    //API相关
    AlreadyExistAliasName("AlreadyExistAliasName", "The alias name already exist", "接口标识已经存在", 400),
    CannotDeleteOnlineApi("CannotDeleteOnlineApi", "You can't delete api until offline the api", "在下线接口之前，不能删除接口", 400),
    DuplicateParamName("DuplicateParamName", "You can't create duplicate param name", "请求参数重复", 400),
    NotPublishedApi("NotPublishedApi", "The api is not published", "API没有发布到当前网关，请先发布", 400),
    NoSuchParamType("NoSuchParamType", "No such param type ", "paramTyped不存在", 400),
    NoSuchArrayDataType("NoSuchArrayDataType", "No such  array data type", "array datat ype不存在", 400),
    ErrorTimeRange("ErrorTimeRange", "The time range is error", "不可用时间区间不合法", 400),


    /**
     * 数据模型相关
     */
    NoSuchModel("NoSuchModel", "No Such Model", "此数据模型不存在", 400),

    /**
     * 导入swagger相关
     */
    FileIsEmpty("FileIsEmpty", "The file must not be empty.", "文件不能为空", 400),
    IllegalFileFormat("IllegalFileFormat", "The file format is illegal.", "文件格式非法", 400),
    ParseSwaggerFailure("ParseSwaggerFailure", "Parse swagger is failure", "解析swagger文件异常，请检查网络配置或swagger文件格式", 400),
    FileExpired("NoSuchFile", "File already expired.", "文件已过期", 404),

    ;

    private String code;
    private String enMsg;
    private String msg;
    private int statusCode;

    private ApiErrorCodeEnum(String code, String enMsg, String msg, int statusCode) {
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
