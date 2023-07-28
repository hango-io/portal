package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @ClassName GetPluginSchemaByPluginIdDto
 * @Description 根据插件Id查询该插件的Schema
 * @Author xianyanglin
 * @Date 2023/6/30 19:15
 */
public class DescribeCustomPluginContentDto {
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
