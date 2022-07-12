package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.dto.plugindto.CopyGlobalPluginDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCodeEnum;
import org.hango.cloud.dashboard.envoy.meta.BindingPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginBindingDto;

import java.util.List;

/**
 * Envoy插件Service层接口
 * <p>
 * 当前插件的元信息存储在api-plane侧，暂不提供插件元信息的添加、更新、删除接口
 *
 * @author hzchenzhongyang 2019-10-23
 */
public interface IEnvoyPluginInfoService {

    /**
     * 查询插件信息时的参数校验（列表&详情）
     *
     * @param gwId 网关id
     * @return {@link ErrorCodeEnum} 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkDescribePlugin(long gwId);

    /**
     * 检查拷贝插件到指定网关的数据合法性
     *
     * @param copyGlobalPluginDto 拷贝全局插件对象
     * @return 错误码
     */
    ErrorCode checkCopyGlobalPluginToGateway(CopyGlobalPluginDto copyGlobalPluginDto);

    /**
     * 调用api-plane的接口获取全量插件列表（不包含schema）
     *
     * @param gwId 网关id
     * @return {@link List<EnvoyPluginInfo>} 插件详情列表
     */
    List<EnvoyPluginInfo> getPluginInfoListFromApiPlane(long gwId);

    /**
     * 调用api-plane的接口根据插件类型获取插件详情（含schema）
     *
     * @param gwId       网关id
     * @param pluginType
     * @return
     */
    EnvoyPluginInfo getPluginInfoFromApiPlane(long gwId, String pluginType);

    /**
     * 绑定插件时的参数校验
     *
     * @param bindingPluginInfo 绑定插件信息
     * @param projectId         项目id
     * @param templateId        关联模板id
     * @return {@link ErrorCodeEnum#Success} ErrorCodeEnum.Success
     */
    ErrorCode checkBindingPlugin(BindingPluginInfo bindingPluginInfo, long projectId, long templateId);

    /**
     * 新增绑定一个插件
     *
     * @param bindingPluginInfo 绑定插件信息
     * @param projectId         项目id
     * @param templateId        关联模板id
     * @return 绑定结果 true: 绑定成功； false: 绑定失败
     */
    boolean bindingPlugin(BindingPluginInfo bindingPluginInfo, long projectId, long templateId);

    /**
     * 绑定插件至数据库
     *
     * @param bindingInfo EnvoyPluginBindingInfo bindingInfo
     * @return 插件绑定id
     */
    long bindingPluginToDb(EnvoyPluginBindingInfo bindingInfo);

    long deletePluginFromDb(EnvoyPluginBindingInfo bindingInfo);


    /**
     * 查询当前对象上生效的插件绑定关系列表（仅含状态为enable的插件列表）
     *
     * @param gwId              网关id
     * @param bindingObjectId   插件绑定对象id
     * @param bindingObjectType 插件绑定对象类型，routeRule/service/global
     * @return {@link List<EnvoyPluginBindingInfo>} 绑定关系列表
     */
    List<EnvoyPluginBindingInfo> getEnablePluginBindingList(long gwId, String bindingObjectId, String bindingObjectType);

    /**
     * 查询当前对象上的插件绑定关系列表（包含状态为enable、disable的插件列表）
     *
     * @param gwId              网关id
     * @param bindingObjectId   插件绑定对象id
     * @param bindingObjectType 插件绑定对象类型，routeRule/service/global
     * @return {@link List<EnvoyPluginBindingInfo>} 绑定关系列表
     */
    List<EnvoyPluginBindingInfo> getPluginBindingList(long gwId, String bindingObjectId, String bindingObjectType);

    /**
     * 解绑一个插件
     *
     * @param pluginBindingInfoId 绑定关系id
     * @return 绑定结果 true: 解绑成功； false:解绑失败
     */
    boolean unbindingPlugin(long pluginBindingInfoId);

