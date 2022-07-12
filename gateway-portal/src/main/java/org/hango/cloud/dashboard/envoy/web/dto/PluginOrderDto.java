package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
public class PluginOrderDto {

    @JSONField(name = "GatewayLabels")
    private Map<String, String> gatewayLabels;

    @NotEmpty(message = "plugins")
    @JSONField(name = "Plugins")
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
}
