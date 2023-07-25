package org.hango.cloud.common.infra.base.errorcode;


/**
 * 该 ApiErrorCode 类仅用于网关服务使用！！！
 * <p>
 * 若 ErrorCodeEnum 枚举格式化时需要的参数是固定的，则定义一个静态变量
 * 若 ErrorCodeEnum 枚举格式化时需要的参数必须为动态传入的，则定义一个静态方法
 * <p>
 * 该类中的静态变量、静态方法的命名格式为：ErrorCodeEnum 枚举名称 + 具体参数名称
 */
public class CommonErrorCode extends ErrorCode {

    /************************************** Common Start **************************************/

    public static final ErrorCode SUCCESS = new ErrorCode(ErrorCodeEnum.SUCCESS);
    public static final ErrorCode FAILED = new ErrorCode(ErrorCodeEnum.FAILED);
    public static final ErrorCode INVALID_BODY_FORMAT = new ErrorCode(ErrorCodeEnum.INVALID_BODY_FORMAT);
    public static final ErrorCode PARAMETER_NULL = new ErrorCode(ErrorCodeEnum.PARAMETER_NULL);
    public static final ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(ErrorCodeEnum.INTERNAL_SERVER_ERROR);
    public static final ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(ErrorCodeEnum.METHOD_NOT_ALLOWED);
    public static final ErrorCode NO_SUCH_API = new ErrorCode(ErrorCodeEnum.NO_SUCH_API);

    public static final ErrorCode UN_SUPPORTED_DEFAULT_VALUE_TYPE = new ErrorCode(ErrorCodeEnum.UN_SUPPORTED_DEFAULT_VALUE_TYPE);
    public static final ErrorCode DEFAULT_VALUE_CONFIG_INVALID = new ErrorCode(ErrorCodeEnum.DEFAULT_VALUE_CONFIG_INVALID);
    public static final ErrorCode MISSING_PARAMETER_QUERY_TIME = new ErrorCode(ErrorCodeEnum.MISSING_PARAMETER, "StartTime Or EndTime");
    public static final ErrorCode QUERY_TIME_ILLEGAL = new ErrorCode(ErrorCodeEnum.QUERY_TIME_ILLEGAL);
    public static final ErrorCode READ_TIME_OUT = new ErrorCode(ErrorCodeEnum.READ_TIME_OUT);
    public static final ErrorCode FILE_IS_EMPTY = new ErrorCode(ErrorCodeEnum.FILE_IS_EMPTY);
    public static final ErrorCode ILLEGAL_FILE_FORMAT = new ErrorCode(ErrorCodeEnum.ILLEGAL_FILE_FORMAT);
    public static final ErrorCode EMPTY_PROJECT_ID = new ErrorCode(ErrorCodeEnum.EMPTY_PROJECT_ID);
    public static final ErrorCode EMPTY_TENANT_ID = new ErrorCode(ErrorCodeEnum.EMPTY_TENANT_ID);

    public static final ErrorCode INVALID_PARAM_GW_TYPE = new ErrorCode(ErrorCodeEnum.INVALID_PARAM_GW_TYPE);

    public static final ErrorCode UPDATE_FAILURE = new ErrorCode(ErrorCodeEnum.UPDATE_FAILURE);

    public static final ErrorCode MISSING_UPLOADED_FILE = new ErrorCode(ErrorCodeEnum.MISSING_UPLOADED_FILE);
    public static final ErrorCode NO_SUCH_GATEWAY = new ErrorCode(ErrorCodeEnum.NO_SUCH_GATEWAY);
    public static final ErrorCode NO_SUCH_VIRTUAL_GATEWAY = new ErrorCode(ErrorCodeEnum.NO_SUCH_VIRTUAL_GATEWAY);
    public static final ErrorCode GW_NAME_ALREADY_EXIST = new ErrorCode(ErrorCodeEnum.GW_NAME_ALREADY_EXIST);
    public static final ErrorCode GW_CLUSTER_NAME_ALREADY_EXIST = new ErrorCode(ErrorCodeEnum.GW_CLUSTER_NAME_ALREADY_EXIST);

