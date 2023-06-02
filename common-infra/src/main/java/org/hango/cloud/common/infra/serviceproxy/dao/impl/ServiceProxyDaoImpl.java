package org.hango.cloud.common.infra.serviceproxy.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.mapper.ServiceProxyMapper;
import org.hango.cloud.common.infra.serviceproxy.dao.IServiceProxyDao;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关元服务关联至网关服务相关dao
 *
 * @author hanjiahao
 */
@Component
public class ServiceProxyDaoImpl implements IServiceProxyDao {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProxyDaoImpl.class);

    @Autowired
    private ServiceProxyMapper serviceProxyMapper;

    @Override
    public BaseMapper<ServiceProxyInfo> getMapper() {
        return serviceProxyMapper;
    }


    @Override
    public Page<ServiceProxyInfo> getServiceProxyByLimit(ServiceProxyQuery query) {
        LambdaQueryWrapper<ServiceProxyInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ServiceProxyInfo::getProjectId, query.getProjectId())
                .orderByDesc(ServiceProxyInfo::getId);
        wrapper.eq(NumberUtils.INTEGER_ZERO != query.getVirtualGwId(), ServiceProxyInfo::getVirtualGwId, query.getVirtualGwId());
        wrapper.eq(StringUtils.isNotBlank(query.getProtocol()), ServiceProxyInfo::getProtocol,query.getProtocol());
        if (StringUtils.isNotBlank(query.getPattern())) {
            wrapper.and(
                    i -> i.like(ServiceProxyInfo::getName, query.getPattern())
                            .or()
                            .like(ServiceProxyInfo::getAlias, query.getPattern())
            );
        }
        if (StringUtils.isNotBlank(query.getCondition())) {
            wrapper.and(
                    i -> i.like(ServiceProxyInfo::getName, query.getCondition())
                            .or()
                            .like(ServiceProxyInfo::getAlias, query.getCondition())
                            .or()
                            .like(ServiceProxyInfo::getHosts, query.getCondition())
            );
        }
        return pageRecordsByField(wrapper, query.of());
    }

    @Override
    public List<ServiceProxyInfo> getByConditionOptional(ServiceProxyQuery query) {
        LambdaQueryWrapper<ServiceProxyInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(NumberUtils.LONG_ZERO < query.getProjectId(), ServiceProxyInfo::getProjectId, query.getProjectId());
        wrapper.eq(StringUtils.isNotBlank(query.getPattern()), ServiceProxyInfo::getName, query.getPattern());
        wrapper.eq(NumberUtils.LONG_ZERO < query.getVirtualGwId(), ServiceProxyInfo::getVirtualGwId, query.getVirtualGwId());
        wrapper.in(CollectionUtils.isNotEmpty(query.getNameList()), ServiceProxyInfo::getName, query.getNameList());
        return getRecordsByField(wrapper);
    }


    @Override
    public long updateVersion(long id, long version) {
        ServiceProxyInfo serviceProxyInfo = get(id);
        if (serviceProxyInfo == null) {
            return NumberUtils.LONG_ZERO;
        }
        serviceProxyInfo.setVersion(version);
        return update(serviceProxyInfo);
    }
}
