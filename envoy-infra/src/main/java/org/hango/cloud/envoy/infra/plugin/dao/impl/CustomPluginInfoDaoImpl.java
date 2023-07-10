package org.hango.cloud.envoy.infra.plugin.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.envoy.infra.base.mapper.CustomPluginInfoMapper;
import org.hango.cloud.envoy.infra.plugin.dao.ICustomPluginInfoDao;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfoQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/7/5
 */
@Component
public class CustomPluginInfoDaoImpl implements ICustomPluginInfoDao{

    @Autowired
    CustomPluginInfoMapper customPluginInfoMapper;

    @Override
    public BaseMapper<CustomPluginInfo> getMapper() {
        return customPluginInfoMapper;
    }

    @Override
    public Page<CustomPluginInfo> getCustomPluginInfoPage(CustomPluginInfoQuery query) {
        LambdaQueryWrapper<CustomPluginInfo> wrapper = buildQueryWrapper(query);
        return pageRecordsByField(wrapper, query.of());
    }

    @Override
    public List<CustomPluginInfo> getCustomPluginInfoList(CustomPluginInfoQuery query) {
        LambdaQueryWrapper<CustomPluginInfo> wrapper = buildQueryWrapper(query);
        return getRecordsByField(wrapper);
    }

    private LambdaQueryWrapper<CustomPluginInfo> buildQueryWrapper(CustomPluginInfoQuery query){
        LambdaQueryWrapper<CustomPluginInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StringUtils.isNotBlank(query.getPluginType()), CustomPluginInfo::getPluginType, query.getPluginType());
        wrapper.like(StringUtils.isNotBlank(query.getPluginName()), CustomPluginInfo::getPluginName, query.getPluginName());
        return wrapper;
    }
}
