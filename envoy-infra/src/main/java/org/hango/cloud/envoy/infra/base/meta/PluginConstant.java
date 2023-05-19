package org.hango.cloud.envoy.infra.base.meta;

/**
 * 插件常量类
 *
 * @author yutao04
 * @date 2021/12/27
 */
public class PluginConstant {
    /**
     * VS类型白名单集合的配置文件路径
     */
    public static final String VS_PLUGIN_LIST_CONF = "gdashboard-application.properties";

    /**
     * 网关插件流程中的日志标识符
     */
    public static final String PLUGIN_LOG_NOTE = "[gateway plugin]";

    /**
     * VS插件列表在properties中的key
     */
    public static final String VS_PLUGIN_LIST_NAME = "plugin.VSPluginList";

    /**
     * 英文逗号
     */
    public static final String COMMA_SIGN = ",";

    /**
     * 插件作用范围：host
     */
    public static final String PLUGIN_SCOPE_HOST = "host";

    /**
     * 插件作用范围：global
     */
    public static final String PLUGIN_SCOPE_GLOBAL = "global";
}
