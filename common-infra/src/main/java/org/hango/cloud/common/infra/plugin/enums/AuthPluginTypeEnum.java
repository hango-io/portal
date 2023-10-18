package org.hango.cloud.common.infra.plugin.enums;

import java.util.Arrays;
import java.util.List;

/**
 * auth_plugin_type enum
 */
public enum AuthPluginTypeEnum {

    SIGNAUTH("sign-auth", "aksk_authn_type"),
    JWTAUTH("jwt-auth", "jwt_authn_type"),
    OAUTH2AUTH("oauth2-auth", "oauth2_authn_type"),
    ;

    private String pluginType;
    private String authnType;

    AuthPluginTypeEnum(String pluginType, String authnType) {
        this.pluginType = pluginType;
        this.authnType = authnType;
    }

    public static AuthPluginTypeEnum get(String pluginType) {
        for (AuthPluginTypeEnum value : AuthPluginTypeEnum.values()) {
            if (value.getPluginType().equals(pluginType)) {
                return value;
            }
        }
        return null;
    }

    public static Boolean isAuthPlugin(String pluginType) {
        return get(pluginType) != null;
    }

    public String getPluginType() {
        return pluginType;
    }

    public static List<String> getPluginTypeList() {
        return Arrays.stream(values()).map(AuthPluginTypeEnum::getPluginType).collect(java.util.stream.Collectors.toList());
    }

}