package org.hango.cloud.common.infra.plugin.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;

import java.util.List;

/**
 * 插件模板service层接口
 *
 * @author hzchenzhongyang 2020-04-08
 */
public interface IPluginTemplateService extends CommonService<PluginTemplateInfo, PluginTemplateDto> {

    /**
     * 查询指定项目下的模板列表数量
     *
     * @param projectId  项目id
     * @param pluginType 插件类型
     * @return 插件模板数量
     */
    long getPluginTemplateInfoCount(long projectId, String pluginType);

    /**
     * 查询指定项目下的模板列表
     *
     * @param projectId  项目id
     * @param pluginType 插件类型
     * @param offset     分页参数offset
     * @param limit      分页参数limit
     * @return {@link List<PluginTemplateDto>} 模板列表
     */
    List<PluginTemplateDto> getPluginTemplateInfoList(long projectId, String pluginType, long offset, long limit);

    /**
     * 同步模板配置到插件的参数校验
     *
     * @param id                   模板id
     * @param pluginBindingInfoIds 待同步的插件列表
     * @return 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkSyncTemplate(long id, List<Long> pluginBindingInfoIds);

    /**
     * 同步模板配置到插件
     *
     * @param id                   模板id
     * @param pluginBindingInfoIds 待同步的插件列表
     * @return 同步失败的插件绑定关系id列表
     */
    List<PluginBindingDto> syncTemplate(long id, List<Long> pluginBindingInfoIds);

    /**
     * 批量获取插件模板
     * @param templateIdList 模板id
     * @return 插件模板
     */
    List<PluginTemplateDto> batchGet(List<Long> templateIdList);

    /**
     * 根据插件类型获取插件模板
     *
     * @param pluginType
     * @return
     */
    List<PluginTemplateInfo> getPluginTemplateByType(String pluginType);
}
