package org.hango.cloud.envoy.infra.pluginmanager.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class PluginOrderDto {

    /**
     * plugin manager 名称
     */
    @JSONField(name = "Name")
    private String name;


    @JSONField(name = "GwCluster")
    private String gwCluster;

    /**
     * 虚拟网关类型 API网关/通用网关..
     */
    @JSONField(name = "GatewayKind")
    private String gatewayKind;

    /**
     * plm对应端口
     */
    @JSONField(name = "Port")
    private Integer port;


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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }
}
