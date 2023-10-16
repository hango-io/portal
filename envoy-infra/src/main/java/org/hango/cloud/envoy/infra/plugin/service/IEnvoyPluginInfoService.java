package org.hango.cloud.envoy.infra.plugin.service;

import org.hango.cloud.common.infra.plugin.dto.PluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;

import java.util.List;

/**
 * @author xin li
 * @date 2022/9/21 15:08
 */
public interface IEnvoyPluginInfoService {

    /**
     * 调用api-plane的接口根据插件类型获取插件详情（含schema）
     *
     * @param virtualGwId       网关id
     * @param pluginType
     * @return
     */
    PluginDto getPluginInfo(long virtualGwId, String pluginType);


    /**
     * 获取插件列表，包含系统插件和自定义插件
     */
    List<PluginDto> getPluginInfoList(long virtualGwId, String pluginScope);

    /**
     * 查看所有系统插件
     *
     * @param virtualGateway 虚拟网关
     * @return 是否操作成功
     */
    List<PluginInfo> getSystemPluginInfos(VirtualGatewayDto virtualGateway);

}
