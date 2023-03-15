package org.hango.cloud.common.infra.base.errorcode;


/**
 * 新版 OpenAPI 后使用的 CommonErrorCode 枚举
 * <p>
 * 该枚举为基础枚举，不能直接用于返回，需要使用 CommonErrorCode 进行封装
 *
 * @see ErrorCodeEnum
 * <p>
 * After 2017.09.20
 */
public enum ErrorCodeEnum {
    /************************************** Common Start **************************************/
    SUCCESS("Success", "Success", "处理成功", 200),
    FAILED("Failed", "Failed", "操作失败", 400),
    UPDATE_FAILURE("UpdateFailure", "update failure", "更新失败", 400),
    UPDATE_FAILED("UpdateFailed", "update %s failed , please connect the administrator", "更新 %s 失败， 请联系管理员", 500),

    INVALID_BODY_FORMAT("InvalidFormat", "The format of the request body is illegal.", "请求体格式非法", 400),
    MISSING_PARAMETER("MissingParameter", "The required input parameter %s for processing this request is not supplied.", "参数  %s 缺失", 400),
    INTERNAL_SERVER_ERROR("InternalServerError", "Internal server error.", "服务器内部错误", 500),
    INVALID_PARAMETER_VALUE("InvalidParameterValue", "The parameter %s cannot accept value %s.", "参数 %s 的值 %s 非法", 400),
    PARAMETER_INVALID("ParameterInvalid", "The parameter %s invaild[%s]", "参数 %s 非法[%s]", 400),
    FILE_IS_EMPTY("FileIsEmpty", "The file must not be empty.", "文件不能为空", 400),
    ILLEGAL_FILE_FORMAT("IllegalFileFormat", "The file format is illegal.", "文件格式非法", 400),
    // 如下几个枚举用于异常处理
    METHOD_NOT_ALLOWED("MethodNotAllow", "Http method not allowed.", "http 方法不支持", 405),

    NO_SUCH_API("NoSuchApi", "No such api.", "没有请求的API", 404),
    PARAMETER_NULL("ParameterNull", "Parameter null", "参数为空", 400),

    JSON_PARSE_EXCEPTION("JSONParseException", "The json parse has exception %s.", "Json解析异常:%s", 400),
    TIME_RANGE_TOO_LARGE("TimeRangeTooLarge", "Does not support interval queries greater than %s days.", "暂不支持大于%s天的区间查询.", 400),
    QUERY_TIME_ILLEGAL("QueryTimeIllegal", "Start Time must before End TIme.", "起止时间必须小于截至时间.", 400),

    READ_TIME_OUT("ReadTimeOut", "read time out.", "查询超时", 400),

    MISSING_UPLOADED_FILE("WrongFile", "Please select a correct file.", "请选择一个文件,且文件大小不能超过10M", 400),
    ALREADY_EXIST("AlreadyExist", "Parameter %s you provided is already exist.", "已存在同名%s，不允许创建", 400),

    EMPTY_PROJECT_ID("EmptyProjectId", "The projectId is empty.", "指定的项目id为空!", 400),

    EMPTY_TENANT_ID("EmptyTenantId", "The tenantId is empty.", "指定的租户id为空!", 400),

    SORT_KEY_INVALID("SortKeyInvalid", "The sort key is invalid", "搜索查询查询搜索项不正确", 400),

    SORT_VALUE_INVALID("SortValueInvalid", "The sort value is invalid", "搜索查询查询值不正确", 400),

    UN_SUPPORTED_DEFAULT_VALUE_TYPE("UnSupportedDefaultValueType", "default value type not support", "默认值类型不支持", 400),
    DEFAULT_VALUE_CONFIG_INVALID("DefaultValueConfigInvalid", "default value config invalid", "默认值配置错误", 400),


    /************************************** Common End **************************************/

    /************************************** Gateway Start **************************************/

