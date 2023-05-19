package org.hango.cloud.common.infra.domain.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.domain.dao.IDomainInfoDao;
import org.hango.cloud.common.infra.domain.enums.DomainStatusEnum;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.domain.meta.DomainInfoQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关域名相关dao
 *
 */
@Component
public class DomainInfoDaoImpl implements IDomainInfoDao {

    @Autowired
    private DomainInfoMapper domainInfoMapper;

    @Override
    public BaseMapper<DomainInfo> getMapper() {
        return domainInfoMapper;
    }


    @Override
    public Page<DomainInfo> getDomainInfoPage(DomainInfoQuery query)  {
        LambdaQueryWrapper<DomainInfo> wrapper = buildQueryWrapper(query);
        return pageRecordsByField(wrapper, query.of());
    }

    @Override
    public List<DomainInfo> getDomainInfoList(DomainInfoQuery query) {
        LambdaQueryWrapper<DomainInfo> wrapper = buildQueryWrapper(query);
        return getRecordsByField(wrapper);
    }

    private LambdaQueryWrapper<DomainInfo> buildQueryWrapper(DomainInfoQuery query){
        if (StringUtils.isBlank(query.getStatus())){
            query.setStatus(DomainStatusEnum.Managed.name());
        }
        LambdaQueryWrapper<DomainInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DomainInfo::getStatus, query.getStatus());
        wrapper.eq(StringUtils.isNotBlank(query.getHost()), DomainInfo::getHost, query.getHost());
        wrapper.eq(StringUtils.isNotBlank(query.getProtocol()), DomainInfo::getProtocol, query.getProtocol());
        wrapper.in(CollectionUtils.isNotEmpty(query.getIds()), DomainInfo::getId, query.getIds());
        wrapper.in(CollectionUtils.isNotEmpty(query.getProjectIds()), DomainInfo::getProjectId, query.getProjectIds());
        wrapper.like(StringUtils.isNotBlank(query.getPattern()), DomainInfo::getHost, query.getPattern());
        return wrapper;
    }
}
