package org.hango.cloud.dashboard.apiserver.service.impl.sdk;

/**
 * 请求类头部数据结构 用于请求头和响应头
 *
 * @author Hu Yuchao(huyuchao@corp.netease.com)
 */
public class HeaderParameter {
    private String value;
    private String name;

    public HeaderParameter(String name, String value) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String type) {
        this.value = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
