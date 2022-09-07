package org.hango.cloud.dashboard.apiserver.util;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * global constants.
 *
 * @author Feng Changjian (hzfengchj)
 * @version $Id: Const.java, v 1.0 2013-8-2 下午03:59:33
 */
public class Const {

    // 当不确定HashMap的初始值大小多少合适值，使用该值
    public static final int DEFAULT_MAP_SIZE = 16;

    public static final String DEFAULT_ENCODING = "utf-8";

    public static final String DEFAULT_CONTENT_TYPE = "application/json";

    public static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    public static final String GBK_ENCODING = "gbk";

    public static final String UNKNOWN_STRING = "unknown";

    public static final String GATEWAY_PATH = "/ngw";

    // 默认分页查询时limit的最大值
    public static final long DEFAULT_MAX_LIMIT = 1000;

    public static final String X_163_LANGUAGE = "x-163-language";
    public static final String X_163_LANGUAGE_VALUE = "zh";

    /**
     * Some time constants
     */
    public static final long MS_OF_MINUTE = 60 * 1000;

    public static final long MS_OF_HOUR = 60 * 60 * 1000;

    public static final long MS_OF_DAY = 24 * MS_OF_HOUR;

    public static final long HOUR_OF_DAY = 24;


    /**
     * Http Method Type
     */
    public static final String GET_METHOD = "GET";

    public static final String POST_METHOD = "POST";

    public static final String PUT_METHOD = "PUT";

    public static final String HEAD_METHOD = "HEAD";

    public static final String DELETE_METHOD = "DELETE";

    public static final String OPTIONS_METHOD = "OPTIONS";

    public static final String SP = "##";

    public static final int OK = 200;


    /**
     * 正则相关
     */
    /**
     * 网关服务管理
     */

    /**
     * 服务名称：必填，支持中文、数字、英文大小写、中划线、下划线，最大长度32字符
     */
    public static final String REGEX_SERVICE_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\.]{1,32}";
    /**
     * 服务标识，必填，支持英文小写，数字，最长64字符
     */
    public static final String REGEX_SERVICE_TAG = "^[a-z_\\-\\/A-Z0-9]{1,63}";
//    public static final String REGEX_SERVICE_TAG = "^[\\s\\S]{1,63}";
    /**
     * 备注信息，选填，支持全文本，最长64字符
     */
    public static final String REGEX_DESCRIPTION = "^[\\s\\S]{0,200}";

    public static final String REGEX_HEALTH_INTERFACE = "(/\\S{0,200}){0,1}";

    /**
     * 路由名称：必填，支持中文、数字、英文大小写、中划线、下划线，最大长度200字符
     */
    public static final String REGEX_ROUTE_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\.\\/]{1,200}";

    /**
     * 网关管理
     */
    /**
     * 网关名称：必填，支持中文，数字，英文大小写，中划线，下划线，最大长度32字符
     */
    public static final String REGEX_GATEWAY_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-]{1,32}";
    /**
     * 网关地址，必填，必须与网关实际配置一样；最大长度64字符
     */
    public static final String REGEX_GATEWAY_URL = "(https?|http)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
    /**
     * 健康检查接口，必填，必须以/开头，支持英文大小写，数字，url规范，最大长度64字符
     */
    public static final String REGEX_HEALTH = "^[/][\\S]{1,64}";

    /**
     * wsdl url校验
     */
    public static final String REGEX_WSDL_URL = "(https?|http)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]\\?wsdl";

    //判断ServiceAddr是否是合法的地址
    //网关相关regex
    public static final String REGEX_URL = "[(https?|http|zookeeper)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]]{1,200}";
    public static final String REGEX_GW_NAME = "\\S{1,32}";
    public static final String REGEX_DES = "\\S{1,255}";
    public static final String REGEX_ORDINARY = "\\S{1,64}";

    //服务相关
    //服务名称
//    public static final String REGEX_SERVICE_NAME = "\\S{1,32}";
    //服务标识
//    public static final String REGEX_SERVICE_ENVID = "[A-Za-z0-9]{1,32}";
    //服务发布类型
    public static final String REGEX_SERVICE_ROUTE_TYPE = "RegistryCenter|CustomInput";
    //服务类型
    public static final String REGEX_SERVICE_TYPE = "http|dubbo|webservice|grpc";

    //api相关
