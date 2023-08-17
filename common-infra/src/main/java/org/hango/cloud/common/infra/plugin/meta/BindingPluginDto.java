package org.hango.cloud.common.infra.plugin.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述插件绑定的网关和对象等信息
 * 内含两个插件发布开关，分别对应路由启、禁用和下线场景
 *
 * @author yutao04
 * @date 2021/12/20
 */
public class BindingPluginDto extends CommonExtension implements Serializable {

    /**
     * 插件绑定的网关ID
     */
    private Long virtualGwId;
    /**
     * 插件绑定的对象类型
     */
    private String bindingObjectType;
    /**
     * 插件绑定的对象ID
     */
    private Long bindingObjectId;
    /**
     * 插件的名称
     */
    private String pluginType;
    /**
     * 插件的详细配置
     */
    private String pluginConfiguration;
    /**
     * 是否是启用路由操作场景（路由下有插件绑定，此场景启用路由需要同步发布插件）
     */
    private boolean isEnableRouteOperation = false;

    /**
     * 插件ID列表
     */
    List<Long> pluginIdList;

    public BindingPluginDto(Long virtualGwId, String bindingObjectType, Long bindingObjectId, String pluginType, String pluginConfiguration) {
        this.virtualGwId = virtualGwId;
        this.bindingObjectType = bindingObjectType;
        this.bindingObjectId = bindingObjectId;
        this.pluginType = pluginType;
        this.pluginConfiguration = pluginConfiguration;
    }

    public BindingPluginDto() {
    }

    /**
     * 根据前台对象信息创建一个业务层流转的插件对象
     *
     * @param bindingDto 前台的插件DTO
     * @return 业务层流转的插件绑定信息对象
     */
    public static BindingPluginDto createBindingPluginFromDto(PluginBindingDto bindingDto) {
        BindingPluginDto bindingPluginDto = new BindingPluginDto();
        bindingPluginDto.setPluginType(bindingDto.getPluginType());
        bindingPluginDto.setBindingObjectId(Long.parseLong(bindingDto.getBindingObjectId()));
        bindingPluginDto.setBindingObjectType(bindingDto.getBindingObjectType());
        bindingPluginDto.setVirtualGwId(bindingDto.getVirtualGwId());
        bindingPluginDto.setPluginConfiguration(bindingDto.getPluginConfiguration());
        return bindingPluginDto;
    }

    /**
     * 根据前台对象信息创建一个业务层流转的插件对象
     *
     * @param pluginBindingInfo 一个具体的绑定插件信息对象
     * @return 业务层流转的插件绑定信息对象
     */
    public static BindingPluginDto createBindingPluginFromPluginBindingInfo(PluginBindingInfo pluginBindingInfo) {
        BindingPluginDto bindingPluginDto = new BindingPluginDto();
        bindingPluginDto.setPluginType(pluginBindingInfo.getPluginType());
        bindingPluginDto.setBindingObjectId(Long.parseLong(pluginBindingInfo.getBindingObjectId()));
        bindingPluginDto.setBindingObjectType(pluginBindingInfo.getBindingObjectType());
        bindingPluginDto.setVirtualGwId(pluginBindingInfo.getVirtualGwId());
        bindingPluginDto.setPluginConfiguration(pluginBindingInfo.getPluginConfiguration());
        return bindingPluginDto;
    }

    public Long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(Long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public String getBindingObjectType() {
        return bindingObjectType;
    }

    public void setBindingObjectType(String bindingObjectType) {
        this.bindingObjectType = bindingObjectType;
    }

    public Long getBindingObjectId() {
        return bindingObjectId;
    }

    public void setBindingObjectId(Long bindingObjectId) {
        this.bindingObjectId = bindingObjectId;
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

    public boolean isEnableRouteOperation() {
        return isEnableRouteOperation;
    }

    public void setEnableRouteOperation(boolean enableRouteOperation) {
        isEnableRouteOperation = enableRouteOperation;
    }

    public List<Long> getPluginIdList() {
        return pluginIdList;
    }

    public void setPluginIdList(List<Long> pluginIdList) {
        this.pluginIdList = pluginIdList;
    }

    public void addPluginId(Long pluginId){
        if (pluginIdList == null) {
            pluginIdList = new ArrayList<>();
        }
        pluginIdList.add(pluginId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
