package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;

import java.util.List;

/**
 * 插件绑定关系dao层接口
 *
 * @author hzchenzhongyang 2019-11-11
 */
public interface IEnvoyPluginBindingInfoDao extends IBaseDao<EnvoyPluginBindingInfo> {
    /**
     * 查询某对象上绑定的插件数量
     *
     * @param gwId              网关id
     * @param projectId         项目id
     * @param bindingObjectId   绑定对象id 非必填
     * @param bindingObjectType 绑定对象类型
     * @return 指定对象上绑定的插件数量
     */
    long getBindingPluginCount(long gwId, long projectId, String bindingObjectId, String bindingObjectType);

    /**
     * 根据网关id列表、绑定对象id列表、绑定对象类型列表查询插件绑定关系数量
     *
     * @param projectId             项目id
     * @param gwIdList              网关id列表
     * @param bindingObjectIdList   绑定id列表
     * @param bindingObjectTypeList 绑定对象类型列表
     * @return 满足条件的插件绑定关系数量
     */
    long getBindingPluginCount(long projectId, long gwId, List<Long> gwIdList, String bindingObjectId, List<String> bindingObjectIdList, List<String> bindingObjectTypeList, String pattern);

    /**
     * 根据网关id列表、绑定对象id列表、绑定对象类型列表分页查询插件绑定关系列表
     *
     * @param projectId             项目id
     * @param gwIdList              网关id列表
     * @param bindingObjectIdList   绑定id列表
     * @param bindingObjectTypeList 绑定对象类型列表
     * @param offset                分页查询参数offset
     * @param limit                 分页查询参数limit
     * @param sortKey               排序关键字
     * @param sortValue             desc/asc
     * @return {@link List<EnvoyPluginBindingInfo>} 满足条件的插件绑定关系列表
     */
    List<EnvoyPluginBindingInfo> getBindingPluginList(long projectId, long gwId, List<Long> gwIdList, String bindingObjectId, List<String> bindingObjectIdList, List<String> bindingObjectTypeList, String pattern, long offset, long limit, String sortKey, String sortValue);

    /**
     * 根据绑定关系id列表批量删除绑定关系
     *
     * @param bindingInfoList 绑定关系列表
     * @return 删除条数
     */
    long batchDeleteBindingInfo(List<EnvoyPluginBindingInfo> bindingInfoList);

    /**
     * 批量解除插件绑定中关联的插件模板
     *
     * @param bindingInfoIdList 插件绑定关系id列表
     * @return 更新条数
     */
    long batchDissociateTemplate(List<Long> bindingInfoIdList);

    /**
     * 批量查询插件绑定关系
     *
     * @param bindingInfoIdList 插件绑定关系id列表
     * @return {@link List<EnvoyPluginBindingInfo>} 插件绑定关系列表
     */
    List<EnvoyPluginBindingInfo> batchGetById(List<Long> bindingInfoIdList);


    /**
     * 查询网关绑定插件
     */
    List<EnvoyPluginBindingInfo> getBindingPluginList(long projectId, long gwId, String bindingObjectType, List<String> bindingObjectIdList);
}
