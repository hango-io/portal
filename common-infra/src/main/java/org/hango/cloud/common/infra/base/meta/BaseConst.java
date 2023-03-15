package org.hango.cloud.common.infra.base.meta;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/1
 */
public class BaseConst {

    /************************************** Punctuation Start **************************************/


    /**
     * 冒号
     */
    public static final String SYMBOL_COLON = ":";

    /**
     * 逗号
     */
    public static final String SYMBOL_COMMA = ",";

    /**
     * 斜线
     */
    public static final String SYMBOL_SLASH = "/";

    public static final String SYMBOL_BACK_SLASH = "\\";

    /**
     * 问号
     */
    public static final String SYMBOL_QUESTION_MARK = "?";

    /**
     * 和号
     */
    public static final String SYMBOL_AND = "&";

    public static final String SYMBOL_ALTERNATION = "|";

    /**
     * 下划线
     */
    public static final String SYMBOL_UNDERLINE = "_";

    /**
     * 百分号
     */
    public static final String SYMBOL_PERCENT = "%";

    /**
     * 井号
     */
    public static final String SYMBOL_POUND_KEY = "#";

    /**
     * 中划线
     */
    public static final String SYMBOL_HYPHEN = "-";

    /**
     * 等号
     */
    public static final String SYMBOL_EQUAL = "=";


    /**
     * 星号
     */
    public static final String SYMBOL_ASTERISK = "*";

    /************************************** Punctuation End **************************************/


    /************************************** HTTP Client Config Start **************************************/

    /**
     * 连接超时时间
     */
    public static final Integer MAX_TIME_OUT = 5000;

    /**
     * 读取的超时时间
     */
    public static final Integer MAX_SO_TIME_OUT = 60000;

    /**
     * 整个连接管理器的最大连接数
     */
    public static final Integer MAX_CONN = 2000;

    /**
     * 每个目标主机的最大连接数
     */
    public static final Integer MAX_CONN_PER_HOST = 100;

    /**
     * Http Scheme
     */
    public static final String SCHEME_HTTP = "http";

    /**
     * Https Scheme
     */
    public static final String SCHEME_HTTPS = "https";

    /**
     * Scheme Joiner
     */
    public static final String SCHEME_JOINER = SYMBOL_COLON + "//";

    /**
     * Http Scheme
     */
    public static final String HTTP_PREFIX = SCHEME_HTTP + SCHEME_JOINER;

    /**
     * Https Scheme
     */
    public static final String HTTPS_PREFIX = SCHEME_HTTPS + SCHEME_JOINER;

    /**
     * HTTPS 默认端口
     */
    public static final Integer DEFAULT_HTTPS_PORT = 443;

    /**
     * HTTP 默认端口
     */
    public static final Integer DEFAULT_HTTP_PORT = 80;

    /**
     * 最大日志记录长度
     */
    public static final int MAX_LOG_INFO_LENGTH = 6144;

    /************************************** HTTP Client Config End **************************************/

    /**
     * Some time constants
     */
    public static final long MS_OF_MINUTE = 60 * 1000;

    public static final long MS_OF_HOUR = 60 * 60 * 1000;

    public static final long MS_OF_DAY = 24 * MS_OF_HOUR;

    public static final long HOUR_OF_DAY = 24;

    /**
     * 默认Map长度
     */
    public static final Integer DEFAULT_MAP_SIZE = 16;

    /**
     * 未知信息
     */
    public static final String UNKNOWN = "unknown";

    public static final String DEFAULT = "default";

    public static final String NEW_LINE = "\n";

    /**
     * portal 错误信息返回语言
     */
    public static final String ACCEPT_LANGUAGE = "X-163-AcceptLanguage";

    /**
     * 中文返回
     */
    public static final String ACCEPT_LANGUAGE_ZH = "zh";

    /**
     * hango配置项前缀定义
     */
    public static final String HANGO_PREFIX = "org.hango";

    /**
     * 操作审计开关项配置
     */
    public static final String OPERATION_AUDIT_ENABLE = HANGO_PREFIX + ".operation.audit.enable";
    /**
     * 配置项，确定是否加载hango基础模块dao层
     */
    public static final String HANGO_INFRA_DAO_ENABLE = HANGO_PREFIX + ".infra.dao.enable";


