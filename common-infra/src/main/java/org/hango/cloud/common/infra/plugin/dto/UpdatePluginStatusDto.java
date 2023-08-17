package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

/**
 * @ClassName UpdatePluginStatusDto
 * @Description TODO
 * @Author xianyanglin
 * @Date 2023/6/30 16:20
 */
@Getter
@Setter
public class UpdatePluginStatusDto {
    /**
     * 数据库自增id
     */
    @JSONField(name = "Id")
    private long id;
    /**
     * 插件状态
     */
    @JSONField(name = "PluginStatus")
    @Pattern(regexp = "online|offline")
    private String pluginStatus;
}
