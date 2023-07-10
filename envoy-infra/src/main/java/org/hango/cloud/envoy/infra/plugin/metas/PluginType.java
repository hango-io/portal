package org.hango.cloud.envoy.infra.plugin.metas;

import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * @Author zhufengwei
 * @Date 2023/7/5
 */
public enum PluginType {
    SECURITY("security", "安全"),
    AUTH("auth", "认证鉴权"),
    TRAFFIC_POLICY("trafficPolicy", "流量管理"),
    DATA_FORMAT("dataFormat", "数据转换");


    private final String name;
    private final String nameZh;

    PluginType(String name, String nameZh) {
        this.name = name;
        this.nameZh = nameZh;
    }

    public String getName() {
        return name;
    }

    public String getNameZh() {
        return nameZh;
    }

    public static String getByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return Stream.of(PluginType.values())
                .filter(pluginType -> pluginType.getName().equals(name))
                .map(PluginType::getNameZh)
                .findFirst()
                .orElse(null);
    }
}
