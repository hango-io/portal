package org.hango.cloud.common.infra.plugin.dao;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.dao.ICommonDao;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;

import java.util.List;

/**
 * 插件绑定关系dao层接口
 *
 * @author hzchenzhongyang 2019-11-11
 */
public interface IPluginBindingInfoDao extends ICommonDao<PluginBindingInfo> {

    /**
     * 分页查询域名信息
     */
    Page<PluginBindingInfo> getPluginBindingInfoPage(PluginBindingInfoQuery query);


    /**
     * 查询域名列表
     */
    List<PluginBindingInfo> getPluginBindingInfoList(PluginBindingInfoQuery query);




}
