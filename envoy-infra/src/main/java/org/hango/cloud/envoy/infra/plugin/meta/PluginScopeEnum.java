package org.hango.cloud.envoy.infra.plugin.meta;

/**
 * @ClassName PluginScopeEnum
 * @Description 插件作用域枚举类
 * @Author xianyanglin
 * @Date 2023/7/10 14:28
 */
public enum PluginScopeEnum {
    ROUTE_RULE("routeRule"),
    GLOBAL("global"),
    GATEWAY("gateway");
    private final String pluginScope;
    // 枚举值的属性和构造方法
    PluginScopeEnum(String pluginScope) {
        this.pluginScope = pluginScope;
    }

    public String getPluginScope() {
        return pluginScope;
    }

    public static PluginScopeEnum fromScope(String type) {
        for (PluginScopeEnum pluginScopeEnum : PluginScopeEnum.values()) {
            if (pluginScopeEnum.pluginScope.equals(type)) {
                return pluginScopeEnum;
            }
        }
        return null;
    }
}
