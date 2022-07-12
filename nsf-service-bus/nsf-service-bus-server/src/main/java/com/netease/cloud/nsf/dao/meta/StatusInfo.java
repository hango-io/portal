package com.netease.cloud.nsf.dao.meta;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/24
 **/
public class StatusInfo {
    private String name;
    private String value;

    public StatusInfo(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
