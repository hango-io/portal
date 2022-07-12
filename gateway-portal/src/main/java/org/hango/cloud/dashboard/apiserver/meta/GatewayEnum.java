package org.hango.cloud.dashboard.apiserver.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2021/11/18
 */
public enum GatewayEnum {
    ENVOY("envoy"),
    SCG("Spring Cloud Gateway");

    private String type;


    GatewayEnum(String type) {
        this.type = type;
    }

    public static GatewayEnum getByType(String type) {
        for (GatewayEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        throw new RuntimeException("不存在的网关类型 : " + type);
    }

    public String getType() {
        return type;
    }
}
