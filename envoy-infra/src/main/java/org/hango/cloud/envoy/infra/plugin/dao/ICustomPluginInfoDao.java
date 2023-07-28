package org.hango.cloud.envoy.infra.plugin.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.dao.ICommonDao;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfoQuery;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/7/5
 */
public interface ICustomPluginInfoDao extends ICommonDao<CustomPluginInfo> {

    /**
     * 分页查询自定义插件列表
     */
    Page<CustomPluginInfo> getCustomPluginInfoPage(CustomPluginInfoQuery query);


    /**
     * 查询自定义列表
     */
    List<CustomPluginInfo> getCustomPluginInfoList(CustomPluginInfoQuery query);
}
