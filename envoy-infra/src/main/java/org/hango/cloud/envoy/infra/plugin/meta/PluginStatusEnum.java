package org.hango.cloud.envoy.infra.plugin.meta;

/**
 * @ClassName PluginTypeEnum
 * @Description 插件状态枚举类
 * @Author xianyanglin
 * @Date 2023/7/5 15:50
 */
public enum PluginStatusEnum {
    ONLINE("online"),
    OFFLINE("offline");
    private final String status;
    // 枚举值的属性和构造方法
    PluginStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }


    public static PluginStatusEnum fromType(String type) {
        for (PluginStatusEnum pluginStatus : PluginStatusEnum.values()) {
            if (pluginStatus.getStatus().equalsIgnoreCase(type)) {
                return pluginStatus;
            }
        }
        return ONLINE;
    }
}
