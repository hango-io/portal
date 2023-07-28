package org.hango.cloud.envoy.infra.plugin.metas;

import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * @Author zhufengwei
 * @Date 2023/7/5
 */
public enum PluginType {
    SECURITY("security", "安全",3),
    AUTH("auth", "认证鉴权", 2),
    TRAFFIC_POLICY("trafficPolicy", "流量管理", 1),
    DATA_FORMAT("dataFormat", "数据转换", 4);


    private final String name;
    private final String nameZh;
    private final Integer order;

    PluginType(String name, String nameZh, Integer order) {
        this.name = name;
        this.nameZh = nameZh;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public String getNameZh() {
        return nameZh;
    }

    public Integer getOrder() {
        return order;
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

    public static Integer getOrder(String name){
        if (StringUtils.isBlank(name)){
            return Integer.MAX_VALUE;
        }
        return Stream.of(PluginType.values())
                .filter(pluginType -> pluginType.getName().equals(name))
                .map(PluginType::getOrder)
                .findFirst()
                .orElse(Integer.MAX_VALUE);
    }
}
