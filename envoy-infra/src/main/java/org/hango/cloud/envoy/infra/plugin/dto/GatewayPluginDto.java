package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author yutao04
 * @date 2021.12.06
 * <p>
 * 路由插件实体类
 */
public class GatewayPluginDto extends CommonExtension implements Serializable {

    @JSONField(name = "Plugins")
    private List<String> plugins;

    @JSONField(name = "Hosts")
    private List<String> hosts;

    @JSONField(name = "Gateway")
    private String gateway;

    @JSONField(name = "GwCluster")
    private String gwCluster;

    /**
     * 插件作用域
     */
    @JSONField(name = "PluginScope")
    private String pluginScope;

    /**
     * 全局插件名称
     */
    @JSONField(name = "Code")
    private String code;

    /**
     * 端口
     */
    @JSONField(name = "Port")
    private Integer port;

    /**
     * 版本号
     */
    @JSONField(name = "Version")
    private Long version;

    /**
     * 插件id
     */
    @JSONField(serialize = false)
    private Long bindingObjectId;


    /**
     * 插件绑定的对象类型（是项目还是路由）
     */
    @JSONField(serialize = false)
    private String bindingObjectType;

    @JSONField(serialize = false)
    private String addr;

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public void setPlugin(String plugin) {
        if (StringUtils.isEmpty(plugin)) {
            return;
        }
        if(CollectionUtils.isEmpty(plugins)){
            plugins = Lists.newArrayList();
        }
        plugins.add(plugin);
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getPluginScope() {
        return pluginScope;
    }

    public void setPluginScope(String pluginScope) {
        this.pluginScope = pluginScope;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }


    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getBindingObjectId() {
        return bindingObjectId;
    }

    public void setBindingObjectId(Long bindingObjectId) {
        this.bindingObjectId = bindingObjectId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getBindingObjectType() {
        return bindingObjectType;
    }

    public void setBindingObjectType(String bindingObjectType) {
        this.bindingObjectType = bindingObjectType;
    }

}