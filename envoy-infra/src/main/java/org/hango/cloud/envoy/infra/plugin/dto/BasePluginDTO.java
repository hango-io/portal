package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author zhufengwei
 * @Date 2023/8/10
 */
@Getter
@Setter
public class BasePluginDTO implements Serializable {
    private static final long serialVersionUID = -6287446275633570700L;
    /**
     * plugin manager name
     */
    @JSONField(name = "Name")
    private String name;


    /**
     * 插件名称
     */
    @JSONField(name = "PluginType")
    private String pluginType;

    /**
     * 插件配置
     */
    @JSONField(name = "PluginConfig")
    private String pluginConfig;

    /**
     * 插件语言 lua/wasm/inline
     */
    @JSONField(name = "Language")
    private String language;

    @JSONField(serialize = false)
    private String addr;

}
