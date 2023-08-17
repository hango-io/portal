package org.hango.cloud.common.infra.plugin.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfoQuery;

import java.util.List;

/**
 * 插件模板service层接口
 *
 * @author hzchenzhongyang 2020-04-08
 */
public interface IPluginTemplateService extends CommonService<PluginTemplateInfo, PluginTemplateDto> {

    /**
     * 获取模板信息列表
     */
    List<PluginTemplateDto> getPluginTemplateInfoList(PluginTemplateInfoQuery query);

    /**
     * 分页查询模板信息
     */
    Page<PluginTemplateInfo> getPluginTemplateInfoPage(PluginTemplateInfoQuery query);

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


}
