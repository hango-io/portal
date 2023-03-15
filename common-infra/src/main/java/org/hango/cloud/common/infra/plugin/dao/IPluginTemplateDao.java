package org.hango.cloud.common.infra.plugin.dao;


import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;

import java.util.List;

/**
 * 插件模板dao层接口
 *
 * @author hzchenzhongyang 2020-04-08
 */
public interface IPluginTemplateDao extends IBaseDao<PluginTemplateInfo> {
    List<PluginTemplateInfo> getPluginTemplateInfoList(long projectId, String pluginType, long offset, long limit);

    long getPluginTemplateInfoCount(long projectId);

    List<PluginTemplateInfo> batchGet(List<Long> templateId);
}
