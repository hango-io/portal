package org.hango.cloud.envoy.infra.pluginmanager.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

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
    @JSONField(name = "GatewayLabels")
    private Map<String, String> gatewayLabels;

    @NotEmpty(message = "plugins")
    @JSONField(name  = "Plugins")
    private List<PluginOrderItemDto> plugins;

    public Map<String, String> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(Map<String, String> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }

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
