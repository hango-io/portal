package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginTemplateInfo;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 插件模板Dto
 *
 * @author hzchenzhongyang 2020-04-08
 */
public class EnvoyPluginTemplateDto {
    /**
     * id
     */
    @JSONField(name = "Id")
    private long id;
    /**
     * 模板创建时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "CreateTime")
    private long createTime;
    /**
     * 模板最后更新时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;
    /**
     * 绑定的插件类型，全局唯一，如：RateLimiter、WhiteList等
     */
    @NotEmpty
    @Pattern(regexp = "([\\s\\S]){1,254}", message = "长度不能超过255")
    @JSONField(name = "PluginType")
    private String pluginType;

    /**
     * 插件名称，仅做展示
     */
    @JSONField(name = "PluginName")
    private String PluginName;
    /**
     * 插件配置
     */
    @NotEmpty
    @JSONField(name = "PluginConfiguration")
    private String pluginConfiguration;
    /**
     * 插件绑定关系所属项目id
     */
    @JSONField(name = "ProjectId")
    private long projectId;
    /**
     * 插件模板状态
     */
    @JSONField(name = "TemplateStatus")
    private int templateStatus;
    /**
     * 插件模板版本
     */
    @JSONField(name = "TemplateVersion")
    private long templateVersion;
    /**
     * 模板名称
     */
    @NotEmpty
    @JSONField(name = "TemplateName")
    @Pattern(regexp = "([\\s\\S]){1,254}")
    private String templateName;
    /**
     * 模板备注
     */
    @JSONField(name = "TemplateNotes")
    @Pattern(regexp = "([\\s\\S]){0,254}")
    private String templateNotes;

    @JSONField(name = "EnvoyPluginBindingDtos")
    private List<EnvoyPluginBindingDto> bindingDtoList;

    /**
     * 是否为全局模版，true代表全局模版，false代表项目模版
     */
    @JSONField(name = "IsGlobal")
    private boolean isGlobal = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getPluginConfiguration() {
        return pluginConfiguration;
    }

    public void setPluginConfiguration(String pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public int getTemplateStatus() {
        return templateStatus;
    }

    public void setTemplateStatus(int templateStatus) {
        this.templateStatus = templateStatus;
    }

    public long getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(long templateVersion) {
        this.templateVersion = templateVersion;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateNotes() {
        return templateNotes;
    }

    public void setTemplateNotes(String templateNotes) {
        this.templateNotes = templateNotes;
    }

    public List<EnvoyPluginBindingDto> getBindingDtoList() {
        return bindingDtoList;
    }

    public void setBindingDtoList(List<EnvoyPluginBindingDto> bindingDtoList) {
        this.bindingDtoList = bindingDtoList;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public String getPluginName() {
        return PluginName;
    }

    public void setPluginName(final String pluginName) {
        PluginName = pluginName;
    }

    public EnvoyPluginTemplateInfo toMeta() {
        EnvoyPluginTemplateInfo templateInfo = new EnvoyPluginTemplateInfo();
        templateInfo.setId(getId());
        templateInfo.setTemplateNotes(getTemplateNotes());
        templateInfo.setProjectId(getProjectId());
        templateInfo.setPluginType(getPluginType());
        templateInfo.setUpdateTime(getUpdateTime());
        templateInfo.setCreateTime(getCreateTime());
        templateInfo.setTemplateName(getTemplateName());
        templateInfo.setTemplateVersion(getTemplateVersion());
        templateInfo.setPluginConfiguration(getPluginConfiguration());
        return templateInfo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
