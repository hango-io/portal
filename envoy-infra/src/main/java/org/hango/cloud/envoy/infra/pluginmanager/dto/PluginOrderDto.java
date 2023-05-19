package org.hango.cloud.envoy.infra.pluginmanager.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class PluginOrderDto {

    /**
     * plugin manager 名称
     */
    @JSONField(name = "Name")
    private String name;

    /**
     * plugin manager 名称
     */
    @JSONField(name = "GatewayKind")
    private String gatewayKind;


    @JSONField(name  = "Plugins")
    private List<PluginOrderItemDto> plugins;


    public List<PluginOrderItemDto> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginOrderItemDto> plugins) {
        this.plugins = plugins;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGatewayKind() {
        return gatewayKind;
    }

    public void setGatewayKind(String gatewayKind) {
        this.gatewayKind = gatewayKind;
    }
}
