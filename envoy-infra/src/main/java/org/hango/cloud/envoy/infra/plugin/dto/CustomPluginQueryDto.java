package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @ClassName GetCustomPluginListDto
 * @Description 查询插件列表dto
 * @Author xianyanglin
 * @Date 2023/6/30 17:36
 */
public class CustomPluginQueryDto {
    /**
     * 插件类型
     */
    @JSONField(name = "PluginType")
    private String pluginType;
    /**
     * 插件名称
     */
    @JSONField(name = "Name")
    private String name;
    /**
     * 偏移量
     */
    @JSONField(name = "Offset")
    private int offset;

    /**
     * 每页条数
     */
    @JSONField(name = "Limit")
    private int limit;

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "GetCustomPluginListDto{" +
                "pluginType='" + pluginType + '\'' +
                ", name='" + name + '\'' +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}
