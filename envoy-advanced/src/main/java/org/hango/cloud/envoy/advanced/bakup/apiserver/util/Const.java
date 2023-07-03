package org.hango.cloud.envoy.advanced.bakup.apiserver.util;


/**
 * global constants.
 *
 * @author Feng Changjian (hzfengchj@corp.netease.com)
 * @version $Id: Const.java, v 1.0 2013-8-2 下午03:59:33
 */
public class Const {
    public static final String DEFAULT_ENCODING = "utf-8";
    /**
     * Http Method Type
     */
    public static final String GET_METHOD = "GET";

    public static final String POST_METHOD = "POST";

    public static final String PUT_METHOD = "PUT";

    public static final String HEAD_METHOD = "HEAD";

    public static final String DELETE_METHOD = "DELETE";

    /**
     * 服务名称：必填，支持中文、数字、英文大小写、中划线、下划线，最大长度64字符
     */
    public static final String REGEX_SERVICE_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\.]{1,64}";
    /**
     * 服务标识，必填，支持英文小写，数字，最长64字符
     */
    public static final String REGEX_SERVICE_TAG = "^[a-z_\\-\\/A-Z0-9]{1,63}";
    /**
     * 备注信息，选填，支持全文本，最长64字符
     */
    public static final String REGEX_DESCRIPTION = "^[\\s\\S]{0,200}";

    public static final String REGEX_HEALTH_INTERFACE = "(/\\S{0,200}){0,1}";


    public static final String G_DASHBOARD_PREFIX = "/gdashboard";

    public static final String ENVOY_GATEWAY_PREFIX = G_DASHBOARD_PREFIX + "/envoy";

    /**
     * 参数类型，分为REQUEST和RESPONSE和QUERYSTRING
     */
    public static final String REQUEST_PARAM_TYPE = "REQUEST";

    public static final String RESPONSE_PARAM_TYPE = "RESPONSE";

    public static final String QUERYSTRING_PARAM_TYPE = "QUERYSTRING";

    /**
     * 审计相关
     */
    public static final String AUDIT_RESOURCE_TYPE_API = "API";
    public static final String AUDIT_RESOURCE_TYPE_API_REQUEST_BODY = "API 请求体";
    public static final String AUDIT_RESOURCE_TYPE_API_RESPONSE_BODY = "API 响应体";
    public static final String AUDIT_RESOURCE_TYPE_API_QUERY_STRING = "API Query String";
    public static final String AUDIT_RESOURCE_TYPE_API_STATUS_CODE = "API 响应状态码";
    public static final String AUDIT_RESOURCE_TYPE_API_REQUEST_HEADER = "API 请求头";
    public static final String AUDIT_RESOURCE_TYPE_API_RESPONSE_HEADER = "API 响应头";
    public static final String AUDIT_RESOURCE_TYPE_API_MODEL = "API 模型";
    public static final String AUDIT_RESOURCE_TYPE_GATEWAY = "网关";
    public static final String AUDIT_RESOURCE_TYPE_PLUGIN_TEMPLATE = "插件模板";

    /**
     * Envoy审计相关
     */
    public static final String AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN = "插件";

    /**
     * swagger导入
     */
    public static final String SWAGGER_DETAILS = "SwaggerDetails";
    public static final String SWAGGER_DETAILS_KEY = "SwaggerDetailKey";

    public static final String ROUTE_RULE_DISABLE_STATE = "disable";

    public static final String SCOPE_TYPE_TENANT = "tenant";
}
