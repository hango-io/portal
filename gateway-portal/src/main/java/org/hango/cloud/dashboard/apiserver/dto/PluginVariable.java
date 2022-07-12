package org.hango.cloud.dashboard.apiserver.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2019/2/28 上午9:59.
 */
public class PluginVariable implements Serializable {
    @JSONField(name = "Name")
    private String name;

    @JSONField(name = "Value")
    private String value;

    @JSONField(name = "Description")
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
