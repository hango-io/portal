package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * 插件模板Dto
 *
 * @author hzchenzhongyang 2020-04-08
 */
public class PluginTemplateDto extends CommonExtensionDto implements Serializable {
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
    @NotEmpty(message = "插件类型不能为空")
    @Pattern(regexp = "([\\s\\S]){1,254}", message = "插件类型长度不能超过255")
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
    @NotEmpty(message = "插件配置不能为空")
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
    @NotEmpty(message = "插件类型不能为空")
    @JSONField(name = "TemplateName")
    @Pattern(regexp = "([\\s\\S]){1,64}", message = "插件模板名称长度不能超过64")
    private String templateName;
    /**
     * 模板备注
     */
    @JSONField(name = "TemplateNotes")
    @Pattern(regexp = "([\\s\\S]){0,200}", message = "插件模板描述长度不能超过200")
    private String templateNotes;

    /**
     * 插件绑定信息
     */
    @JSONField(name = "PluginBindings")
    private List<PluginBindingDto> bindingDtoList;

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

    public List<PluginBindingDto> getBindingDtoList() {
        return bindingDtoList;
    }

    public void setBindingDtoList(List<PluginBindingDto> bindingDtoList) {
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

    public PluginTemplateInfo toMeta() {
        PluginTemplateInfo templateInfo = new PluginTemplateInfo();
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
