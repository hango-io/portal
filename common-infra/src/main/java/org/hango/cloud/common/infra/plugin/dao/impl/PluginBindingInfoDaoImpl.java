package org.hango.cloud.common.infra.plugin.dao.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.mapper.PluginBindingInfoMapper;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 插件绑定关系dao层接口类
 *
 * @author hzchenzhongyang 2019-11-11
 */
@Component
public class PluginBindingInfoDaoImpl implements IPluginBindingInfoDao {

    @Autowired
    private PluginBindingInfoMapper pluginBindingInfoMapper;

    @Override
    public BaseMapper<PluginBindingInfo> getMapper() {
        return pluginBindingInfoMapper;
    }

    @Override
    public Page<PluginBindingInfo> getPluginBindingInfoPage(PluginBindingInfoQuery query) {
        LambdaQueryWrapper<PluginBindingInfo> wrapper = buildQueryWrapper(query);
        return pageRecordsByField(wrapper, query.of());
    }

    @Override
    public List<PluginBindingInfo> getPluginBindingInfoList(PluginBindingInfoQuery query) {
        LambdaQueryWrapper<PluginBindingInfo> wrapper = buildQueryWrapper(query);
        return getRecordsByField(wrapper);
    }

    private LambdaQueryWrapper<PluginBindingInfo> buildQueryWrapper(PluginBindingInfoQuery query){
        LambdaQueryWrapper<PluginBindingInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getVirtualGwId() != null, PluginBindingInfo::getVirtualGwId, query.getVirtualGwId());
        wrapper.eq(query.getProjectId() != null, PluginBindingInfo::getProjectId, query.getProjectId());
        wrapper.eq(query.getTemplateId() != null, PluginBindingInfo::getTemplateId, query.getTemplateId());
        wrapper.eq(StringUtils.isNotBlank(query.getBindingObjectId()), PluginBindingInfo::getBindingObjectId, query.getBindingObjectId());
        wrapper.eq(StringUtils.isNotBlank(query.getBindingObjectType()), PluginBindingInfo::getBindingObjectType, query.getBindingObjectType());
        wrapper.eq(StringUtils.isNotBlank(query.getBindingStatus()), PluginBindingInfo::getBindingStatus, query.getBindingStatus());
        wrapper.in(CollectionUtils.isNotEmpty(query.getPluginType()), PluginBindingInfo::getPluginType, query.getPluginType());
        wrapper.notIn(CollectionUtils.isNotEmpty(query.getExcludedPluginType()), PluginBindingInfo::getPluginType, query.getExcludedPluginType());
        wrapper.like(StringUtils.isNotBlank(query.getPattern()), PluginBindingInfo::getPluginType, query.getPattern());
        return wrapper;
    }
}