    /**
     * 备注信息，选填，支持全文本，最长200字符
     */
    public static final String REGEX_DESCRIPTION = "^[\\s\\S]{0,200}";

    /**
     * api-prefix
     */
    public static final String HANGO_DASHBOARD_PREFIX = "/gdashboard";

    /**
     * hangoV1版本
     */
    public static final String HANGO_VIRTUAL_GATEWAY_V1_PREFIX = "/v1/virtualGateway";

    public static final String ACTION = "Action";

    public static final String VERSION = "Version";
    public static final String VERSION_18_08_09 = "2018-08-09";



    public static final String PLANE_VERSION = "2019-07-25";

    public static final String PLANE_PORTAL_PATH = "/api/portal";

    public static final String PLANE_PLUGIN_PATH = "/api/plugin";
    public static final String META_PATH = "/api/metadata";

    public static final String RESULT = "Result";

    public static final String RESULT_LIST = "List";

    public static final String ACCOUNT_ID = "AccountId";

    /**
     * 可以直接放行的接口
     */
    public static final List<String> IGNORE_CHECK_URI = Lists.newArrayList("/api/auth/login",
            "/api/auth/oidc", "/api/restfulSdk", "/api/swagger", "/healthcheck");


    /************************************** Gateway Manager Start **************************************/

