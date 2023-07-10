package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @ClassName DeletePluginDto
 * @Description 删除插件dto
 * @Author xianyanglin
 * @Date 2023/6/30 16:56
 */
public class DeletePluginDto {
    /**
     * 数据库自增id
     */
    @JSONField(name = "Id")
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
