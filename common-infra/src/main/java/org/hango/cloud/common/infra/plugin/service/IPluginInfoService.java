package org.hango.cloud.common.infra.plugin.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCodeEnum;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;

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
     * 查询当前对象上生效的插件绑定关系列表（仅含状态为enable的插件列表）
     *
     * @param virtualGwId       网关id
     * @param bindingObjectId   插件绑定对象id
     * @param bindingObjectType 插件绑定对象类型，routeRule/service/global
     * @return {@link List<PluginBindingInfo>} 绑定关系列表
     */
    List<PluginBindingInfo> getEnablePluginBindingList(long virtualGwId, String bindingObjectId, String bindingObjectType);

    /**
     * 查询当前对象上的插件绑定关系列表（包含状态为enable、disable的插件列表）
     *
     * @param virtualGwId       网关id
     * @param bindingObjectId   插件绑定对象id
     * @param bindingObjectType 插件绑定对象类型，routeRule/service/global
     * @return {@link List<PluginBindingDto>} 绑定关系列表
     */
    List<PluginBindingDto> getPluginBindingList(long virtualGwId, String bindingObjectId, String bindingObjectType);


    /**
     * 查询当前对象上生效的插件绑定关系列表（仅含状态为enable的插件列表）
     *
     * @param virtualGwId       网关id
     * @return {@link List<PluginBindingInfo>} 绑定关系列表
     */
    List<PluginBindingInfo> getEnablePluginBindingList(long virtualGwId);

    /**
     * 查询某对象上绑定的插件数量
     *
     * @param virtualGwId       网关id
     * @param projectId         项目id
     * @param bindingObjectId   绑定对象id 非必填
     * @param bindingObjectType 绑定对象类型列表 非必填
     * @param pattern           绑定插件列表模糊匹配。包括网关名称、绑定对象（服务、路由）名称、插件名称（类型）
     * @param excludedPluginTypeList  需要排除的插件类型集合
     * @return 指定对象上绑定的插件数量
     */
    long getBindingPluginCount(long virtualGwId, long projectId, String bindingObjectId, String bindingObjectType, String pattern, List<String> excludedPluginTypeList);

    /**
     * 查询某对象上绑定的插件数量（排除指定的内部插件）
     *
     * @param virtualGwId       网关id
     * @param projectId         项目id
     * @param bindingObjectId   绑定对象id 非必填
     * @param bindingObjectType 绑定对象类型列表 非必填
     * @param pattern           绑定插件列表模糊匹配。包括网关名称、绑定对象（服务、路由）名称、插件名称（类型）
     * @return
     */
    long getBindingPluginCountExcludedInnerPlugins(long virtualGwId, long projectId, String bindingObjectId, String bindingObjectType, String pattern);
    /**
     * 查询某对象上绑定的插件列表，过滤内置插件
     *
     * @param virtualGwId       网关id
     * @param projectId         项目id
     * @param bindingObjectId   绑定对象id 非必填
     * @param bindingObjectType 绑定对象类型 非必填
     * @param pattern           绑定插件列表模糊匹配。包括网关名称、绑定对象（服务、路由）名称、插件名称（类型）
     * @param offset            分页查询参数offset
     * @param limit             分页查询参数limit
     * @param sortKey           排序字段名称
     * @param sortValue         排序方式 desc/asc
     * @return {@link List<PluginBindingInfo>} 指定对象上绑定的插件列表（未排序）
     */
    List<PluginBindingDto> getBindingPluginListOutSide(long virtualGwId, long projectId, String bindingObjectId, String bindingObjectType, String pattern, long offset, long limit, String sortKey, String sortValue);

    /**
     * 查询某对象上绑定的插件列表
     *
     * @param virtualGwId       网关id
     * @param projectId         项目id
     * @param bindingObjectId   绑定对象id 非必填
     * @param bindingObjectType 绑定对象类型 非必填
     * @param pattern           绑定插件列表模糊匹配。包括网关名称、绑定对象（服务、路由）名称、插件名称（类型）
     * @param offset            分页查询参数offset
     * @param limit             分页查询参数limit
     * @param sortKey           排序字段名称
     * @param sortValue         排序方式 desc/asc
     * @return {@link List< PluginBindingInfo >} 指定对象上绑定的插件列表（未排序）
     */
    List<PluginBindingDto> getBindingPluginList(long virtualGwId, long projectId, String bindingObjectId, String bindingObjectType, String pattern, long offset, long limit, String sortKey, String sortValue);

    /**
     * 删除指定对象绑定的插件列表（仅删除插件绑定表中数据，不调用api-plane地址进行更新）
     *
     * @param virtualGwId       网关id
     * @param bindingObjectId   绑定对象id
     * @param bindingObjectType 绑定对象类型
     * @return 删除绑定关系记录条数
     */
    long deletePluginList(long virtualGwId, String bindingObjectId, String bindingObjectType);

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
     * 根据插件模板id查询关联的插件绑定列表
     *
     * @param templateId 插件模板id
     * @return {@link List<PluginBindingDto>} 插件绑定列表
     */
    List<PluginBindingDto> getBindingListByTemplateId(long templateId);

    /**
     * 批量解除插件绑定中关联的插件模板
     *
     * @param bindingInfoList 插件绑定关系id列表
     * @return true: 解除插件模板关联成功  false: 解除插件模板关联失败
     */
    boolean batchDissociateTemplate(List<Long> bindingInfoList);

    /**
     * 根据插件绑定关系id列表批量查询详情
     *
     * @param bindingInfoIdList 插件绑定关系id列表
     * @return {@link List<PluginBindingDto>} 插件绑定关系列表
     */
    List<PluginBindingDto> batchGetById(List<Long> bindingInfoIdList);

    /**
     * 拷贝全局插件到指定网关
     *
     * @param copyGlobalPluginDto 拷贝全局插件对象
     * @return 是否拷贝成功
     */
    boolean copyGlobalPluginToGatewayByVirtualGwId(CopyGlobalPluginDto copyGlobalPluginDto);

    /**
     * 通过网关ID查询指定类型的插件
     *
     * @param bindingPluginDto 绑定插件信息
     * @param projectId        插件所属的项目ID
     * @return 插件绑定对象集合
     */
    List<PluginBindingInfo> getPluginBindingListByVirtualGwIdAndTypeAndProjectId(BindingPluginDto bindingPluginDto, Long projectId);


    /**
     * 校验查询已绑定插件信息参数
     *
     * @param bindingObjectId
     * @param bindingObjectType
     * @return
     */
    ErrorCode checkDescribeBindingPlugins(String bindingObjectId, String bindingObjectType);

    /**
     * 获取绑定插件数据
     *
     * @param bindingPluginDto
     * @return
     */
    List<PluginBindingInfo> getBindingInfoList(PluginBindingDto bindingPluginDto);

    /**
     * 获取绑定插件数据
     *
     * @param bindingPluginDto
     * @return
     */
    List<PluginBindingInfo> getBindingInfoList(BindingPluginDto bindingPluginDto);


    /**
     * 获取绑定插件
     * @param bindingPluginDto
     * @return
     */
    PluginBindingInfo getBindingInfo(BindingPluginDto bindingPluginDto);

    /**
     * 获取内部使用插件，不对外暴露的插件plugin_type
     *
     * @return 内部插件plugin_type集合
     */
    List<String> getInnerPlugins();

    /**
     * 是否是内部插件
     *
     * @param pluginBindingDto 插件绑定对象
     * @return 是否是内部插件
     */
    boolean isInnerPlugin(PluginBindingDto pluginBindingDto);
}
