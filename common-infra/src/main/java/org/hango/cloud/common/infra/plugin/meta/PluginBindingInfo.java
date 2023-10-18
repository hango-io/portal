package org.hango.cloud.common.infra.plugin.meta;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

/**
 * 插件绑定关系meta
 *
 * @author hzchenzhongyang 2019-11-11
 */
@Getter
@Setter
@TableName("hango_plugin_binding")
public class PluginBindingInfo extends CommonExtension {
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
     * 对象-插件绑定关系作用的网关id
     */
    private Long virtualGwId;

    /**
     * 插件绑定对象id，与bindingObjectType共同确定一个具体对象
     */
    private String bindingObjectId;

    /**
     * 插件绑定对象类型，与bindingObjectId共同确定一个具体对象
     */
    private String bindingObjectType;

    /**
     * 插件配置
     */
    private String pluginConfiguration;

    /**
     * 绑定插件类型
     */
    private String pluginType;

    /**
     * 绑定插件名称
     */
    private String pluginName;
    /**
     * 绑定关系所属项目id
     */
    private Long projectId;

    /**
     * 状态，disable/enable，disable时插件在数据面不生效
     */
    private String bindingStatus;

    /**
     * 关联模板id,0表示没有关联模板
     */
    private Long templateId;

    /**
     * 关联插件模板版本号，若与模板当前版本号不一致则可以通过同步实现
     */
    private Long templateVersion;

    /**
     * 网关类型
     */
    private String gwType;

    /**
     * 版本号
     */
    private Long version;

}
