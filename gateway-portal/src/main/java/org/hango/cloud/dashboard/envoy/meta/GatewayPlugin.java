package org.hango.cloud.dashboard.envoy.meta;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;

import java.util.List;

/**
 * @author yutao04
 * @date 2021.12.06
 * <p>
 * 路由插件实体类
 */
public class GatewayPlugin {

    private List<String> plugins;

    private List<String> hosts;

    private String gateway;

    private String pluginType;

    /**
     * 路由插件名称标识的一部分
     */
    private Long routeId;

    /**
     * 全局插件名称
     */
    private String code;

    private GatewayInfo gatewayInfo;

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public GatewayInfo getGatewayInfo() {
        return gatewayInfo;
    }

    public void setGatewayInfo(GatewayInfo gatewayInfo) {
        this.gatewayInfo = gatewayInfo;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    /**
     * GatewayPlugin对象转JSON字符串
     *
     * @return JSON类型字符串
     */
    public String toJsonString() {
        JSONObject gatewayPlugin = new JSONObject();
        gatewayPlugin.put("Plugins", this.plugins);
        gatewayPlugin.put("RouteId", this.routeId);
        gatewayPlugin.put("Hosts", this.hosts);
        gatewayPlugin.put("Gateway", this.gateway);
        gatewayPlugin.put("Code", this.code);
        gatewayPlugin.put("PluginType", this.pluginType);
        return gatewayPlugin.toJSONString();
    }

    /**
     * 根据路由标识判断该插件是否为路由级别插件
     *
     * @return 是否为路由级别插件（true: 路由级别；false: 非路由级别）
     */
    public boolean isRoutePlugin() {
        return StringUtils.isEmpty(code);
    }
}