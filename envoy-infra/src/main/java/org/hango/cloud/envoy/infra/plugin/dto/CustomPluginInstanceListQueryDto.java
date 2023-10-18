package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName GetCustomPluginListDto
 * @Description 查询插件实例列表dto
 * @Author xianyanglin
 * @Date 2023/6/30 17:36
 */
@Getter
@Setter
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
}
