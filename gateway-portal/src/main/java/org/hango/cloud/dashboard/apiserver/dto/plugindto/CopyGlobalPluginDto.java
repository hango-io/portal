package org.hango.cloud.dashboard.apiserver.dto.plugindto;

import com.alibaba.fastjson.annotation.JSONField;

import javax.validation.constraints.Min;

/**
 * @author yutao04
 * @date 2022/2/18 15:03
 */
public class CopyGlobalPluginDto {
    /**
     * 源插件
     */
    @Min(value = 0, message = "PluginId Range Error")
    @JSONField(name = "PluginId")
    private Long pluginId;

    /**
     * 目标网关ID
     */
    @Min(value = 0, message = "GwId Range Error")
    @JSONField(name = "GwId")
    private Long gwId;

    @Min(value = 0, message = "ProjectId Range Error")
    @JSONField(name = "ProjectId")
    private Long projectId;

    /**
     * 拷贝完成后期望插件的状态
     */
    @JSONField(name = "IsEnable")
    private Boolean isEnable;

    public Long getPluginId() {
        return pluginId;
    }

    public void setPluginId(Long pluginId) {
        this.pluginId = pluginId;
    }

    public Long getGwId() {
        return gwId;
    }

    public void setGwId(Long gwId) {
        this.gwId = gwId;
    }

    public Boolean getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(Boolean enable) {
        isEnable = enable;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "CopyGlobalPluginDto{" +
                "pluginId=" + pluginId +
                ", gwId=" + gwId +
                ", projectId=" + projectId +
                ", isEnable=" + isEnable +
                '}';
    }
}
