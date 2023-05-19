package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 网关插件绑定关系dto
 *
 * @author hzchenzhongyang 2019-11-13
 */
public class PluginBindingDto extends CommonExtensionDto implements Serializable, Comparable<PluginBindingDto> {
    /**
     * 表自增id
     */
    @JSONField(name = "PluginBindingInfoId")
    private long id;
    /**
     * 对象-插件绑定关系作用的网关id
     */
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;
    /**
     * 插件绑定对象id，与bindingObjectType共同确定一个具体对象
     */
    @JSONField(name = "BindingObjectId")
    @NotNull
    private String bindingObjectId;
    /**
     * 插件绑定对象类型，与bindingObjectId共同确定一个具体对象
     */
    @Pattern(regexp = "routeRule|service|global|host", message = "插件范围仅支持routeRule/service/global/host")
    @NotNull
    @JSONField(name = "BindingObjectType")
    private String bindingObjectType;
    /**
     * 插件配置
     */
    @JSONField(name = "PluginConfiguration")
    private String pluginConfiguration;
    /**
     * 绑定关系创建时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "CreateTime")
    private long createTime;
    /**
     * 绑定关系更新时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;
    /**
     * 绑定插件类型
     */
    @JSONField(name = "PluginType")
    private String pluginType;
    /**
     * 绑定关系所属项目id
     */
    @JSONField(name = "ProjectId")
    private long projectId;
    /**
     * 插件优先级
     */
    @JSONField(name = "PluginPriority")
    private long pluginPriority;
    /**
     * 状态，disable/enable，disable时插件在数据面不生效
     */
    @JSONField(name = "BindingStatus")
    private String bindingStatus;

    @JSONField(name = "PluginName")
    private String pluginName;

    @JSONField(name = "BindingObjectName")
    private String bindingObjectName;

    @JSONField(name = "GwName")
    private String gwName;

    @JSONField(name = "TemplateId")
    private long templateId;

    @JSONField(name = "TemplateVersion")
    private long templateVersion;

    @JSONField(name = "TemplateStatus")
    private long templateStatus;

    @JSONField(name = "GwType")
    private String gwType;
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

    public long getPluginPriority() {
        return pluginPriority;
    }

    public void setPluginPriority(long pluginPriority) {
        this.pluginPriority = pluginPriority;
    }

    public String getBindingStatus() {
        return bindingStatus;
    }

    public void setBindingStatus(String bindingStatus) {
        this.bindingStatus = bindingStatus;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getBindingObjectName() {
        return bindingObjectName;
    }

    public void setBindingObjectName(String bindingObjectName) {
        this.bindingObjectName = bindingObjectName;
    }

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
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

    public long getTemplateStatus() {
        return templateStatus;
    }

    public void setTemplateStatus(long templateStatus) {
        this.templateStatus = templateStatus;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
    }

    public PluginBindingDto(Long virtualGwId, String bindingObjectType, String bindingObjectId, String pluginType, String pluginConfiguration) {
        this.virtualGwId = virtualGwId;
        this.bindingObjectType = bindingObjectType;
        this.bindingObjectId = bindingObjectId;
        this.pluginType = pluginType;
        this.pluginConfiguration = pluginConfiguration;
    }

    public PluginBindingDto() {
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * 排序规则：先按照优先级排序，高优先级的排序靠前；统一优先级再按照绑定时间排序（同一个插件允许重复绑定），创建时间靠前的排序靠前
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(PluginBindingDto o) {
        if (null == o) {
            throw new NullPointerException("the specified object is null");
        }
        // 比较的两个对象不存在equals的情况

        if (this.createTime > o.createTime) {
            return 1;
        }
        if (this.createTime < o.createTime) {
            return -1;
        }
        return 0;
    }
}
