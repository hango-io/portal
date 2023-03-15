package org.hango.cloud.common.infra.plugin.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

import java.io.Serializable;

/**
 * 插件绑定关系meta
 *
 * @author hzchenzhongyang 2019-11-11
 */
public class PluginBindingInfo extends CommonExtension implements Serializable {
    public static final String BINDING_OBJECT_TYPE_ROUTE_RULE = "routeRule";
    public static final String BINDING_OBJECT_TYPE_SERVICE = "service";
    public static final String BINDING_OBJECT_TYPE_GLOBAL = "global";
    public static final String BINDING_OBJECT_TYPE_HOST = "host";

    public static final String BINDING_STATUS_ENABLE = "enable";
    public static final String BINDING_STATUS_DISABLE = "disable";

    /**
     * 表自增id
     */
    private long id;
    /**
     * 对象-插件绑定关系作用的网关id
     */
    private long virtualGwId;
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
     * 绑定关系创建时间，时间戳格式，精确到毫秒
     */
    private long createTime;
    /**
     * 绑定关系更新时间，时间戳格式，精确到毫秒
     */
    private long updateTime;
    /**
     * 绑定插件类型
     */
    private String pluginType;
    /**
     * 绑定关系所属项目id
     */
    private long projectId;
    /**
     * 状态，disable/enable，disable时插件在数据面不生效
     */
    private String bindingStatus;
    /**
     * 关联模板id，若未关联则为0
     */
    private long templateId;
    /**
     * 关联插件模板版本号，若与模板当前版本号不一致则可以通过同步实现
     */
    private long templateVersion;

    /**
     * 网关类型
     */
    private String gwType;

    /**
     * 版本号
     */
    private long version;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public String getBindingObjectId() {
        return bindingObjectId;
    }

    public void setBindingObjectId(String bindingObjectId) {
        this.bindingObjectId = bindingObjectId;
    }

    public String getBindingObjectType() {
        return bindingObjectType;
    }

    public void setBindingObjectType(String bindingObjectType) {
        this.bindingObjectType = bindingObjectType;
    }

    public String getPluginConfiguration() {
        return pluginConfiguration;
    }

    public void setPluginConfiguration(String pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
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

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getBindingStatus() {
        return bindingStatus;
    }

    public void setBindingStatus(String bindingStatus) {
        this.bindingStatus = bindingStatus;
    }

    public long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(long templateId) {
        this.templateId = templateId;
    }

    public long getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(long templateVersion) {
        this.templateVersion = templateVersion;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
