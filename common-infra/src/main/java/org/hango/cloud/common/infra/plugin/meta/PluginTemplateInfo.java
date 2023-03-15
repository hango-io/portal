package org.hango.cloud.common.infra.plugin.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

import java.io.Serializable;

/**
 * 插件模板info类
 *
 * @author hzchenzhongyang 2020-04-08
 */
public class PluginTemplateInfo extends CommonExtension implements Serializable {
    /**
     * 无需同步
     **/
    public static final int STATUS_NO_NEED_SYNC = 0;
    /**
     * 未完全同步
     **/
    public static final int STATUS_INCOMPLETE_SYNC = 1;
    /**
     * 未同步
     **/
    public static final int STATUS_NEED_SYNC = 2;
    /**
     * id
     */
    private long id;
    /**
     * 模板创建时间，时间戳格式，精确到毫秒
     */
    private long createTime;
    /**
     * 模板最后更新时间，时间戳格式，精确到毫秒
     */
    private long updateTime;
    /**
     * 绑定的插件类型，全局唯一，如：RateLimiter、WhiteList等
     */
    private String pluginType;
    /**
     * 插件配置
     */
    private String pluginConfiguration;
    /**
     * 插件绑定关系所属项目id
     */
    private long projectId;
    /**
     * 插件模板版本
     */
    private long templateVersion;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 模板备注
     */
    private String templateNotes;

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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
