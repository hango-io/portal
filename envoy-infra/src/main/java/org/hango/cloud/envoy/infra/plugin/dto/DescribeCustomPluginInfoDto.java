package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @ClassName DescribeCustomPluginInfoDto
 * @Description 根据插件ID查询插件详情dto
 * @Author xianyanglin
 * @Date 2023/6/30 17:30
 */
public class DescribeCustomPluginInfoDto {
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
