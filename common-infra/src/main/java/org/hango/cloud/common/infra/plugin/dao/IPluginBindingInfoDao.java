package org.hango.cloud.common.infra.plugin.dao;


import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;

import java.util.List;

/**
 * 插件绑定关系dao层接口
 *
 * @author hzchenzhongyang 2019-11-11
 */
public interface IPluginBindingInfoDao extends IBaseDao<PluginBindingInfo> {

    /**
     * 根据网关id列表、绑定对象id列表、绑定对象类型列表查询插件绑定关系数量
     *
     * @param projectId             项目id
     * @param bindingObjectTypeList 绑定对象类型列表
     * @param excludedPluginTypeList 需要排除的插件类型集合
     * @return 满足条件的插件绑定关系数量
     */
    long getBindingPluginCount(long projectId, long virtualGwId, String bindingObjectId, String bindingObjectTypeList, String pattern, List<String> excludedPluginTypeList);

    /**
     * 根据网关id列表、绑定对象id列表、绑定对象类型列表分页查询插件绑定关系列表
     *
     * @param projectId             项目id
     * @param offset                分页查询参数offset
     * @param limit                 分页查询参数limit
     * @param sortKey               排序关键字
     * @param sortValue             desc/asc
     * @return {@link List< PluginBindingInfo >} 满足条件的插件绑定关系列表
     */
    List<PluginBindingInfo> getBindingPluginList(long projectId, long virtualGwId, String bindingObjectId, String bindingObjectType, String pattern, long offset, long limit, String sortKey, String sortValue);

    /**
     * 根据绑定关系id列表批量删除绑定关系
     *
     * @param bindingInfoList 绑定关系列表
     * @return 删除条数
     */
    long batchDeleteBindingInfo(List<PluginBindingDto> bindingInfoList);

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
     * @return {@link List< PluginBindingInfo >} 插件绑定关系列表
     */
    List<PluginBindingInfo> batchGetById(List<Long> bindingInfoIdList);


    /**
     * 查询网关绑定插件
     */
    List<PluginBindingInfo> getBindingPluginList(long projectId, long virtualGwId, String bindingObjectType, List<String> bindingObjectIdList);

    /**
     * 查询自定义插件列表下的实例
     */
    List<PluginBindingInfo> getBindingPluginList(String pluginType,String bindingObjectType, long offset, long limit);

    /**
     * 根据插件类型、插件作用域查询插件绑定关系数量
     *
     * @param pluginType 插件类型
     * @param bindingObjectType 插件作用域
     * @return 满足条件的插件绑定关系数量
     */
    long getBindingPluginCount(String pluginType,String bindingObjectType);
    /**
     * 更新版本号
     */
    long updateVersion(long id, long version);
}