    public static final ErrorCode ALREADY_EXIST_VIRTUAL_GW_NAME = new ErrorCode(ErrorCodeEnum.ALREADY_EXIST_VIRTUAL_GW_NAME);
    public static final ErrorCode ALREADY_EXIST_VIRTUAL_GW_CODE = new ErrorCode(ErrorCodeEnum.ALREADY_EXIST_VIRTUAL_GW_CODE);
    public static final ErrorCode ALREADY_EXIST_VIRTUAL_GW_PORT = new ErrorCode(ErrorCodeEnum.ALREADY_EXIST_VIRTUAL_GW_PORT);

    public static final ErrorCode SERVICE_NAME_ALREADY_EXIST = new ErrorCode(ErrorCodeEnum.ALREADY_EXIST, "Name");

    public static final ErrorCode NO_SUCH_SERVICE = new ErrorCode(ErrorCodeEnum.NO_SUCH_SERVICE);
    public static final ErrorCode INVALID_SERVICE_PORT = new ErrorCode(ErrorCodeEnum.INVALID_SERVICE_PORT);
    public static final ErrorCode INVALID_SERVICE_SUBSET_PORT = new ErrorCode(ErrorCodeEnum.INVALID_SERVICE_SUBSET_PORT);

    public static final ErrorCode NO_SUCH_ROUTE_RULE = new ErrorCode(ErrorCodeEnum.NO_SUCH_ROUTE_RULE);

    public static final ErrorCode NO_SUCH_DOMAIN = new ErrorCode(ErrorCodeEnum.NO_SUCH_DOMAIN);


    public static final ErrorCode EXIST_PUBLISHED_SERVICE = new ErrorCode(ErrorCodeEnum.EXIST_PUBLISHED_SERVICE);

    public static final ErrorCode SERVICE_ALREADY_PUBLISHED = new ErrorCode(ErrorCodeEnum.SERVICE_ALREADY_PUBLISHED);

    public static final ErrorCode TYPE_NOT_MATCH = new ErrorCode(ErrorCodeEnum.TYPE_NOT_MATCH);

    public static final ErrorCode EXIST_PUBLISHED_PLUGIN = new ErrorCode(ErrorCodeEnum.EXIST_PUBLISHED_PLUGIN);

    public static final ErrorCode EXIST_PUBLISHED_DOMAIN = new ErrorCode(ErrorCodeEnum.EXIST_PUBLISHED_DOMAIN);

    public static final ErrorCode CANNOT_DELETE_GATEWAY = new ErrorCode(ErrorCodeEnum.CANNOT_DELETE_GATEWAY);

    public static final ErrorCode CANNOT_DELETE_API_SERVICE = new ErrorCode(ErrorCodeEnum.CANNOT_DELETE_API_SERVICE);

    public static final ErrorCode SERVICE_NOT_PUBLISHED = new ErrorCode(ErrorCodeEnum.SERVICE_NOT_PUBLISHED);
    public static final ErrorCode SAME_SERVICE_WHEN_PUBLISH_MIRROR = new ErrorCode(ErrorCodeEnum.SAME_SERVICE_WHEN_PUBLISH_MIRROR);

    public static final ErrorCode SERVICE_TYPE_INVALID = new ErrorCode(ErrorCodeEnum.SERVICE_TYPE_INVALID);

    public static final ErrorCode SAME_NAME_ROUTE_EXIST = new ErrorCode(ErrorCodeEnum.SAME_NAME_ROUTE_EXIST);

    public static final ErrorCode NO_ROUTE_RULE_PATH = new ErrorCode(ErrorCodeEnum.NO_ROUTE_RULE_PATH);
    public static final ErrorCode ROUTE_PATH_INVALID = new ErrorCode(ErrorCodeEnum.ROUTE_PATH_INVALID);


