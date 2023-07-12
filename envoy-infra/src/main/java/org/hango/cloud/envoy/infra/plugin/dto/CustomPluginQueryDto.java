package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName GetCustomPluginListDto
 * @Description 查询插件列表dto
 * @Author xianyanglin
 * @Date 2023/6/30 17:36
 */
@Getter
@Setter
public class CustomPluginQueryDto {
    /**
     * 插件类型
     */
    @JSONField(name = "PluginCategory")
    private String pluginCategory;
    /**
     * 插件名称
     */
    @JSONField(name = "PluginType")
    private String pluginType;
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
