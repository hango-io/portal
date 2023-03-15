package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

import javax.validation.constraints.Pattern;

/**
 * 插件Dto
 *
 * @author hzchenzhongyang 2019-10-23
 */
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
     * 插件创建时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "CreateTime")
    private long createTime;
    /**
     * 插件更新时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;
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
    /**
     * 插件逻辑，用于网关使用
     */
    @JSONField(name = "PluginHandler")
    private String pluginHandler;
    /**
     * 插件优先级，数字越小优先级越高，不允许重复
     */
    @JSONField(name = "PluginPriority")
    private long pluginPriority;

    @JSONField(name = "CategoryKey")
    private String categoryKey;

    @JSONField(name = "CategoryName")
    private String categoryName;

    @JSONField(name = "PluginGuidance")
    private String pluginGuidance;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getPluginScope() {
        return pluginScope;
    }

    public void setPluginScope(String pluginScope) {
        this.pluginScope = pluginScope;
    }

    public String getInstructionForUse() {
        return instructionForUse;
    }

    public void setInstructionForUse(String instructionForUse) {
        this.instructionForUse = instructionForUse;
    }

    public String getPluginSchema() {
        return pluginSchema;
    }

    public void setPluginSchema(String pluginSchema) {
        this.pluginSchema = pluginSchema;
    }

    public String getPluginHandler() {
        return pluginHandler;
    }

    public void setPluginHandler(String pluginHandler) {
        this.pluginHandler = pluginHandler;
    }

    public long getPluginPriority() {
        return pluginPriority;
    }

    public void setPluginPriority(long pluginPriority) {
        this.pluginPriority = pluginPriority;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPluginGuidance() {
        return pluginGuidance;
    }

    public void setPluginGuidance(String pluginGuidance) {
        this.pluginGuidance = pluginGuidance;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
