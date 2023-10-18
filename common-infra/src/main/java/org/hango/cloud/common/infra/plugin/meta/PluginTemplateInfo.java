package org.hango.cloud.common.infra.plugin.meta;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

/**
 * 插件模板info类
 *
 * @author hzchenzhongyang 2020-04-08
 */
@Getter
@Setter
@TableName("hango_plugin_template")
public class PluginTemplateInfo extends CommonExtension{
    /**
     * 表自增id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;

    /**
     * 绑定的插件类型，全局唯一，如：RateLimiter、WhiteList等
     */
    private String pluginType;

    /**
     * 绑定的插件名称
     */
    private String pluginName;

    /**
     * 插件配置
     */
    private String pluginConfiguration;
    /**
     * 插件绑定关系所属项目id
     */
    private Long projectId;
    /**
     * 插件模板版本
     */
    private Long templateVersion;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 模板备注
     */
    private String templateNotes;

}
