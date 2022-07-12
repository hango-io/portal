package org.hango.cloud.dashboard.envoy.meta;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginBindingDto;

/**
 * 描述插件绑定的网关和对象等信息
 * 内含两个插件发布开关，分别对应路由启、禁用和下线场景
 *
 * @author yutao04
 * @date 2021/12/20
 */
public class BindingPluginInfo {
    /**
     * 路由级别插件标识
     */
    public static final String PLUGIN_TYPE_ROUTE = "routeRule";
    /**
     * 全局（项目）级别插件标识
     */
    public static final String PLUGIN_TYPE_GLOBAL = "global";
    /**
     * 插件绑定的网关ID
     */
    private Long gwId;
    /**
     * 插件绑定的对象类型（是项目还是路由）
     */
    private String bindingObjectType;
    /**
     * 插件绑定的对象ID（若绑定对象是路由则是路由ID；项目则是项目ID）
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
     * 仅拷贝路由流程用到，其他场景为空
     */
    private String destGatewayId = "";

    public BindingPluginInfo(Long gwId, String bindingObjectType, Long bindingObjectId, String pluginType, String pluginConfiguration) {
        this.gwId = gwId;
        this.bindingObjectType = bindingObjectType;
        this.bindingObjectId = bindingObjectId;
        this.pluginType = pluginType;
        this.pluginConfiguration = pluginConfiguration;
    }

    public BindingPluginInfo() {
    }

    /**
     * 根据前台对象信息创建一个业务层流转的插件对象
     *
     * @param bindingDto 前台的插件DTO
     * @return 业务层流转的插件绑定信息对象
     */
    public static BindingPluginInfo createBindingPluginFromDto(EnvoyPluginBindingDto bindingDto) {
        BindingPluginInfo bindingPluginInfo = new BindingPluginInfo();
        bindingPluginInfo.setPluginType(bindingDto.getPluginType());
        bindingPluginInfo.setBindingObjectId(Long.parseLong(bindingDto.getBindingObjectId()));
        bindingPluginInfo.setBindingObjectType(bindingDto.getBindingObjectType());
        bindingPluginInfo.setGwId(bindingDto.getGwId());
        bindingPluginInfo.setPluginConfiguration(bindingDto.getPluginConfiguration());
        return bindingPluginInfo;
    }

    /**
     * 根据前台对象信息创建一个业务层流转的插件对象
     *
     * @param pluginBindingInfo 一个具体的绑定插件信息对象
     * @return 业务层流转的插件绑定信息对象
     */
    public static BindingPluginInfo createBindingPluginFromEnvoyPluginBindingInfo(EnvoyPluginBindingInfo pluginBindingInfo) {
        BindingPluginInfo bindingPluginInfo = new BindingPluginInfo();
        bindingPluginInfo.setPluginType(pluginBindingInfo.getPluginType());
        bindingPluginInfo.setBindingObjectId(Long.parseLong(pluginBindingInfo.getBindingObjectId()));
        bindingPluginInfo.setBindingObjectType(pluginBindingInfo.getBindingObjectType());
        bindingPluginInfo.setGwId(pluginBindingInfo.getGwId());
        bindingPluginInfo.setPluginConfiguration(pluginBindingInfo.getPluginConfiguration());
        return bindingPluginInfo;
    }

    public Long getGwId() {
        return gwId;
    }

    public void setGwId(Long gwId) {
        this.gwId = gwId;
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

    public String getDestGatewayId() {
        return destGatewayId;
    }

    public void setDestGatewayId(String destGatewayId) {
        this.destGatewayId = destGatewayId;
    }

    /**
     * 根据路由标识判断该插件是否为路由级别插件
     *
     * @return 是否为路由级别插件（true: 路由级别；false: 非路由级别）
     */
    public boolean isRoutePlugin() {
        return bindingObjectType.equals(PLUGIN_TYPE_ROUTE);
    }

    /**
     * 根据项目标识判断该插件是否为全局（项目级别）插件
     *
     * @return 是否为全局插件（true: 全局级别；false: 非全局级别）
     */
    public boolean isGlobalPlugin() {
        return bindingObjectType.equals(PLUGIN_TYPE_GLOBAL);
    }

    /**
     * 根据目标网关ID（destGatewayId）值是否为空判断当前是否是拷贝路由流程
     *
     * @return 是否为空判断当前是否是拷贝路由流程
     */
    public boolean isCopyRoute() {
        return StringUtils.isNotEmpty(this.destGatewayId);
    }

    /**
     * 判断是否符合发布插件的规则
     *
     * @param enableState 路由的使能状态
     * @return 是否符合发布插件的规则
     */
    public boolean canPublishPlugin(String enableState) {
        return Const.ROUTE_RULE_ENABLE_STATE.equals(enableState);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
