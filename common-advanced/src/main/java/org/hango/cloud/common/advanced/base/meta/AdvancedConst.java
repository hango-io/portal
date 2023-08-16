package org.hango.cloud.common.advanced.base.meta;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/8
 */
public class AdvancedConst {

    public static final String AUDIT_DATASOURCE_MYSQL = "mysql";
    public static final String AUDIT_DATASOURCE_MONGO = "mongo";
    public static final String AUDIT_DATASOURCE_ELASTICSEARCH = "elasticsearch";

    public static final String SERVICE_MODULE = "api_gateway";


    public static final String REGEX_GATEWAY_TYPE = "envoy|Spring Cloud Gateway";
    public static final String ENVOY_GATEWAY_TYPE = "envoy";
    public static final String SCG_GATEWAY_TYPE = "Spring Cloud Gateway";

    public static final String MODULE_PLATFORM_USER_AUTH = "platform-user-auth";
    public static final String MODULE_PLATFORM_SERVICE_AUTH = "platform-service-auth";
    public static final String MODULE_PLATFORM_PROM = "prometheus";
    public static final String MODULE_NSF_META = "nsf-meta";
    public static final String MODULE_PLATFORM_AUDIT = "platform-audit";
    public static final String USER_AUTH_PREFIX = "/authority";
    public static final String AUDIT_PREFIX = "/audit";

    public static final Integer QUERY_MAX_DAY = 7;

    //可以直接放行的接口
    //FIXME 删除不要的uri
    public static final List<String> NO_USERPERMISSION = Arrays.asList("/api/auth/login",
            "/api/auth/oidc", "/api/restfulSdk", "/api/swagger", "/healthcheck", "/actuator/prometheus");


    public static final String PROJECT_DOMAIN = "project";
    public static final String SYSTEM_DOMAIN = "system";

    public static final String USER_PERMISSION = "x-auth-token";
    public static final String USER_ACCOUNT_ID = "x-auth-accountId";

    public static final String ID = "Id";

    public static final String PARENT_ID = "ParentId";

    public static final String PERMISSION_SCOPE_NAME = "PermissionScopeName";

    public static final String PERMISSION_SCOPE_EN_NAME = "PermissionScopeEnName";

    public static final String MODULE_META_SERVICE = "meta-service";





    /**
     * nsf eureka其别名为NSF，在初始化注册中心时会配置到db中
     */
    public static final String NSF_EUREKA_ALIAS = "NSF";
    /**
     * nsf eureka服务项目隔离后服务名后缀一部分，例如"service.nsf.project1.eureka"
     */
    public static final String EUREKA_NSF_TAG = ".nsf.";


    /**
     * 网关授权，网关实例授权
     */
    public static final String GATEWAY_AUTH = "GatewayAuth";
    public static final String GATEWAY_API_AUTH = "GatewayApi";
    public static final String GATEWAY_AUTH_ID = "gw";

    public static final String AUTH_GATEWAY_ROUTE = "gw_route";
    public static final String AUTH_GATEWAY_SERVICE = "gw_service";
    public static final String AUTH_GATEWAY = "gw_project";

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
    /**
     * 活跃连接数
     */
    public static final String ACTIVE_CONNECTION_COUNT = "ACTIVE_CONNECTION_COUNT";

    /**
     * 新增连接数
     */
    public static final String CONNECTION_COUNT = "CONNECTION_COUNT";

    /**
     * 入带宽
     */
    public static final String BYTE_RECEIVED = "BYTE_RECEIVED";

    /**
     * 出带宽
     */
    public static final String BYTE_SEND = "BYTE_SEND";
    /**
     * TLS握手数
     */
    public static final String TLS_HANDSHAKE = "TLS_HANDSHAKE";

    /**
     * TLS连接失败数
     */
    public static final String TLS_CONNECTION_ERROR = "TLS_CONNECTION_ERROR";



    /**
     * 审计查询相关
     */
    public static final String AUDIT_RESP_CODE_4XX = "4XX";
    public static final String AUDIT_RESP_CODE_5XX = "5XX";
    public static final String AUDIT_RESP_CODE_ALL = "ALL";
    public static final String ATTRIBUTE_FOR_AUDIT = "ATTRIBUTE_FOR_AUDIT";
    public static final String AUDIT_QUERY_TIMEOUT = "AUDIT_QUERY_TIMEOUT";
    public static final String AUDIT_HOST_UNREACHABLE = "AUDIT_ELASTICSEARCH_HOST_UNREACHABLE";
    public static final String AUDIT_SCROLL_TIMEOUT = "search_phase_execution_exception";


}