    public static final ErrorCode SAME_PARAM_ROUTE_RULE_EXIST = new ErrorCode(ErrorCodeEnum.SAME_PARAM_ROUTE_RULE_EXIST);
    public static final ErrorCode ROUTE_SERVICE_PROTOCOL_NOT_SAME = new ErrorCode(ErrorCodeEnum.ROUTE_SERVICE_PROTOCOL_NOT_SAME);
    public static final ErrorCode ROUTE_SERVICE_SAME = new ErrorCode(ErrorCodeEnum.ROUTE_SERVICE_SAME);
    public static final ErrorCode ROUTE_SERVICE_SUBSET_SAME = new ErrorCode(ErrorCodeEnum.ROUTE_SERVICE_SUBSET_SAME);

    public static final ErrorCode ROUTE_RULE_ALREADY_PUBLISHED = new ErrorCode(ErrorCodeEnum.ROUTE_RULE_ALREADY_PUBLISHED);

    public static final ErrorCode ROUTE_RULE_NOT_PUBLISHED = new ErrorCode(ErrorCodeEnum.ROUTE_RULE_NOT_PUBLISHED);
    public static final ErrorCode COPY_ROUTE_SAME_GW = new ErrorCode(ErrorCodeEnum.COPY_ROUTE_SAME_GW);
    public static final ErrorCode COPY_ROUTE_ONLY_SUPPORT_HTTP_SERVICE = new ErrorCode(ErrorCodeEnum.COPY_ROUTE_ONLY_SUPPORT_HTTP_SERVICE);
    public static final ErrorCode GATEWAY_NOT_BINDING_SAME_HOST = new ErrorCode(ErrorCodeEnum.GATEWAY_NOT_BINDING_SAME_HOST);
    public static final ErrorCode BACKEND_SERVICE_DIFFERENT = new ErrorCode(ErrorCodeEnum.BACKEND_SERVICE_DIFFERENT);

    public static final ErrorCode INVALID_TOTAL_WEIGHT = new ErrorCode(ErrorCodeEnum.INVALID_TOTAL_WEIGHT);

    public static final ErrorCode INVALID_SUBSET_NAME = new ErrorCode(ErrorCodeEnum.INVALID_SUBSET_NAME);
    public static final ErrorCode INVALID_LABEL = new ErrorCode(ErrorCodeEnum.INVALID_LABEL);
    public static final ErrorCode NO_SUBSET_OF_SERVICE = new ErrorCode(ErrorCodeEnum.NO_SUBSET_OF_SERVICE);
    public static final ErrorCode PROJECT_NOT_ASSOCIATED_GATEWAY = new ErrorCode(ErrorCodeEnum.PROJECT_NOT_ASSOCIATED_GATEWAY);
    public static final ErrorCode GW_NOT_ASSOCIATED_DOMAIN = new ErrorCode(ErrorCodeEnum.GW_NOT_ASSOCIATED_DOMAIN);
    public static final ErrorCode CANNOT_DELETE_VIRTUAL_GATEWAY = new ErrorCode(ErrorCodeEnum.CANNOT_DELETE_VIRTUAL_GATEWAY);
    public static final ErrorCode ROUTE_HAS_TRAFFIC_MARK_RULES = new ErrorCode(ErrorCodeEnum.ROUTE_HAS_TRAFFIC_MARK_RULES);

    public static final ErrorCode DUPLICATED_SUBSET_NAME = new ErrorCode(ErrorCodeEnum.DUPLICATED_SUBSET_NAME);
    public static final ErrorCode INVALID_SUBSET_STATIC_ADDR = new ErrorCode(ErrorCodeEnum.INVALID_SUBSET_STATIC_ADDR);
    public static final ErrorCode DUPLICATED_STATIC_ADDR = new ErrorCode(ErrorCodeEnum.DUPLICATED_STATIC_ADDR);
    public static final ErrorCode INVALID_ADDR = new ErrorCode(ErrorCodeEnum.INVALID_ADDR);

