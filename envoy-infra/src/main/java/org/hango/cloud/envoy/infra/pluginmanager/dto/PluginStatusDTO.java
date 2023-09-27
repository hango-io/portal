package org.hango.cloud.envoy.infra.pluginmanager.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author zhufengwei
 * @Date 2023/10/8
 */
@Getter
@Setter
public class PluginStatusDTO {
    /**
     * plugin manager 名称
     */
    @JSONField(name = "PluginManagerName")
    private String pluginManagerName;

    /**
     * plugin 名称
     */
    @JSONField(name = "PluginName")
    private String pluginName;

    /**
     * 插件状态
     */
    @JSONField(name = "Enable")
    private Boolean enable;
}
