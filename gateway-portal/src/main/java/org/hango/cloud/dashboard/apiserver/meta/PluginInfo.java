package org.hango.cloud.dashboard.apiserver.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2019/1/17 下午3:52.
 */
public class PluginInfo implements Serializable {

    private static final long serialVersionUID = -8416621426808698423L;

    private long id;

    private long createDate;

    private long modifyDate;

    private long gwId;

    /**
     * 插件名称
     */
    private String pluginName;

    /**
     * 插件版本
     */
    private String pluginVersion;

    /**
     * 上传的插件文件名称
     */
    private String pluginFileName;

    /**
     * 插件文件内容
     */
    private String pluginContent;

    /**
     * 插件变量集合
     */
    private String pluginVariable;

    /**
     * 插件状态，其中1表示开启，0表示关闭
     */
    private int pluginStatus;

    /**
     * 插件最近一次启动时间
     */
    private long lastStartTime;

    /**
     * 插件最近一次启动时长
     */
    private long pluginStartingTime;

    /**
     * 插件总的被调用次数
     */
    private long pluginCallNumber;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public String getPluginFileName() {
        return pluginFileName;
    }

    public void setPluginFileName(String pluginFileName) {
        this.pluginFileName = pluginFileName;
    }

    public String getPluginContent() {
        return pluginContent;
    }

    public void setPluginContent(String pluginContent) {
        this.pluginContent = pluginContent;
    }

    public String getPluginVariable() {
        return pluginVariable;
    }

    public void setPluginVariable(String pluginVariable) {
        this.pluginVariable = pluginVariable;
    }

    public int getPluginStatus() {
        return pluginStatus;
    }

    public void setPluginStatus(int pluginStatus) {
        this.pluginStatus = pluginStatus;
    }

    public long getPluginStartingTime() {
        return pluginStartingTime;
    }

    public void setPluginStartingTime(long pluginStartingTime) {
        this.pluginStartingTime = pluginStartingTime;
    }

    public long getPluginCallNumber() {
        return pluginCallNumber;
    }

    public void setPluginCallNumber(long pluginCallNumber) {
        this.pluginCallNumber = pluginCallNumber;
    }

    public long getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(long lastStartTime) {
        this.lastStartTime = lastStartTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