    GW_NAME_ALREADY_EXIST("AlreadyExistGwName", "GwName already exist.", "网关名称已经存在", 400),
    ALREADY_EXIST_VIRTUAL_GW_NAME("AlreadyExistVirtualGwName", "VirtualGwName already exist.", "虚拟网关名称已经存在", 400),
    ALREADY_EXIST_VIRTUAL_GW_CODE("AlreadyExistVirtualGwCode", "Code already exist.", "虚拟网关标识已经存在", 400),
    ALREADY_EXIST_VIRTUAL_GW_PORT("AlreadyExistVirtualGwPort", "Port already exist.", "虚拟网关端口已经存在", 400),
    GW_CLUSTER_NAME_ALREADY_EXIST("AlreadyExistGwName", "Gateway Cluster Name already exist.", "网关集群名称已经存在", 400),
    CANNOT_DELETE_GATEWAY("CannotDeleteGateway", "Cannot delete gateway,please offline service or api", "网关存在已发布信息，无法删除",
            400),
    NO_SUCH_GATEWAY("NoSuchGateway", "No such gateway", "指定的网关不存在", 400),

    NO_SUCH_VIRTUAL_GATEWAY("NoSuchVirtualGateway", "No such virtual gateway", "指定的虚拟网关不存在", 400),
    CANNOT_DELETE_VIRTUAL_GATEWAY("CannotDeleteVirtualGateway", "Cannot delete virtual gateway , please delete project binding before", "虚拟网关无法删除，请先删除该网关关联的项目", 400),

    INVALID_PARAM_GW_TYPE("InvalidParamGwType", "Wrong Gateway Type.", "网关类型填写错误", 400),
    PROJECT_NOT_ASSOCIATED_GATEWAY("ProjectNotAssociatedGateway", "The current project is not associated with the specified gateway.", "当前项目未关联指定网关，不允许发布", 400),

    MIRROR_BY_ROUTE_RULE("MirrorByRouteRule", "The service has been mirrored by route rule", "该服务已被路由流量镜像使用", 400),

    /************************************** Gateway end **************************************/

    /************************************** Service Start **************************************/

    NO_SUCH_SERVICE("NoSuchService", "No such service", "没有请求的服务", 400),
    CANNOT_MODIFY_SERVICE("CannotUpdateService", "You can't update service until offline the service", "在下线服务之前，不能修改服务的基本信息", 400),
    CANNOT_DELETE_ONLINE_SERVICE("CannotDeleteOnlineService", "You can't delete service until offline the service", "在下线服务之前，不能删除服务", 400),
    CANNOT_DELETE_API_SERVICE("CannotDeleteApiService", "You can't delete service until delete all apis", "在删除api前，不能删除服务", 400),
    SERVICE_NOT_PUBLISHED("ServiceNotPublished", "We can't found the service publish information.", "您指定的Service未发布!", 400),
    SERVICE_TYPE_INVALID("ServiceTypeInvalid", "Service type is invalid", "服务类型不合法", 400),

    SAME_NAME_ROUTE_RULE_EXIST("SameNameRouteRuleExist", "The route rule with the same name already exists and cannot be "
            + "created.", "当前项目已存在同名路由，项目下不允许创建同名路由", 400),

    SERVICE_ALREADY_PUBLISHED("ServiceAlreadyPublished", "The service has already published", "服务已经发布到当前网关", 400),

    EXIST_PUBLISHED_SERVICE("ExistPublishedService", "exist published service, do not allow to delete ", "当前项目存在已发布服务，不允许删除", 400),
    EXIST_PUBLISHED_PLUGIN("ExistPublishedPlugin", "exist published plugin, do not allow to delete ", "当前项目存在已发布插件，不允许删除", 400),

    PUBLISH_TYPE_NOT_SUPPORT("PublishTypeNotSupport", "The publish type of service can not support this operation", "该服务的发布方式并不支持本操作", 400),

    BACKEND_SERVICE_DIFFERENT("BackendServiceDifferent", "Backend services are different.", "发布服务，指定后端服务不同不允许创建", 400),

    DUPLICATED_SUBSET_NAME("DuplicatedSubsetName", "SubsetName is duplicated", "版本名称不能重复", 400),

    INVALID_SUBSET_NAME("InvalidSubsetName", "Subset does not exist", "版本名称不存在", 400),
    INVALID_SUBSET_STATIC_ADDR("InvalidSubsetStaticAddr", "Subset's address is invalid", "版本中的地址不合法，不能为空且需要包含在服务发布地址列表中", 400),
    DUPLICATED_SUBSET_STATIC_ADDR("DuplicatedSubsetStaticAddr", "Subset's addresses are duplicated", "版本中的地址不合法，存在重复的地址", 400),
    DUPLICATED_STATIC_ADDR("DuplicatedStaticAddr", "Static address only belongs to one subset", "一个地址仅能属于一个版本", 400),

