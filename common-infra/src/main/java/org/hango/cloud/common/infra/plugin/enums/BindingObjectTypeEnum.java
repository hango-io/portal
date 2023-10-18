package org.hango.cloud.common.infra.plugin.enums;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @Author zhufengwei
 * @Date 2023/7/25
 */
public enum BindingObjectTypeEnum {
    ROUTE("routeRule", "路由级插件"),
    HOST("host", "域名级插件"),
    //历史原因，global并不是全局插件，而是项目级插件
    GLOBAL("global", "项目级插件"),
    GATEWAY("gateway", "网关级插件")
    ;

    private final String value;

    private final String desc;

    BindingObjectTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static BindingObjectTypeEnum getByValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Stream.of(values())
                .filter(o -> o.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    public static List<String> getCustomPluginScope(){
        return Arrays.asList(ROUTE.getValue(), GLOBAL.getValue(), GATEWAY.getValue());
    }


    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
