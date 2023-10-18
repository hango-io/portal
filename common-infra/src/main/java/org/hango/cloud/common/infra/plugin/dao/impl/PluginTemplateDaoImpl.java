package org.hango.cloud.common.infra.plugin.dao.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.mapper.PluginTemplateInfoMapper;
import org.hango.cloud.common.infra.plugin.dao.IPluginTemplateDao;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfoQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 插件模板dao层实现类
 *
 * @author hzchenzhongyang 2020-04-08
 */
@Component
public class PluginTemplateDaoImpl implements IPluginTemplateDao {

    @Autowired
    private PluginTemplateInfoMapper pluginBindingInfoMapper;

    @Override
    public BaseMapper<PluginTemplateInfo> getMapper() {
        return pluginBindingInfoMapper;
    }

    @Override
    public Page<PluginTemplateInfo> getPluginTemplateInfoPage(PluginTemplateInfoQuery query) {
        LambdaQueryWrapper<PluginTemplateInfo> wrapper = buildQueryWrapper(query);
        return pageRecordsByField(wrapper, query.of());
    }

    @Override
    public List<PluginTemplateInfo> getPluginTemplateInfoList(PluginTemplateInfoQuery query) {
        LambdaQueryWrapper<PluginTemplateInfo> wrapper = buildQueryWrapper(query);
        return getRecordsByField(wrapper);
    }

    private LambdaQueryWrapper<PluginTemplateInfo> buildQueryWrapper(PluginTemplateInfoQuery query){
        LambdaQueryWrapper<PluginTemplateInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getProjectId() != null, PluginTemplateInfo::getProjectId, query.getProjectId());
        wrapper.eq(StringUtils.isNotBlank(query.getPluginType()), PluginTemplateInfo::getPluginType, query.getPluginType());
        wrapper.eq(StringUtils.isNotBlank(query.getTemplateName()), PluginTemplateInfo::getTemplateName, query.getTemplateName());
        return wrapper;
    }
}
