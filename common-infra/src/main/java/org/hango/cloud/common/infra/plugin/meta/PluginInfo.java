package org.hango.cloud.common.infra.plugin.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

import java.io.Serializable;
import java.util.Objects;

/**
 * 网关插件元信息
 *
 * @author hzchenzhongyang 2019-10-23
 */
public class PluginInfo extends CommonExtension implements Serializable {
    private static final long serialVersionUID = -8894242290672165439L;
    /**
     * 插件名称，展示使用，如：分布式限流、单机版限流
     */
    private String pluginName;
    /**
     * 插件类型，全局唯一，如：WhiteListPlugin、RateLimiterPlugin
     */
    private String pluginType;
    /**
     * 插件作用范围，即可绑定对象，可选值为route rule、service
     */
    private String pluginScope;
    /**
     * 插件使用说明，用于前端展示、指导用户使用
     */
    private String instructionForUse;
    /**
     * 插件表单schema，用于前端渲染表单
     */
    private String pluginSchema;


    private String categoryKey;


    private String pluginGuidance;


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


    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginInfo that = (PluginInfo) o;
        return getPluginType().equals(that.getPluginType());
    }


    @Override
    public int hashCode() {
        return Objects.hash(getPluginType());
    }
}
