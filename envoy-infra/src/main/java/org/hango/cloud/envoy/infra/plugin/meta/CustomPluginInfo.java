package org.hango.cloud.envoy.infra.plugin.meta;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName CustomPluginInfo
 * @Description 自定义插件信息
 * @Author xianyanglin
 * @Date 2023/7/1 15:47
 */
@Getter
@Setter
@Builder
@TableName(value = "hango_custom_plugin_info")
public class CustomPluginInfo {

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
     * 插件类型，必填、唯一， ua-restriction
     */
    private String pluginType;

    /**
     * 插件名称，非必填，ip黑白名单
     */
    private String pluginName;

    /**
     * 插件描述
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String description;

    /**
     * 插件实现语言 lua/wasm
     */
    private String language;


    /**
     * 插件代码来源：file/oci
     */
    private String sourceType;

    /**
     * 插件地址
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String sourceUrl;

    /**
     * 镜像仓库secret
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String secretName;

    /**
     * 插件分类 trafficPolicy（流量管理）、auth(认证鉴权)  security(安全)、dataFormat（数据转换）
     */
    private String pluginCategory;

    /**
     * 插件状态 offline/online
     */
    private String pluginStatus;

    /**
     * 插件作用域
     */
    private String pluginScope;

    /**
     * 插件作者
     */
    private String author;

    /**
     * 插件schema
     */
    private String pluginSchema;

    /**
     * 脚本内容
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String pluginContent;
}
