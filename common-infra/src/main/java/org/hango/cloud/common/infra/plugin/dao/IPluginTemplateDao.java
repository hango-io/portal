package org.hango.cloud.common.infra.plugin.dao;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.dao.ICommonDao;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfoQuery;

import java.util.List;

/**
 * 插件模板dao层接口
 *
 * @author hzchenzhongyang 2020-04-08
 */
public interface IPluginTemplateDao extends ICommonDao<PluginTemplateInfo> {

    /**
     * 分页查询插件模板信息
     */
    Page<PluginTemplateInfo> getPluginTemplateInfoPage(PluginTemplateInfoQuery query);


    /**
     * 查询插件模板列表
     */
    List<PluginTemplateInfo> getPluginTemplateInfoList(PluginTemplateInfoQuery query);
}
