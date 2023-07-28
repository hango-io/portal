package org.hango.cloud.common.infra.virtualgateway.dao.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.mapper.VirtualGatewayInfoMapper;
import org.hango.cloud.common.infra.virtualgateway.dao.IVirtualGatewayDao;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGatewayQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_GATEWAY;
import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_INGRESS;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Time: 创建时间: 2018/1/17 下午5:25.
 */
@Component
public class VirtualGatewayDaoImpl implements IVirtualGatewayDao {

    @Autowired
    private VirtualGatewayInfoMapper virtualGatewayInfoMapper;

    @Override
    public BaseMapper<VirtualGateway> getMapper() {
        return virtualGatewayInfoMapper;
    }



    @Override
    public Page<VirtualGateway> getVirtualGatewayPage(VirtualGatewayQuery query) {
        LambdaQueryWrapper<VirtualGateway> wrapper = buildQueryWrapper(query);
        return pageRecordsByField(wrapper, query.of());
    }

    @Override
    public List<VirtualGateway> getVirtualGatewayList(VirtualGatewayQuery query) {
        LambdaQueryWrapper<VirtualGateway> wrapper = buildQueryWrapper(query);
        return getRecordsByField(wrapper);
    }

    @Override
    public Boolean exist(VirtualGatewayQuery query) {
        return !CollectionUtils.isEmpty(getVirtualGatewayList(query));
    }

    private LambdaQueryWrapper<VirtualGateway> buildQueryWrapper(VirtualGatewayQuery query){
        LambdaQueryWrapper<VirtualGateway> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StringUtils.isNotBlank(query.getName()), VirtualGateway::getName, query.getName());
        wrapper.eq(StringUtils.isNotBlank(query.getCode()), VirtualGateway::getCode, query.getCode());
        wrapper.eq(StringUtils.isNotBlank(query.getType()), VirtualGateway::getType, query.getType());
        wrapper.eq(query.getPort() != null, VirtualGateway::getPort, query.getPort());
        wrapper.in(!CollectionUtils.isEmpty(query.getGwIds()), VirtualGateway::getGwId, query.getGwIds());
        wrapper.like(StringUtils.isNotBlank(query.getPattern()), VirtualGateway::getName, query.getPattern());
        wrapper.notIn(Boolean.TRUE.equals(query.getManaged()), VirtualGateway::getType, Arrays.asList(KUBERNETES_GATEWAY, KUBERNETES_INGRESS));
        wrapper.in(Boolean.FALSE.equals(query.getManaged()), VirtualGateway::getType, Arrays.asList(KUBERNETES_GATEWAY, KUBERNETES_INGRESS));
        wrapper.apply(query.getDomainId() != null, "find_in_set ('"+ query.getDomainId() +"', domain_id )");
        if (query.getProjectIds() != null){
            wrapper.apply(handleProjectQuery(query.getProjectIds()));
        }
        return wrapper;
    }


    private String handleProjectQuery(List<Long> projectIds){
        String findInSetTemplate = " find_in_set(%d,project_id) ";
        Set<String> projectQuery = projectIds.stream().map(p -> String.format(findInSetTemplate, p)).collect(Collectors.toSet());
        return StringUtils.join(projectQuery, "or");
    }
}
