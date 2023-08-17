package org.hango.cloud.common.infra.plugin.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCodeEnum;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingQueryDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;

import java.util.List;

/**
 * 插件Service层接口
 * <p>
 * 当前插件的元信息存储在api-plane侧，暂不提供插件元信息的添加、更新、删除接口
 *
 * @author hzchenzhongyang 2019-10-23
 */
public interface IPluginInfoService extends CommonService<PluginBindingInfo, PluginBindingDto> {

    /**
     * 查询插件信息时的参数校验（列表&详情）
     *
     * @param virtualGwId 网关id
     * @return 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkDescribePlugin(long virtualGwId);

    /**
     * 校验删除参数
     *
     * @param pluginBindingInfoId
     * @return
     */
    ErrorCode checkUnbindParam(long pluginBindingInfoId);

    /**
     * 检查拷贝插件到指定网关的数据合法性
     *
     * @param copyGlobalPluginDto 拷贝全局插件对象
     * @return 错误码
     */
    ErrorCode checkCopyGlobalPluginToGateway(CopyGlobalPluginDto copyGlobalPluginDto);



    /**
     * 修改插件绑定关系状态时的参数校验
     *
     * @param pluginBindingInfoId 插件绑定关系id
     * @param bindingStatus       更新后状态
     *                            // TODO FIXME 修改return注释
     * @return {@link ErrorCodeEnum#SUCCESS} ErrorCodeEnum.Success
     */
    ErrorCode checkUpdatePluginBindingStatus(long pluginBindingInfoId, String bindingStatus);

    /**
     * 拷贝全局插件到指定网关
     *
     * @param copyGlobalPluginDto 拷贝全局插件对象
     * @return 是否拷贝成功
     */
    boolean copyGlobalPluginToGatewayByVirtualGwId(CopyGlobalPluginDto copyGlobalPluginDto);

    /**
     * 校验查询已绑定插件信息参数
     *
     * @param bindingObjectId
     * @param bindingObjectType
     * @return
     */
    ErrorCode checkDescribeBindingPlugins(String bindingObjectId, String bindingObjectType);

    /**
     * 查询当前对象上生效的插件绑定关系列表（仅含状态为enable的插件列表）
     */
    List<PluginBindingInfo> getEnablePluginBindingList(long virtualGwId, Long bindingObjectId, String bindingObjectType);

    /**
     * 查询当前对象上的插件绑定关系列表（包含状态为enable、disable的插件列表）
     */
    List<PluginBindingDto> getPluginBindingList(long virtualGwId, Long bindingObjectId, String bindingObjectType);


    /**
     * 分页查询插件（排除指定的内部插件）
     */
    Page<PluginBindingInfo> getOutsideBindingPluginPage(PluginBindingQueryDto query);

    /**
     * 查询插件列表
     */
    List<PluginBindingDto> getBindingPluginInfoList(PluginBindingInfoQuery query);
}