    /**
     * 网关名称：必填，支持中文，数字，英文大小写，中划线，下划线，最大长度32字符
     */
    public static final String REGEX_GATEWAY_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\.]{1,32}";

    /**
     * 网关、虚拟网关标识正则校验
     */
    public static final String REGEX_GATEWAY_CODE = "^(?!-)[[a-z0-9\\\\-]*]{1,64}(?<!-)$";

    public static final String REGEX_DNS_1123 = "[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*";

    public static final String REGEX_DOMAIN = "^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$";

    public static final String PROTOCOL_SCHEME_PATTERN = SCHEME_HTTPS + SYMBOL_ALTERNATION + SCHEME_HTTP;



    /**
     * 网关地址，必填，必须与网关实际配置一样
     */
    public static final String REGEX_GATEWAY_URL = "(https?|http)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";


    /************************************** Gateway Manager End **************************************/

    /**
     * 服务发布状态，已发布
     **/
    public static final int STATUS_PUBLISHED = 1;
    /**
     * 服务发布状态，未发布
     **/
    public static final int STATUS_UNPUBLISHED = 0;

    /**
     * 使能状态
     */

    public static final String ROUTE_RULE_ENABLE_STATE = "enable";
    public static final String ROUTE_RULE_DISABLE_STATE = "disable";

    /**
     * 服务发布类型
     */
    public static final String REGEX_SERVICE_ROUTE_TYPE = "RegistryCenter|CustomInput";

    /**
     * 服务类型
     */
    public static final String REGEX_SERVICE_TYPE = "http|dubbo|webservice|grpc";


    /**
     * API测试用例 校验规则 所在的位置body 或 header
     */
    public static final String POSITION_BODY = "body";

    public static final String POSITION_HEADER = "header";

    public static final String POSITION_COOKIE = "cookie";

    public static final String POSITION_DUBBO_PARAM = "dubbo_param";

    public static final String STATUS_CODE = "status_code";
    /**
     * API测试用例 校验规则
     */
    public static final String JSON_MATCH = "json_match";

    public static final String CONTAINS = "contains";


    public static final String REQUEST_EXAMPLE = "REQUEST_EXAMPLE";
    public static final String RESPONSE_EXAMPLE = "RESPONSE_EXAMPLE";

    public static final String API = "api";

    /**
     * 同步数据
     */
    public static final String API_ID = "apiId";
    public static final String OBJECT_ID = "objectId";

    public static final String ROUTE = "route";

    public static final String SERVICE = "service";

    public static final String PROJECT_PLUGIN = "projectPlugin";

    public static final String VIRTUAL_GATEWAY = "virtualGateway";

    public static final String MODEL = "model";
    public static final long ERROR_RESULT = -1;
    public static final String URI_TYPE_EXACT = "exact";

    public static final String URI_TYPE_PREFIX = "prefix";

    public static final String URI_TYPE_REGEX = "regex";

    /**
     * nginx捕获相关正则，非标准正则，需要在创建路由中提示
     */
    public static final String NGINX_CAPTURE_REGEX = ".*\\?<.*>.*";

    public static final Set<String> CONST_METHODS = new HashSet<>(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "PATCH", "OPTIONS", "TRACE", "CONNECT"));
    public static final long GLOBAL_PLUGIN_PROJECT_ID = 0L;
    public static final long NO_VERSION = 0;

    public static String CONST_PRIORITY = "priority";
    public static String CONST_DESC = "desc";
    public static String CONST_ASC = "asc";

    public static final String STATIC_PUBLISH_TYPE = "STATIC";
    public static final String DYNAMIC_PUBLISH_TYPE = "DYNAMIC";

    /**
     * 负载均衡策略相关
     */
    public static final String SERVICE_LOADBALANCER_SIMPLE = "Simple";
    public static final String SERVICE_LOADBALANCER_SIMPLE_ROUND_ROBIN = "ROUND_ROBIN";
    public static final String SERVICE_LOADBALANCER_SIMPLE_LEAST_CONN = "LEAST_CONN";
    public static final String SERVICE_LOADBALANCER_SIMPLE_RANDOM = "RANDOM";
    public static final String SERVICE_LOADBALANCER_HASH = "ConsistentHash";
    public static final String SERVICE_LOADBALANCER_HASH_HTTPHEADERNAME = "HttpHeaderName";
    public static final String SERVICE_LOADBALANCER_HASH_HTTPCOOKIE = "HttpCookie";
    public static final String SERVICE_LOADBALANCER_HASH_USESOURCEIP = "UseSourceIp";
    public static final String STATIC_EP_VERSION = "version";


    public static final String PRIORITY = "priority";
    public static final String PROJECT_ID = "projectId";


    public static final Set<String> SORT_KEY = new HashSet<>(Arrays.asList("create_time", PRIORITY));
    public static final Set<String> SORT_VALUE = new HashSet<>(Arrays.asList("desc", "asc"));

    public static final String DUBBO_SERVICE_SUFFIX = ".dubbo";

    public static final String AUTH_GATEWAY_ROUTE = "gw_route";
    public static final String AUTH_GATEWAY_SERVICE = "gw_service";
    public static final String AUTH_GATEWAY = "gw_project";

    /**
     * 路由名称：必填，支持中文、数字、英文大小写、中划线、下划线，最大长度200字符
     */
    public static final String REGEX_ROUTE_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\.\\/]{1,200}";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String SUCCESS = "Success";

    public static final String FAILED = "Failed";

    public static final String DATA = "DATA";

    public static final String PROJECT_CODE = "projectCode";

    /**
     * 路由级别插件标识
     */
    public static final String PLUGIN_TYPE_ROUTE = "routeRule";
    /**
     * 全局（项目）级别插件标识
     */
    public static final String PLUGIN_TYPE_GLOBAL = "global";

    /**
     * 域名级别插件标识
     */
    public static final String PLUGIN_TYPE_HOST = "host";

    /**
     * 查询服务接口的过滤条件前缀字符
     */
    public static final String PREFIX_LABEL = "label_";
    public static final String PREFIX_HOST = "host_";
    public static final String PREFIX_ADDRESS = "address_";
    public static final String PREFIX_PORT = "port_";
    public static final String PREFIX_PROTOCOL = "protocol_";

    public static final String DEFAULT_ENCODING = "utf-8";

    public static final String SOAP_JSON_TRANSCODER_PLUGIN = "soap-json-transcoder";

    public static final String VIRTUAL_GATEWAY_TYPE_NETWORK_PROXY = "NetworkProxy";

    public static final String SERVICE_ID = "serviceId";

    public static final String VIRTUAL_GW_ID = "virtualGwId";

    public static final String BINDING_OBJECT_ID = "bindingObjectId";

    public static final String BINDING_OBJECT_TYPE = "bindingObjectType";

    public static final String ROUTE_RULE_PROXY_LIST = "RouteRuleProxyList";

    public static final String PATTERN = "pattern";

    public static final String DO_FIND_MULTI_ENHANCEMENT = "doFindMultiEnhancement";

    public static final String DO_FIND_SINGLE_ENHANCEMENT = "doFindSingleEnhancement";

    public static final String OFFSET = "offset";

    public static final String LIMIT = "limit";

}
