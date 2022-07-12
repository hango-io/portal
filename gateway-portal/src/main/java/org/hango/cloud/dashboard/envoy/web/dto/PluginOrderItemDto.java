package org.hango.cloud.dashboard.envoy.web.dto;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/3/23
 */
public class PluginOrderItemDto {

    private boolean enable;

    private String name;

    private Object settings;

    private Object inline;

    private Integer listenerType;

    private Integer port;

    public boolean getEnable() {
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

    public Object getSettings() {
        return settings;
    }

    public void setSettings(Object settings) {
        this.settings = settings;
    }

    public Object getInline() {
        return inline;
    }

    public void setInline(Object inline) {
        this.inline = inline;
    }

    public Integer getListenerType() {
        return listenerType;
    }

    public void setListenerType(Integer listenerType) {
        this.listenerType = listenerType;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
