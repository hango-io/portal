package org.hango.cloud.common.infra.base.meta;

/**
 * @Author zhufengwei
 * @Date 2023/4/25
 */
public class ApiConst {
    /**
     * hangoV1版本
     */
    public static final String V_1_PREFIX = "/v1/";

    public static final String API = "api";


    public static final String ROUTE = "route";

    public static final String SERVICE = "service";

    public static final String REPUBLISH = "republish";


    public static final String VIRTUAL_GATEWAY = "virtualGateway";

    public static final String DOMAIN = "domain";

    public static final String HANGO_SERICE_V1_PREFIX = V_1_PREFIX + SERVICE;

    public static final String HANGO_VIRTUAL_GATEWAY_V1_PREFIX = V_1_PREFIX + VIRTUAL_GATEWAY;

    public static final String HANGO_DOMAIN_V1_PREFIX = V_1_PREFIX + DOMAIN;

    public static final String HANGO_REPUBLISH = V_1_PREFIX + REPUBLISH;
}
