package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName DescribeCustomPluginDtoResult
 * @Description 插件详情
 * @Author xianyanglin
 * @Date 2023/7/3 19:12
 */
@Getter
@Setter
public class DescribeCustomPluginDto {
    /**
     * 插件Id
     */
    @JSONField(name = "Id")
    private Long id;
    /**
     * 插件名称
     */
    @JSONField(name = "PluginType")
    private String pluginType;
    /**
     * 插件中文名称
     */
    @JSONField(name = "PluginName")
    private String pluginName;
    /**
     * 插件描述
     */
    @JSONField(name = "Description")
    private String description;
    /**
     * 插件语言
     */
    @JSONField(name = "Language")
    private String language;
    /**
     * 脚本类型
     */
    @JSONField(name = "SourceType")
    private String sourceType;
    /**
     * 脚本内容
     */
    @JSONField(name = "SourceContent")
    private String sourceContent;
    /**
     * 脚本类型
     * 执行阶段 security(安全)，authentication(认证)，trafficManagement(流量治理)，transformation(请求响应转换)
     */
    @JSONField(name = "PluginCategory")
    private String pluginCategory;
    /**
     * 插件作用域:route(路由)，global(全局)
     */
    @JSONField(name = "PluginScope")
    private String pluginScope;

    /**
     * 插件作用域:online（上架）、offline（下架）
     */
    @JSONField(name = "PluginStatus")
    private String pluginStatus;

    /**
     * schema表单
     */
    @JSONField(name = "SchemaContent")
    private String schemaContent;

    /**
     * 插件联系人
     */
    @JSONField(name = "Author")
    private String author;

    /**
     * 插件修改时间
     */
    @JSONField(name = "CreateTime")
    private Long createTime;

    /**
     * 插件修改时间
     */
    @JSONField(name = "UpdateTime")
    private Long updateTime;

    @Override
    public String toString() {
        return "DescribeCustomPluginDto{" +
                "id=" + id +
                ", pluginType='" + pluginType + '\'' +
                ", pluginName='" + pluginName + '\'' +
                ", description='" + description + '\'' +
                ", language='" + language + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", sourceContent='" + sourceContent + '\'' +
                ", pluginCategory='" + pluginCategory + '\'' +
                ", pluginScope='" + pluginScope + '\'' +
                ", pluginStatus='" + pluginStatus + '\'' +
                ", schemaContent='" + schemaContent + '\'' +
                ", author='" + author + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