    /**
     * 负载均衡相关
     */
    public static final ErrorCode INVALID_SLOW_START_WINDOW = new ErrorCode(ErrorCodeEnum.INVALID_SLOW_START_WINDOW);
    public static final ErrorCode INVALID_LOAD_BALANCE_TYPE = new ErrorCode(ErrorCodeEnum.INVALID_LOAD_BALANCE_TYPE);
    public static final ErrorCode INVALID_SIMPLE_LOAD_BALANCE_TYPE = new ErrorCode(ErrorCodeEnum.INVALID_SIMPLE_LOAD_BALANCE_TYPE);
    public static final ErrorCode INVALID_CONSISTENT_HASH_OBJECT = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_OBJECT);
    public static final ErrorCode INVALID_CONSISTENT_HASH_TYPE = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_TYPE);
    public static final ErrorCode INVALID_CONSISTENT_HASH_HTTP_COOKIE_OBJECT = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_HTTP_COOKIE_OBJECT);
    public static final ErrorCode INVALID_CONSISTENT_HASH_HTTP_COOKIE_NAME = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_HTTP_COOKIE_NAME);
    public static final ErrorCode INVALID_CONSISTENT_HASH_HTTP_COOKIE_TTL = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_HTTP_COOKIE_TTL);
    public static final ErrorCode INVALID_CONSISTENT_HASH_HTTP_HEADER_NAME = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_HTTP_HEADER_NAME);
    public static final ErrorCode INVALID_CONSISTENT_HASH_SOURCE_IP = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_SOURCE_IP);
    public static final ErrorCode INVALID_HTTP_1_MAX_PENDING_REQUESTS = new ErrorCode(ErrorCodeEnum.INVALID_HTTP_1_MAX_PENDING_REQUESTS);
    public static final ErrorCode INVALID_HTTP_2_MAX_REQUESTS = new ErrorCode(ErrorCodeEnum.INVALID_HTTP_2_MAX_REQUESTS);
    public static final ErrorCode INVALID_IDLE_TIMEOUT = new ErrorCode(ErrorCodeEnum.INVALID_IDLE_TIMEOUT);
    public static final ErrorCode INVALID_MAX_REQUESTS_PER_CONNECTION = new ErrorCode(ErrorCodeEnum.INVALID_MAX_REQUESTS_PER_CONNECTION);
    public static final ErrorCode CANNOT_UPDATE_SERVICE_NAME = new ErrorCode(ErrorCodeEnum.CANNOT_UPDATE_SERVICE_NAME);
    public static final ErrorCode INVALID_MAX_CONNECTIONS = new ErrorCode(ErrorCodeEnum.INVALID_MAX_CONNECTIONS);
    public static final ErrorCode INVALID_CONNECT_TIMEOUT = new ErrorCode(ErrorCodeEnum.INVALID_CONNECT_TIMEOUT);

    public static final ErrorCode MIRROR_BY_ROUTE_RULE = new ErrorCode(ErrorCodeEnum.MIRROR_BY_ROUTE_RULE);

    public static final ErrorCode NO_SUCH_PLUGIN = new ErrorCode(ErrorCodeEnum.NO_SUCH_PLUGIN);

    public static final ErrorCode NO_SUCH_PLUGIN_BINDING = new ErrorCode((ErrorCodeEnum.NO_SUCH_PLUGIN_BINDING));

    public static final ErrorCode EXISTS_PLUGIN_WITH_THE_SAME_NAME = new ErrorCode((ErrorCodeEnum.EXISTS_PLUGIN_WITH_THE_SAME_NAME));
    public static final ErrorCode NO_SUCH_PLUGIN_TEMPLATE = new ErrorCode(ErrorCodeEnum.NO_SUCH_PLUGIN_TEMPLATE);
    public static final ErrorCode PLUGIN_IS_BOUND = new ErrorCode(ErrorCodeEnum.PLUGIN_IS_BOUND);

    public static final ErrorCode SAME_NAME_PLUGIN_TEMPLATE_EXIST = new ErrorCode(ErrorCodeEnum.SAME_NAME_PLUGIN_TEMPLATE_EXIST);

    public static final ErrorCode CANNOT_DUPLICATE_BINDING = new ErrorCode(ErrorCodeEnum.CANNOT_DUPLICATE_BINDING);
    public static final ErrorCode CANNOT_DUPLICATE_BINDING_AUTH_PLUGIN = new ErrorCode(ErrorCodeEnum.CANNOT_DUPLICATE_BINDING_AUTH_PLUGIN);
    public static final ErrorCode PUBLISH_TYPE_NOT_SUPPORT = new ErrorCode(ErrorCodeEnum.PUBLISH_TYPE_NOT_SUPPORT);

    public static final ErrorCode ILLEGAL_PLUGIN_TYPE = new ErrorCode((ErrorCodeEnum.ILLEGAL_PLUGIN_TYPE));

    public static final ErrorCode INVALID_API_PATH = new ErrorCode(ErrorCodeEnum.INVALID_API_PATH);
    public static final ErrorCode INVALID_HTTP_STATUS_CODE = new ErrorCode(ErrorCodeEnum.INVALID_HTTP_STATUS_CODE);

    public static final ErrorCode INVALID_CONSECUTIVE_ERRORS = new ErrorCode(ErrorCodeEnum.INVALID_CONSECUTIVE_ERRORS);
    public static final ErrorCode INVALID_BASE_EJECTION_TIME = new ErrorCode(ErrorCodeEnum.INVALID_BASE_EJECTION_TIME);
    public static final ErrorCode INVALID_MAX_EJECTION_PRECENT = new ErrorCode(ErrorCodeEnum.INVALID_MAX_EJECTION_PRECENT);

    public static final ErrorCode INVALID_PATH = new ErrorCode(ErrorCodeEnum.INVALID_PATH);

    private CommonErrorCode(ErrorCodeEnum errorCodeEnum, String[] args) {
        super(errorCodeEnum, args);
    }

    public static ErrorCode timeRangeTooLarge(String days) {
        return new ErrorCode(ErrorCodeEnum.TIME_RANGE_TOO_LARGE, days);
    }

    public static ErrorCode invalidRange(String column, int min, int max) {
        return new ErrorCode(ErrorCodeEnum.INVALID_RANGE, column, String.valueOf(min), String.valueOf(max));
    }

    public static ErrorCode invalidDuration(String duration) {
        return new ErrorCode(ErrorCodeEnum.INVALID_PARAMETER_VALUE, "Duration", duration);
    }


    public static ErrorCode invalidParameterValue(Object value, String name) {
        return new ErrorCode(ErrorCodeEnum.INVALID_PARAMETER_VALUE, name, value.toString());
    }

    public static ErrorCode invalidParameterValue(Object value, String name, String message) {
        return new ErrorCode(ErrorCodeEnum.INVALID_PARAMETER_VALUE, name, String.valueOf(value), message);
    }

    public static ErrorCode ParameterInvalid(String name, String message) {
        return new ErrorCode(ErrorCodeEnum.PARAMETER_INVALID, name, message);
    }
    public static ErrorCode NotSupportedRegistryType(String serviceType, String registryCenterType) {
        return new ErrorCode(ErrorCodeEnum.NOT_SUPPORTED_REGISTRY_TYPE, serviceType, registryCenterType);
    }

    public static ErrorCode invalidParameterServiceId(Object serviceId) {
        return new ErrorCode(ErrorCodeEnum.INVALID_PARAMETER_VALUE, "ServiceId", String.valueOf(serviceId));
    }

    public static ErrorCode invalidParameter(String errMsg) {
        return new ErrorCode(ErrorCodeEnum.INVALID_PARAMETER, errMsg);
    }

    public static ErrorCode MissingParameter(String paramName) {
        return new ErrorCode(ErrorCodeEnum.MISSING_PARAMETER, paramName);
    }

    public static ErrorCode invalidParameter(String object, String name) {
        return new ErrorCode(ErrorCodeEnum.INVALID_PARAMETER_VALUE, name, object);
    }


    public static ErrorCode subsetUsedByRouteRule(String routeRuleName) {
        return new ErrorCode(ErrorCodeEnum.SUBSET_USED_BY_ROUTE_RULE, routeRuleName);
    }
}