    /************************************** Service End **************************************/
    /************************************** Route Rule Start **************************************/
    NO_SUCH_ROUTE_RULE("NoSuchRouteRule", "No such route rule", "指定的路由规则不存在", 400),
    NO_ROUTE_RULE_PATH("NoRouteRulePath", "No route rule path", "创建路由，path不能为空", 400),
    NO_SUCH_DOMAIN("NoSuchDomain", "No such route domain", "域名不存在", 400),
    ROUTE_RULE_CONTAINS_NGINX_CAPTURE("RouteRuleContainsNginxCapture", "Route rule contains nginx capture regex", "创建路由，path正则中不能包含nginx捕获正则", 400),
    ROUTE_RULE_METHOD_INVALID("RouteRuleMethodInvalid", "The route rule method is invalid", "路由规则指定的method不合法", 400),
    SAME_PARAM_ROUTE_RULE_EXIST("SameParamRouteRuleExist", "The same param of route rule already exists and cannot be created.", "相同参数的路由规则已存在，不允许重复创建!", 400),
    ROUTE_RULE_ALREADY_PUBLISHED("RouteRuleAlreadyPublished", "The route rule has already published", "路由规则已经发布至该网关", 400),
    NOT_MODIFY_PRIORITY("NotModifyPriority", "Not modify priority", "未修改优先级，不允许复制", 400),
    NOT_MODIFY_ROUTE_RULE_NAME("NotModifyRouteRuleName", "Not modify routeRuleName", "未修改路由规则名称，不允许复制", 400),
    BATCH_PUBLISH_ROUTE_ERROR("BatchPublishRouteError", "Batch publish route error, the error gw is: %s.", "批量发布路由至多网关失败，失败网关: %s", 500),

    ROUTE_RULE_NOT_PUBLISHED("RouteRuleNotPublished", "The route rule not published", "路由规则未发布", 400),
    INVALID_DESTINATION_SERVICE("InvalidDestinationService", "Destination service is invalid", "路由规则发布时指定的后端服务不能为空", 400),
    ROUTE_RULE_ALREADY_PUBLISHED_TO_GW("RouteRuleAlreadyPublishedToGw", "The route rule has already published to gw, gw is: %s.", "路由规则已发布至网关: %s", 400),
    INVALID_TOTAL_WEIGHT("InvalidTotalWeight", "Total weight was invalid.", "权重之和必须为100", 400),

    SUBSET_USED_BY_ROUTE_RULE("SubsetUsedByRouteRule", "Subset was used by route rule.", "不能删除已被路由规则：%s，引用的版本", 400),

    ROUTE_HAS_TRAFFIC_MARK_RULES("RouteHasTrafficMarkRules", "The route rule has traffic mark rules", "该路由存在流量染色规则，请先删除流量染色规则再下线路由", 400),
    ROUTE_RULE_SERVICE_NOT_MATCH("RouteRuleServiceNotMatch", "The route rule not match the service", "服务和路由规则不匹配", 400),

    /************************************** Route Rule End **************************************/
    /************************************** API Start **************************************/

    /************************************** API End **************************************/
    /************************************** Load Balance Start **************************************/
    //负载均衡相关
    INVALID_SLOW_START_WINDOW("InvalidSlowStartWindow", "The service warm-up time can be configured in the range [1s-3600s] only", "服务预热时间窗仅支持配置[1s-3600s]区间", 400),
    INVALID_LOAD_BALANCE_TYPE("InvalidLoadBanlanceType", "This load balance type is invalid", "服务负载均衡类型取值为Simple或ConsistentHash", 400),
    INVALID_SIMPLE_LOAD_BALANCE_TYPE("InvalidSimpleLoadBanlanceType", "This simple load balance type is invalid", "Simple类型的负载均衡规则，仅包含ROUND_ROUBIN、LEAST_CONN、RANDOM", 400),
    INVALID_CONSISTENT_HASH_OBJECT("InvalidConsistentHashObject", "Consistent hash object is invalid", "一致性哈希对象格式非法", 400),
    INVALID_CONSISTENT_HASH_TYPE("InvalidConsistentHashType", "Consistent hash type is invalid", "一致性哈希对象类型为HttpHeaderName、HttpCookie、UseSourceIp三者之一", 400),
    INVALID_CONSISTENT_HASH_HTTP_COOKIE_OBJECT("InvalidConsistentHashHttpCookieObject", "Http cookie is invalid", "一致性哈希对象使用cookie时，cookie对象不能为空", 400),
    INVALID_CONSISTENT_HASH_HTTP_COOKIE_NAME("InvalidConsistentHashHttpCookieName", "Http cookie name is invalid", "一致性哈希对象使用cookie时，cookie名称不能为空", 400),
    INVALID_CONSISTENT_HASH_HTTP_COOKIE_TTL("InvalidConsistentHashHttpCookieName", "Http cookie ttl is invalid", "一致性哈希对象使用cookie时，cookie ttl不能小于0", 400),
    INVALID_CONSISTENT_HASH_HTTP_HEADER_NAME("InvalidConsistentHashHttpHeaderName", "Http header name is invalid", "一致性哈希对象使用HttpHeaderName时，HttpHeaderName不能为空", 400),
    INVALID_CONSISTENT_HASH_SOURCE_IP("InvalidConsistentHashSourceIP", "Source ip is invalid", "一致性哈希对象使用源IP时，源IP不能为空", 400),

