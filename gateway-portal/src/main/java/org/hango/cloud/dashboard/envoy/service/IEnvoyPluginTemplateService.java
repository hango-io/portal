package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCodeEnum;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginTemplateInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginTemplateDto;

import java.util.List;

/**
 * 插件模板service层接口
 *
 * @author hzchenzhongyang 2020-04-08
 */
public interface IEnvoyPluginTemplateService {
    /**
     * 创建插件模板时参数校验
     *
     * @param templateInfo 插件模板info
     * @return {@link ErrorCodeEnum} 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkCreatePluginTemplate(EnvoyPluginTemplateInfo templateInfo);

    /**
     * 创建插件模板
     *
     * @param templateInfo 插件模板info
     * @return 创建成功后的插件模板id
     */
    long createPluginTemplate(EnvoyPluginTemplateInfo templateInfo);

    /**
     * 根据id查询模板详情
     *
     * @param id 模板id
     * @return {@link EnvoyPluginTemplateInfo} 模板详情
     */
    EnvoyPluginTemplateInfo getTemplateById(long id);

    /**
     * 更新插件模板时的参数校验
     *
     * @param templateInfo 插件模板
     * @return {@link ErrorCodeEnum} 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkUpdatePluginTemplate(EnvoyPluginTemplateInfo templateInfo);

    /**
     * 更新插件模板
     *
     * @param templateInfo 插件模板
     * @return true: 更新成功 false: 更新失败
     */
    boolean updatePluginTemplate(EnvoyPluginTemplateInfo templateInfo);

    /**
     * 删除插件模板时的参数校验
     *
     * @param id 模板id
     * @return {@link ErrorCodeEnum} 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkDeletePluginTemplate(long id);

    /**
     * 删除插件模板
     *
     * @param id 模板id
     * @return true: 成功  false: 失败
     */
    boolean deletePluginTemplate(long id);

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
     * @return {@link List<EnvoyPluginTemplateInfo>} 模板列表
     */
    List<EnvoyPluginTemplateInfo> getPluginTemplateInfoList(long projectId, String pluginType, long offset, long limit);

    /**
     * 同步模板配置到插件的参数校验
     *
     * @param id                   模板id
     * @param pluginBindingInfoIds 待同步的插件列表
     * @return {@link ErrorCodeEnum} 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkSyncTemplate(long id, List<Long> pluginBindingInfoIds);

    /**
     * 同步模板配置到插件
     *
     * @param id                   模板id
     * @param pluginBindingInfoIds 待同步的插件列表
     * @return 同步失败的插件绑定关系id列表
     */
    List<EnvoyPluginBindingInfo> syncTemplate(long id, List<Long> pluginBindingInfoIds);

    List<EnvoyPluginTemplateInfo> batchGet(List<Long> templateId);

    EnvoyPluginTemplateDto fromMeta(EnvoyPluginTemplateInfo templateInfo);
}
