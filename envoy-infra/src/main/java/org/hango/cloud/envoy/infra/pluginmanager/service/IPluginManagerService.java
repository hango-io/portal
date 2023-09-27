package org.hango.cloud.envoy.infra.pluginmanager.service;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.plugin.dto.CustomPluginPublishDTO;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginManagerDto;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderDto;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/23
 */
public interface IPluginManagerService {

    /**
     * 获取插件配置
     *
     * @param virtualGwId
     * @return
     */
    List<PluginManagerDto> getPluginManager(long virtualGwId);

    /**
     * 校验网关修改配置参数
     *
     * @param virtualGwId
     * @param name
     * @param enable
     * @return
     */
    ErrorCode checkPluginManager(long virtualGwId, String name, boolean enable);

    /**
     * 更新插件状态
     */
    boolean updatePluginStatus(long virtualGwId, String name, boolean enable);

    /**
     * 上线自定义插件
     */
    boolean onlineCustomPlugin(long virtualGwId, CustomPluginPublishDTO customPluginPublishDTO);

    /**
     * 下线自定义插件
     */
    boolean offlineCustomPlugin(long virtualGwId, CustomPluginPublishDTO customPluginPublishDTO);

    /**
     * 更新数据面插件开关配置（插件开关数据以原有模板为准）
     *
     * @param virtualGatewayDto
     * @return
     */
    boolean publishPluginManager(VirtualGatewayDto virtualGatewayDto);

    /**
     * 删除数据面插件开关配置
     *
     * @param virtualGatewayDto
     * @return
     */
    boolean offlinePluginManager(VirtualGatewayDto virtualGatewayDto);

    /**
     * 重新排序plm资源
     */
    boolean resortPluginManager(String confAddr, List<String> names);

    /**
     * 从数据面获取插件配置
     */
    PluginOrderDto getPluginOrder(Long vgId);


    Boolean updateCustomPluginStatus(VirtualGatewayDto virtualGatewayDto, CustomPluginInfo customPluginInfo);
}
