package org.hango.cloud.envoy.infra.plugin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.dto.PluginUpdateDto;
import org.hango.cloud.common.infra.plugin.dto.UpdatePluginStatusDto;
import org.hango.cloud.envoy.infra.plugin.dto.*;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;

public interface CustomPluginInfoService {
    /**
     * 根据插件名称获取插件信息
     *
     * @param customPluginInfoDto 插件信息
     * @return Boolean
     */
    ErrorCode checkPluginImportParameter(CustomPluginInfoDto customPluginInfoDto);


    /**
     * 校验自定义插件更新参数
     */
    ErrorCode checkUpdateCustomPlugin(PluginUpdateDto pluginUpdateDto);


    /**
     * 校验自定义插件上下架参数
     */
    ErrorCode checkUpdatePluginStatus(UpdatePluginStatusDto updatePluginStatusDto);
    /**
     * 根据插件名称获取插件信息
     *
     * @param customPluginInfoDto 插件信息
     * @return 插件ID
     */
    Long pluginImport(CustomPluginInfoDto customPluginInfoDto);
    /**
     * 根据插件名称获取插件信息
     *
     * @param pluginUpdateDto 修改插件信息
     * @return 插件ID
     */
    Integer pluginUpdate(PluginUpdateDto pluginUpdateDto);
    /**
     * 上下架插件
     *
     * @param updatePluginStatusDto 上下架插件信息
     * @return 插件ID
     */
    ErrorCode updatePluginStatus(UpdatePluginStatusDto updatePluginStatusDto);

    /**
     * 删除插件参数检查
     */
    ErrorCode checkDeletePlugin(Long id);

    /**
     * 删除插件
     */
    ErrorCode deletePlugin(Long id);
    /**
     * 根据插件ID查询插件详情
     *
     * @param pluginId 插件ID
     * @return 插件ID
     */
    DescribeCustomPluginDto describeCustomPluginInfo(Long pluginId);

    /**
     * 查询插件列表
     *
     * @param customPluginQueryDto 查询插件列表
     * @return 插件ID
     */
    Page<DescribeCustomPluginDto> getCustomPluginList(CustomPluginQueryDto customPluginQueryDto);

    /**
     * 查询插件实例列表
     *
     * @param customPluginInstanceListQueryDto 查询插件实例列表
     * @return 插件ID
     */
    Page<CustomPluginInstanceDto> getCustomPluginInstancePage(CustomPluginInstanceListQueryDto customPluginInstanceListQueryDto);


    /**
     * 查询插件
     */
    CustomPluginInfo getCustomPlugin(String pluginType);
}
