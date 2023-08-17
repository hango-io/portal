package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName PluginImportDTO
 * @Description 插入自定义插件面向Api-plane的dto
 * @Author xianyanglin
 * @Date 2023/6/30 15:50
 */
@Getter
@Setter
@Builder
public class CustomPluginDTO {
    /** 插件的名称: uri-restriction.lua */
    @JSONField(name = "PluginName")
    private String pluginName;

    /** 插件的内容 */
    @JSONField(name = "PluginContent")
    private String pluginContent;

    /** 网关标识 */
    @JSONField(name = "GwCluster")
    private String gwCluster;
}
