package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginTemplateInfo;

import java.util.List;

/**
 * 插件模板dao层接口
 *
 * @author hzchenzhongyang 2020-04-08
 */
public interface IEnvoyPluginTemplateDao extends IBaseDao<EnvoyPluginTemplateInfo> {
    List<EnvoyPluginTemplateInfo> getPluginTemplateInfoList(long projectId, String pluginType, long offset, long limit);

    long getPluginTemplateInfoCount(long projectId);

    List<EnvoyPluginTemplateInfo> batchGet(List<Long> templateId);
}