    /**
     * 根据插件绑定关系id查询插件绑定关系详情
     *
     * @param pluginBindingInfoId 插件绑定关系id
     * @return {@link EnvoyPluginBindingInfo} 插件绑定关系详情
     */
    EnvoyPluginBindingInfo getPluginBindingInfo(long pluginBindingInfoId);

    /**
     * 更新插件配置时参数校验
     *
     * @param pluginBindingInfoId 插件绑定关系id
     * @param pluginConfiguration 更新后插件配置
     * @param templateId          模板id，当为-1时解除插件绑定与模板的关联关系
     * @return {@link ErrorCodeEnum#Success} ErrorCodeEnum.Success
     */
    ErrorCode checkUpdatePluginConfiguration(long pluginBindingInfoId, String pluginConfiguration, long templateId);

    /**
     * 更新插件配置
     *
     * @param pluginBindingInfoId 插件绑定关系id
     * @param pluginConfiguration 更新后插件配置
     * @param templateId          模板id，当为-1时解除插件绑定与模板的关联关系
     * @return 更新结果 true: 更新成功； false: 更新失败
     */
    boolean updatePluginConfiguration(long pluginBindingInfoId, String pluginConfiguration, long templateId);

    /**
     * 更新插件配置
     *
     * @param pluginBindingInfoId   插件绑定关系id
     * @param pluginConfiguration   更新后插件配置
     * @param templateId            模板id
     * @param pluginTemplateVersion 插件模板版本
     * @return 更新结果 true: 更新成功 false: 更新失败
     */
    boolean updatePluginConfiguration(long pluginBindingInfoId, String pluginConfiguration, long templateId, long pluginTemplateVersion);

    /**
     * 查询某对象上绑定的插件数量
     *
     * @param gwId                  网关id
     * @param projectId             项目id
     * @param bindingObjectId       绑定对象id 非必填
     * @param bindingObjectTypeList 绑定对象类型列表 非必填
     * @param pattern               绑定插件列表模糊匹配。包括网关名称、绑定对象（服务、路由）名称、插件名称（类型）
     * @return 指定对象上绑定的插件数量
     */
    long getBindingPluginCount(long gwId, long projectId, String bindingObjectId, List<String> bindingObjectTypeList, String pattern);

    /**
     * 查询某对象上绑定的插件列表
     *
     * @param gwId                  网关id
     * @param projectId             项目id
     * @param bindingObjectId       绑定对象id 非必填
     * @param bindingObjectTypeList 绑定对象类型列表 非必填
     * @param pattern               绑定插件列表模糊匹配。包括网关名称、绑定对象（服务、路由）名称、插件名称（类型）
     * @param offset                分页查询参数offset
     * @param limit                 分页查询参数limit
     * @param sortKey               排序字段名称
     * @param sortValue             排序方式 desc/asc
     * @return {@link List<EnvoyPluginBindingInfo>} 指定对象上绑定的插件列表（未排序）
     */
    List<EnvoyPluginBindingInfo> getBindingPluginList(long gwId, long projectId, String bindingObjectId, List<String> bindingObjectTypeList, String pattern, long offset, long limit, String sortKey, String sortValue);

    /**
     * 删除指定对象绑定的插件列表（仅删除插件绑定表中数据，不调用api-plane地址进行更新）
     *
     * @param gwId              网关id
     * @param bindingObjectId   绑定对象id
     * @param bindingObjectType 绑定对象类型
     * @return 删除绑定关系记录条数
     */
    long deletePluginList(long gwId, String bindingObjectId, String bindingObjectType);

    /**
     * 修改插件绑定关系状态时的参数校验
     *
     * @param pluginBindingInfoId 插件绑定关系id
     * @param bindingStatus       更新后状态
     *                            // TODO FIXME 修改return注释
     * @return {@link ErrorCodeEnum#Success} ErrorCodeEnum.Success
     */
    ErrorCode checkUpdatePluginBindingStatus(long pluginBindingInfoId, String bindingStatus);

