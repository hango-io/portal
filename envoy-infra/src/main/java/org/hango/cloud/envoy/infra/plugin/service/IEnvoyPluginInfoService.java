package org.hango.cloud.envoy.infra.plugin.service;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.dto.PluginDto;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.Operation;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;

import javax.validation.constraints.NotNull;
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
    PluginDto getPluginInfoFromApiPlane(long virtualGwId, String pluginType);

    /**
     * 调用api-plane的接口获取全量插件列表（不包含schema）
     *
     * @param virtualGwId 网关id
     * @return {@link List<  PluginDto  >} 插件详情列表
     */
    List<PluginDto> getPluginInfoListFromApiPlane(long virtualGwId, String pluginScope);

    boolean createPluginAndMakeRequest(BindingPluginDto bindingPluginDto, Operation operation, List<String> toBePublishedPluginList);

    List<String> createToBePublishedPluginList(BindingPluginDto bindingPluginInfo, Operation operation, List<Long> pluginIdList);

    /**
     * 发布网关插件
     *
     * @param bindingPluginDto 网关插件聚合信息对象
     * @return 是否操作成功
     */
    boolean publishGatewayPlugin(BindingPluginDto bindingPluginDto);

    /**
     * 更新指定ID的网关插件
     *
     * @param bindingPluginDto 网关插件聚合信息对象
     * @param pluginId         需要被更新的插件ID
     * @return 是否操作成功
     */
    boolean updateGatewayPlugin(BindingPluginDto bindingPluginDto, long pluginId);

    /**
     * 删除指定ID的网关插件
     *
     * @param bindingPluginDto 网关插件聚合信息对象
     * @param pluginId         需要被删除的插件ID
     * @return 是否操作成功
     */
    boolean deleteGatewayPlugin(BindingPluginDto bindingPluginDto, long pluginId);

    /**
     * 删除指定ID列表的网关插件
     *
     * @param bindingPluginDto 网关插件聚合信息对象
     * @param pluginIdList     需要被删除的插件ID集合
     * @return 是否操作成功
     */
    boolean deleteGatewayPlugin(BindingPluginDto bindingPluginDto, List<Long> pluginIdList);

    /**
     * 根据pluginType调用api-plane查询插件信息
     * @param pluginType
     * @return
     */
    PluginInfo getPluginInfoByPluginType(String pluginType);
}
