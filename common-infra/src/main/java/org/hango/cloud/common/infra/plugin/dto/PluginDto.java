package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

import javax.validation.constraints.Pattern;

/**
 * 插件Dto
 *
 * @author hzchenzhongyang 2019-10-23
 */
@Getter
@Setter
public class PluginDto extends CommonExtensionDto {
    /**
     * 数据库自增id
     */
    @JSONField(name = "Id")
    private long id;
    /**
     * 插件名称，展示使用，如：分布式限流、单机版限流
     */
    @JSONField(name = "PluginName")
    @Pattern(regexp = "([\\s\\S]){1,254}", message = "参数 PluginName 不能为空且长度不能超过255")
    private String pluginName;
    /**
     * 插件类型，全局唯一，如：WhiteListPlugin、RateLimiterPlugin
     */
    @JSONField(name = "PluginType")
    @Pattern(regexp = "([\\s\\S]){1,254}", message = "参数 PluginType 不能为空且长度不能超过255")
    private String pluginType;
    /**
     * 插件开发者，若为system则代表系统预置插件，系统预置插件不允许修改
     */
    @JSONField(name = "Author")
    @Pattern(regexp = "([\\s\\S]){1,254}", message = "参数 Author 不能")
    private String author;
    /**
     * 插件作用范围，即可绑定对象，可选值为route rule、service、global
     */
    @JSONField(name = "PluginScope")
    private String pluginScope;
    /**
     * 插件使用说明，用于前端展示、指导用户使用
     */
    @JSONField(name = "InstructionForUse")
    private String instructionForUse;
    /**
     * 插件表单schema，用于前端渲染表单
     */
    @JSONField(name = "PluginSchema")
    private String pluginSchema;


    @JSONField(name = "CategoryKey")
    private String categoryKey;

    @JSONField(name = "CategoryName")
    private String categoryName;

    @JSONField(name = "PluginGuidance")
    private String pluginGuidance;

    @JSONField(name = "PluginSource")
    private String pluginSource;
}