//    public static final String REGEX_ALIAS_NAME = "^[a-z]{2,32}|^(\\s)";

    /**
     * api method
     */
    public static final String REGEX_API_METHOD = "GET|POST|DELETE|PUT|HEAD";

    public static final String SLASH = "/";
    //api type
    public static final String REGEX_API_TYPE = "RESTFUL";
    //api description
    public static final String REGEX_API_DESCRIPTION = "\\S{1,255}|(\\s)";


    //熔断相关regex
    public static final String HYSTRIX_TYPE = "group|command|class";

    //公共参数
    public static final String ACTION = "Action";
    public static final String VERSION = "Version";

    public static final String SERVICE_MODULE = "api_gateway";

    public static final String DUBBO = "dubbo";
    public static final String WEBSERVICE = "webservice";

    /**
     * API测试用例 校验规则
     */
    public static final String JSON_MATCH = "json_match";

    public static final String CONTAINS = "contains";

    public static final String STATUS_CODE = "status_code";

    /**
     * API测试用例 校验规则 所在的位置body 或 header
     */
    public static final String POSITION_BODY = "body";

    public static final String POSITION_HEADER = "header";

    public static final String POSITION_COOKIE = "cookie";

    public static final String POSITION_DUBBO_PARAM = "dubbo_param";

    public static final String REQUEST_EXAMPLE = "REQUEST_EXAMPLE";
    public static final String RESPONSE_EXAMPLE = "RESPONSE_EXAMPLE";

    //记录操作日志时，区分对象类型
    public static final String API = "api";
    public static final String ROUTE = "route";

    public static final String SERVICE = "service";

    public static final String MODEL = "model";

    //服务名称正则限制
    public static final String CLASS_NAME = "[A-Za-z0-9]+$";
    public static final String PHONE_REGEX = "1\\d{10}$";

    //http method集合
    public static final List<String> METHOD_LIST = Arrays.asList(GET_METHOD, POST_METHOD, PUT_METHOD, DELETE_METHOD);

    //分流类型：权重分流和参数分流
    public static final String WEIGHT_SHUNT = "weight";

    public static final String PARAMETER_SHUNT = "parameter";

    //参数分流类型：名单分流和取模阈值分流
    public static final String LIST_SHUNT = "listShunt";

    public static final String MODULUS_SHUNT = "modulusShunt";

    public static final String ENV_ID_PATTERN = "[a-z][\\da-z_\\-\\.]{0,30}[\\da-z]";


    public static final String AUDIT_DATASOURCE_MYSQL = "mysql";
    public static final String AUDIT_DATASOURCE_MONGO = "mongo";
    public static final String AUDIT_DATASOURCE_ELASTICSEARCH = "elasticsearch";
    public static final String AUDIT_DATASOURCE_SWITCH = "AuditDatasourceSwitch";
    public static final String METRIC_URL = "MetricUrl";
    public static final int SLOW_RESP_TIME = 3000;
    public static final String ERROR_TYPE_4XX = "badrequest";
    public static final String ERROR_TYPE_5XX = "servererror";
    public static final String ERROR_TYPE_504 = "networkerror";
    public static final String ERROR_SLOW_RESP = "slowresp";
    public static final String SERVICE_NAME = "serviceName";
    public static final String RESP_CODE = "respCode";
    public static final String COUNT = "count";
    public static final String DURATION = "duration";

    public static final String DURATION_95 = "DURATION_95";
    public static final String DURATION_99 = "DURATION_99";
    public static final String DURATION_AVG = "DURATION_AVG";
    public static final String MAX_DURATION = "MAX_DURATION";
    public static final String TOTAL_DURATION = "TOTAL_DURATION";
    public static final String SUCCESS_COUNT = "SUCCESS_COUNT";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String NETWORK_FAILED_COUNT = "NETWORK_FAILED_COUNT";
    public static final String FAILED_RATE = "FAILED_RATE";
    public static final String ERROR_REQUEST = "ERROR_REQUEST";
    public static final String TOTAL_COUNT = "TOTAL_COUNT";
    public static final String QPS = "QPS";
    public static final String HYSTRIX = "HYSTRIX";
    public static final String RANK_TOTAL_COUNT = "RANK_TOTAL_COUNT";
    public static final String RANK_FAILED_COUNT = "RANK_FAILED_COUNT";
    public static final String RANK_BAD_REQUEST_COUNT = "RANK_BAD_REQUEST_COUNT";


    //gportal提供的openapi接口
    public static final String SWAGGER_URL = "/api/swagger";

    public static final String HTTP_PREFIX = "http://";

    public static final String HTTPS_PREFIX = "https://";

    //可以直接放行的接口
    //FIXME 删除不要的uri
    public static final List<String> NO_USERPERMISSION = Arrays.asList("/api/auth/login",
            "/api/auth/oidc", "/api/restfulSdk", "/api/swagger", "/healthcheck");

    public static final String G_DASHBOARD_PREFIX = "/gdashboard";

    public static final String ENVOY_GATEWAY_PREFIX = G_DASHBOARD_PREFIX + "/envoy";

    public static final String INTEGRATION_PREFIX = G_DASHBOARD_PREFIX + "/camel";

    public static final String GATEWAY_TYPE_PATTERN = "envoy|Spring Cloud Gateway";


    /**
     * 参数类型，分为REQUEST和RESPONSE和QUERYSTRING
     */
    public static final String REQUEST_PARAM_TYPE = "REQUEST";

    public static final String RESPONSE_PARAM_TYPE = "RESPONSE";

    public static final String QUERYSTRING_PARAM_TYPE = "QUERYSTRING";


    /**
     * 同步数据
     */
    public static final String API_ID = "apiId";
    public static final String OBJECT_ID = "objectId";

    public static final String PROJECT_DOMAIN = "project";
    public static final String SYSTEM_DOMAIN = "system";

    public static final String GATEWAY_HYSTRIX_TYPE = "class";
    public static final String CLASS_HYSTRIX_TYPE = "group";
    public static final String API_HYSTRIX_TYPE = "api";
    public static final String ENV_ID = "envId";

    /**
     * 审计相关
     */
    public static final String AUDIT_RESOURCE_TYPE_API = "API";
    public static final String AUDIT_RESOURCE_TYPE_API_REQUEST_BODY = "API 请求体";
    public static final String AUDIT_RESOURCE_TYPE_API_RESPONSE_BODY = "API 响应体";
    public static final String AUDIT_RESOURCE_TYPE_API_QUERY_STRING = "API Query String";
    public static final String AUDIT_RESOURCE_TYPE_API_STATUS_CODE = "API 响应状态码";
    public static final String AUDIT_RESOURCE_TYPE_API_EXAMPLE = "API 示例";
    public static final String AUDIT_RESOURCE_TYPE_API_REQUEST_HEADER = "API 请求头";
    public static final String AUDIT_RESOURCE_TYPE_API_RESPONSE_HEADER = "API 响应头";
    public static final String AUDIT_RESOURCE_TYPE_API_MODEL = "API 模型";
    public static final String AUDIT_RESOURCE_TYPE_GATEWAY = "网关";
    public static final String AUDIT_RESOURCE_TYPE_HYSTRIX = "熔断策略";
    public static final String AUDIT_RESOURCE_TYPE_PLUGIN = "插件";
    public static final String AUDIT_RESOURCE_TYPE_PLUGIN_TEMPLATE = "插件模板";
    public static final String AUDIT_RESOURCE_TYPE_BLACK_LIST = "黑白名单";
    public static final String AUDIT_RESOURCE_TYPE_SERVICE = "服务";
    public static final String AUDIT_RESOURCE_TYPE_LB = "分流策略";
    public static final String AUDIT_RESOURCE_TYPE_FLOW_RE = "流量复制策略";
    public static final String AUDIT_RESOURCE_TYPE_TRAFFIC_CONTROL = "流控策略";
    public static final String AUDIT_RESOURCE_TYPE_ALERT = "告警策略";
    public static final String AUDIT_RESOURCE_TYPE_TRAFFIC_MARK = "流量染色";

    /**
     * Envoy审计相关
     */
    public static final String AUDIT_RESOURCE_TYPE_ROUTE_RULE = "路由规则";
    public static final String AUDIT_RESOURCE_TYPE_HEALTH_CHECK = "健康检查规则";
    public static final String AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN = "插件";
    public static final String AUDIT_RESOURCE_TYPE_ENVOY_SERVICE = "服务";

    /**
     * 审计查询相关
     */
    public static final String AUDIT_RESP_CODE_4XX = "4XX";
    public static final String AUDIT_RESP_CODE_5XX = "5XX";
    public static final String AUDIT_RESP_CODE_ALL = "ALL";
    public static final String AUDIT_SCROLL_ID = "ScrollId";
    public static final String ATTRIBUTE_FOR_AUDIT = "ATTRIBUTE_FOR_AUDIT";
    public static final String AUDIT_QUERY_TIMEOUT = "AUDIT_QUERY_TIMEOUT";
    public static final String AUDIT_HOST_UNREACHABLE = "AUDIT_ELASTICSEARCH_HOST_UNREACHABLE";
    public static final String AUDIT_SCROLL_TIMEOUT = "search_phase_execution_exception";
    public static final Integer QUERY_MAX_DAY = 7;

    /**
     * 分流类型，分为参数分流和权重分流
     */
    public static final String SHUNT_WAY = "parameter|weight";

    /**
     * NSF-COLOR
     */
    public static final String NSF_COLOR_REGEX = "^#(?:[0-9a-fA-F]{3}){1,2}$";

    /**
     * 匹配方式，分为名单分流和取模阈值
     */
    public static final String SHUNT_PARAM_MATCHING_MODE = "modulusShunt|listShunt";

    /**
     * 分流参数类型
     */
    public static final String SHUNT_PARAM_TYPE = "Header|Query|Cookie|Path";

    /**
     * 分流目标类型，分为应用、IP、版本和标签
     */
    public static final String SHUNT_INSTANCE_TYPE = "Application|Ip|Version|Tag|CustomTag";

    public static final String SHUNT_INSTANCE_TYPE_APPLICATION = "Application";

    public static final String SHUNT_INSTANCE_TYPE_IP = "Ip";

    public static final String SHUNT_INSTANCE_TYPE_VERSION = "Version";

    public static final String SHUNT_INSTANCE_TYPE_TAG = "Tag";

    public static final String SHUNT_INSTANCE_TYPE_CUSTOM_TAG = "CustomTag";


    public static final String SHUNT_INSTANCE_TYPE_TAG_REGEX = "[A-Za-z0-9+-._/@]{1,255}=[A-Za-z0-9+-._/@]{1,255}";


    /**
     * 网关授权，网关实例授权
     */
    public static final String GATEWAY_AUTH = "GatewayAuth";
    public static final String GATEWAY_API_AUTH = "GatewayApi";
    public static final String GATEWAY_AUTH_ID = "gw";

    public static final String AUTH_GATEWAY_ROUTE = "gw_route";
    public static final String AUTH_GATEWAY_SERVICE = "gw_service";
    public static final String AUTH_GATEWAY = "gw_project";

    /**
     * swagger导入
     */
    public static final String SWAGGER_DETAILS = "SwaggerDetails";
    public static final String SWAGGER_DETAILS_KEY = "SwaggerDetailKey";

    /**
     * 注册中心类型
     */
    public static final String REGISTRY_CENTER_TYPE_REGEX = "Eureka|Consul|Zookeeper";


    /**
     * 策略维度
     */
    public static final String OBJECT_TYPE_SERVICE = "service";
    public static final String OBJECT_TYPE_API = "api";
    public static final String OBJECT_TYPE_GLOBAL = "global";

    /**
     * envoy const配置
     */
    /**
     * envoy元数据服务名称，必填，支持任意字符，32位
     */
    public static final String REGEX_ENVOY_SERVICE_NAME = "^[\\s\\S]{1,64}";

    public static final String STATIC_PUBLISH_TYPE = "STATIC";
    public static final String DYNAMIC_PUBLISH_TYPE = "DYNAMIC";

    /**
     * 使能状态
     */
    public static final String ROUTE_RULE_ENABLE_STATE = "enable";
    public static final String ROUTE_RULE_DISABLE_STATE = "disable";

    public static final long ERROR_RESULT = -1;


    public static final String desKey = "#%^1*&(*HRqzlUn]";

    /**
     * nginx捕获相关正则，非标准正则，需要在创建路由中提示
     */
    public static final String NGINX_CAPTURE_REGEX = ".*\\?<.*>.*";


    public static final String POLICY_BIND_STATUS_REGEX = "BIND|UNBIND";
    public static final String POLICY_BIND = "BIND";
    public static final String POLICY_UNBIND = "UNBIND";

    public static final String ENVOY_GATEWAY_TYPE = "envoy";
    public static final Set<String> CONST_METHODS = new HashSet<>(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "PATCH", "OPTIONS", "TRACE", "CONNECT"));

    public static final Set<String> SORT_KEY = new HashSet<>(Arrays.asList("create_time", "priority"));
    public static final Set<String> SORT_VALUE = new HashSet<>(Arrays.asList("desc", "asc"));

    public static final String URI_TYPE_EXACT = "exact";
    public static final String URI_TYPE_PREFIX = "prefix";
    public static final String URI_TYPE_REGEX = "regex";
    public static final Set<Integer> HTTP_STATUS_CODE = new HashSet<Integer>(Arrays.asList(100, 101, 103, 200, 201, 202, 203, 204, 205, 206, 300, 301, 302, 303, 304, 307,
            308, 400, 401, 402, 403, 404, 405, 406, 407, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 418, 422, 425, 426, 428, 429, 431, 451, 500, 502, 503, 504, 505, 511));
    public static final String GLOBAL_PLUGIN_TRACE = "neTraceFileLog";
    public static final String PLUGIN_NAME_RETRY = "com.netease.resty";
    public static final String PLUGIN_NAME_TRAFFIC_MARK = "proxy.filters.http.traffic_mark";
    /**
     * 流量染色插件的plugin_type
     */
    public static final String PLUGIN_TYPE_TRAFFIC_MARK = "traffic-mark";
    /**
     * 插件启动状态
     */
    public static final Integer PLUGIN_STATE_DISABLE = 0;
    public static final Integer PLUGIN_STATE_ENABLE = 1;
    /**
     * Envoy Dubbo Header
     */
    public static final String HEADER_DUBBO_INTERFACE = "x-dubbo-service";
    public static final String HEADER_DUBBO_METHOD = "x-dubbo-method";
    public static final String HEADER_DUBBO_GROUP = "x-dubbo-group";
    public static final String HEADER_DUBBO_VERSION = "x-dubbo-version";
    public static final String HEADER_DUBBO_PARAMS = "x-dubbo-params";
    public static final String HEADER_DUBBO_GENERIC = "x-dubbo-generic";
    public static final String HEADER_DUBBO_REQUIRED = "x-dubbo-required";
    public static final String HEADER_DUBBO_DEFAULT = "x-dubbo-default";
    public static final String HEADER_DUBBO_PARAM_SOURCE = "x-dubbo-source";
    public static final String HEADER_DUBBO_ATTACTMENT_HEADER = "x-dubbo-header";
    public static final String HEADER_DUBBO_ATTACTMENT_COOKIE = "x-dubbo-cookie";
    public static final String HEADER_DUBBO_CUSTOM_PARAMS_MAPPING_SWITCH = "x-dubbo-custom-param-mapping-switch";
    public static final String DUBBO_SERVICE_SUFFIX = ".dubbo";
    public static final String DUBBO_META_REFRESH_KEY_TEMPLATE = "api-gateway-dubbo-org.hango.cloud.dashboard.meta-%d-%s";
    public static final String SCOPE_TYPE_TENANT = "tenant";
    public static final String SCOPE_TYPE_PROJECT = "project";
    /**
     * 项目名称
     */
    public static final String PROJECT_CODE = "projectCode";
    public static final String HOST_BINDING_TYPE = "host";
    public static final String PROJECT_BINDING_TYPE = "project";
    /**
     * nsf eureka其别名为NSF，在初始化注册中心时会配置到db中
     */
    public static final String NSF_EUREKA_ALIAS = "NSF";
    /**
     * nsf eureka服务项目隔离后服务名后缀一部分，例如"service.nsf.project1.eureka"
     */
    public static final String EUREKA_NSF_TAG = ".nsf.";
    public static final String ERROR_CODE = "errorCode";
    /**
     * 查询服务接口的过滤条件前缀字符
     */
    public static final String PREFIX_LABEL = "label_";
    public static final String PREFIX_HOST = "host_";
    public static final String PREFIX_ADDRESS = "address_";
    public static final String PREFIX_PORT = "port_";
    public static final String PREFIX_PROTOCOL = "protocol_";
    /**
     * 网关根据项目ID查询结果集合key
     */
    public static final String SELF_ASSOCIATED_GATEWAYS = "self_associated_gateways";
    public static final String OTHER_ASSOCIATED_GATEWAYS = "other_associated_gateways";
    public static String CONST_PRIORITY = "priority";
    public static String CONST_DESC = "desc";
    public static String CONST_ASC = "asc";
}
