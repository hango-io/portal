package org.hango.cloud.envoy.infra.base.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/8/29
 */
public class EnvoyConst {

    /**
     * 项目名称
     */

    public static final String MODULE_API_PLANE = "apigw-api-plane";

    public static final String ENVOY_GATEWAY_TYPE = "envoy";

    /**
     * dubbo 相关常量
     */
    public static final String DUBBO_META_REFRESH_KEY_TEMPLATE = "api-gateway-dubbo-meta-%d-%s";

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
}
