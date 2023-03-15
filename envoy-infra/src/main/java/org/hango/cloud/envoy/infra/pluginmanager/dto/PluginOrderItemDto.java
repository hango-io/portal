package org.hango.cloud.envoy.infra.pluginmanager.dto;

import com.alibaba.fastjson.annotation.JSONField;

public class PluginOrderItemDto {

    @JSONField(name ="enable")
    private boolean enable;

    @JSONField(name ="name")
    private String name;

    @JSONField(name ="port")
    private Integer port;

    @JSONField(name ="inline")
    private Object inline;

    @JSONField(name ="listenerType")
    private String listenerType;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Object getInline() {
        return inline;
    }

    public void setInline(Object inline) {
        this.inline = inline;
    }

    public String getListenerType() {
        return listenerType;
    }

    public void setListenerType(String listenerType) {
        this.listenerType = listenerType;
    }
}
