package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.envoy.infra.pluginmanager.dto.RiderDTO;

/**
 * @Author zhufengwei
 * @Date 2023/9/26
 */
@Getter
@Setter
public class CustomPluginPublishDTO {
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
     * plugin 端口
     */
    @JSONField(name = "Port")
    private Integer port;

    /**
     * wasm插件
     */
    @JSONField(name = "Wasm")
    private RiderDTO wasm;

    /**
     * lua插件
     */
    @JSONField(name = "Lua")
    private RiderDTO lua;

    /**
     * 插件分类 trafficPolicy（流量管理）、auth(认证鉴权)  security(安全)、dataFormat（数据转换）
     */
    @JSONField(name = "PluginCategory")
    private String pluginCategory;
}
