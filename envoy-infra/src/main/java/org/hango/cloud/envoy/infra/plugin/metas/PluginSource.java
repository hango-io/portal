package org.hango.cloud.envoy.infra.plugin.metas;

/**
 * @Author zhufengwei
 * @Date 2023/7/5
 */
public enum PluginSource {
    CUSTOM("custom"),
    SYSTEM("system");


    private final String name;

    PluginSource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
