package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;

import javax.validation.constraints.Pattern;

/**
 * @ClassName UpdatePluginStatusDto
 * @Description TODO
 * @Author xianyanglin
 * @Date 2023/6/30 16:20
 */
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPluginStatus() {
        return pluginStatus;
    }

    public void setPluginStatus(String pluginStatus) {
        this.pluginStatus = pluginStatus;
    }

    @Override
    public String toString() {
        return "UpdatePluginStatusDto{" +
                "id=" + id +
                ", pluginStatus='" + pluginStatus + '\'' +
                '}';
    }
}
