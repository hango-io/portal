package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @ClassName GetCustomPluginListDto
 * @Description 查询插件实例列表dto
 * @Author xianyanglin
 * @Date 2023/6/30 17:36
 */
public class CustomPluginInstanceListQueryDto {
    /**
     * 插件Id
     */
    @JSONField(name = "PluginId")
    private Long pluginId;
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

    public Long getPluginId() {
        return pluginId;
    }

    public void setPluginId(Long pluginId) {
        this.pluginId = pluginId;
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
        return "GetCustomPluginInstanceListDto{" +
                "pluginId='" + pluginId + '\'' +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}