    /**
     * 修改插件绑定关系状态
     *
     * @param pluginBindingInfoId 插件绑定关系id
     * @param bindingStatus       更新后状态
     * @return true: 更新成功 false: 更新失败
     */
    boolean updatePluginBindingStatus(long pluginBindingInfoId, String bindingStatus);

    /**
     * 为EnvoyPluginBindingDto填充名称（网关名、绑定对象名称）
     *
     * @param envoyPluginBindingDtoList 插件绑定关系列表Dto
     */
    void fillDtoFiled(List<EnvoyPluginBindingDto> envoyPluginBindingDtoList);

    /**
     * 根据插件模板id查询关联的插件绑定列表
     *
     * @param templateId 插件模板id
     * @return {@link List<EnvoyPluginBindingInfo>} 插件绑定列表
     */
    List<EnvoyPluginBindingInfo> getBindingListByTemplateId(long templateId);

    /**
     * 根据插件模板id、网关id查询关联的插件绑定关系
     *
     * @param templateId 插件模板id
     * @param gwId       网关id
     * @return {@link List<EnvoyPluginBindingInfo>} 插件绑定列表
     */
    List<EnvoyPluginBindingInfo> getBindingListByTemplateId(long templateId, long gwId);

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
     * @return {@link List<EnvoyPluginBindingInfo>} 插件绑定关系列表
     */
    List<EnvoyPluginBindingInfo> batchGetById(List<Long> bindingInfoIdList);

    /**
     * 是否是内部插件
     * 内部插件不展示，内部插件是由系统创建的插件而非用户创建的插件，例如：soap-json-transcoder插件
     *
     * @param envoyPluginBindingInfo 插件绑定
     * @return 是否是内部插件
     */
    boolean isInsidePlugin(EnvoyPluginBindingInfo envoyPluginBindingInfo);

    EnvoyPluginBindingDto fromMeta(EnvoyPluginBindingInfo bindingInfo);

    /**
     * 发布网关插件
     *
     * @param bindingPluginInfo 网关插件聚合信息对象
     * @return 是否操作成功
     */
    boolean publishGatewayPlugin(BindingPluginInfo bindingPluginInfo);

    /**
     * 更新指定ID的网关插件
     *
     * @param bindingPluginInfo 网关插件聚合信息对象
     * @param pluginId          需要被更新的插件ID
     * @return 是否操作成功
     */
    boolean updateGatewayPlugin(BindingPluginInfo bindingPluginInfo, long pluginId);

    /**
     * 删除指定ID的网关插件
     *
     * @param bindingPluginInfo 网关插件聚合信息对象
     * @param pluginId          需要被删除的插件ID
     * @return 是否操作成功
     */
    boolean deleteGatewayPlugin(BindingPluginInfo bindingPluginInfo, long pluginId);

    /**
     * 删除指定ID列表的网关插件
     *
     * @param bindingPluginInfo 网关插件聚合信息对象
     * @param pluginIdList      需要被删除的插件ID集合
     * @return 是否操作成功
     */
    boolean deleteGatewayPlugin(BindingPluginInfo bindingPluginInfo, List<Long> pluginIdList);

    /**
     * 拷贝全局插件到指定网关
     *
     * @param copyGlobalPluginDto 拷贝全局插件对象
     * @return 是否拷贝成功
     */
    boolean copyGlobalPluginToGatewayByGwId(CopyGlobalPluginDto copyGlobalPluginDto);

    /**
     * 通过网关ID查询指定类型的插件
     *
     * @param bindingPluginInfo 绑定插件信息
     * @param projectId         插件所属的项目ID
     * @return 插件绑定对象集合
     */
    List<EnvoyPluginBindingInfo> getPluginBindingListByGwIdAndTypeAndProjectId(BindingPluginInfo bindingPluginInfo, Long projectId);
}
