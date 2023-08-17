package org.hango.cloud.envoy.infra.plugin.service;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
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
     * 查询插件信息时的参数校验（列表&详情）
     *
     * @param virtualGwId 网关id
     * @return 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkDescribePlugin(long virtualGwId);

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