    INVALID_HTTP_1_MAX_PENDING_REQUESTS("InvalidHttp1MaxPendingRequests", "http1MaxPendingRequests is invalid", "http1MaxPendingRequests不能小于0", 400),
    INVALID_HTTP_2_MAX_REQUESTS("InvalidHttp2MaxRequests", "Http2MaxRequests is invalid", "http2MaxRequests不能小于0", 400),
    INVALID_IDLE_TIMEOUT("InvalidIdleTimeout", "IdleTimeout is invalid", "idleTimeout不能小于0", 400),
    INVALID_MAX_REQUESTS_PER_CONNECTION("InvalidMaxRequestsPerConnection", "MaxRequestsPerConnection is invalid", "maxRequestsPerConnection不能小于0", 400),
    INVALID_MAX_CONNECTIONS("InvalidmaxConnections", "MaxConnections is invalid", "maxConnections不能小于0", 400),
    INVALID_CONNECT_TIMEOUT("InvalidConnectTimeout", "ConnectTimeout is invalid", "connectTimeout不能小于0", 400),

    /************************************** Load Balance End **************************************/

    /************************************** Plugin Start **************************************/
    NO_SUCH_PLUGIN("NoSuchPlugin", "No such plugin.", "指定的插件不存在", 400),
    NO_SUCH_PLUGIN_BINDING("NoSuchPluginBinding", "No such plugin binding info", "指定的插件绑定关系不存在", 400),
    NO_SUCH_PLUGIN_TEMPLATE("NoSuchPluginTemplate", "No such plugin template", "指定的插件模板不存在", 400),
    SAME_NAME_PLUGIN_TEMPLATE_EXIST("SameNamePluginTemplateExist", "The plugin template with the same name already exists and cannot be created.", "同名插件模板已存在，不允许重复创建!", 400),

    CANNOT_DUPLICATE_BINDING("CannotDuplicateBinding", "The plugin binding already exists and duplicate binding are not allowed", "插件绑定关系已存在，不允许重复绑定同一插件", 400),
    CANNOT_DUPLICATE_BINDING_AUTH_PLUGIN("CannotDuplicateBindingAuthPlugin", "The auth type plugin binding already exists",
            "认证类型插件绑定关系已存在，不允许重复绑定", 400),
    ILLEGAL_PLUGIN_TYPE("IllegalPluginType", "The plugin type is illegal", "指定的插件类型不匹配", 400),

    INVALID_PARAMETER("InvalidParameter", "invaild parameter", "%s", 400),

    //健康检查相关
    INVALID_API_PATH("InvalidApiPath", "Api path is invalid", "接口路径不合法", 400),
    INVALID_HTTP_STATUS_CODE("InvalidHttpStatusCode", "Http status code is invalid", "健康状态码不合法", 400),
    INVALID_CONSECUTIVE_ERRORS("InvalidConsecutiveErrors", "Consecutive errors parameter is invalid", "连续失败次数不合法", 400),

    INVALID_BASE_EJECTION_TIME("InvalidBaseEjectionTime", "Base ejection time is invalid", "驱逐时间不合法", 400),
    INVALID_MAX_EJECTION_PRECENT("InvalidMaxEjectionPercent", "Max ejection percent is invalid", "最多可驱逐的实例比不合法", 400),

    /************************************** Plugin End **************************************/
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
