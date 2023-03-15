package org.hango.cloud.common.infra.routeproxy.dao.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.mapper.RouteRuleProxyMapper;
import org.hango.cloud.common.infra.base.util.PageUtil;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.routeproxy.dao.IRouteRuleProxyDao;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.CONST_ASC;

/**
 * @author xin li
 * @date 2022/9/6 15:56
 */
@Repository
public class RouteRuleProxyDaoImpl implements IRouteRuleProxyDao {

    @Autowired
    RouteRuleProxyMapper routeRuleProxyMapper;

    @Override
    public Page<RouteRuleProxyPO> getRouteRuleProxyPage(RouteRuleQuery ruleInfoQuery, Page<RouteRuleProxyPO> page) {
        PageUtil.sortHandle(ruleInfoQuery);
        //构建分页信息
        QueryWrapper<RouteRuleProxyPO> wrapper = new QueryWrapper<>();
        wrapper.orderBy(true, CONST_ASC.equals(ruleInfoQuery.getSortValue()), ruleInfoQuery.getSortKey());
        //构建查询信息
        LambdaQueryWrapper<RouteRuleProxyPO> query = wrapper.lambda();
        buildQueryWrapper(query, ruleInfoQuery);
        return routeRuleProxyMapper.selectPage(page, query);
    }

    @Override
    public List<RouteRuleProxyPO> getRouteRuleProxyList(RouteRuleQuery ruleInfoQuery) {
        //构建查询信息
        LambdaQueryWrapper<RouteRuleProxyPO> query = Wrappers.lambdaQuery();
        buildQueryWrapper(query, ruleInfoQuery);
        return routeRuleProxyMapper.selectList(query);
    }

    @Override
    public List<RouteRuleProxyPO> getRuleProxyListByMatchInfo(RouteRuleProxyPO routeRuleProxyPO) {
        QueryWrapper<RouteRuleProxyPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", routeRuleProxyPO.getProjectId());
        queryWrapper.eq("virtual_gw_id", routeRuleProxyPO.getVirtualGwId());
        queryWrapper.eq("priority", routeRuleProxyPO.getPriority());
        buildMatchQueryWrapper(queryWrapper, "uri", routeRuleProxyPO.getUri());
        buildMatchQueryWrapper(queryWrapper, "method", routeRuleProxyPO.getMethod());
        buildMatchQueryWrapper(queryWrapper, "host", routeRuleProxyPO.getHost());
        buildMatchQueryWrapper(queryWrapper, "query_param", routeRuleProxyPO.getQueryParam());
        buildMatchQueryWrapper(queryWrapper, "header", routeRuleProxyPO.getHeader());
        return routeRuleProxyMapper.selectList(queryWrapper);
    }

    private void buildMatchQueryWrapper(QueryWrapper<RouteRuleProxyPO> queryWrapper, String key, Object value){
        if (value == null){
            queryWrapper.isNull(key);
        }else {
            queryWrapper.eq(key, value.toString());
        }
    }

    private void buildQueryWrapper(LambdaQueryWrapper<RouteRuleProxyPO> query, RouteRuleQuery ruleInfoQuery){
        if (ruleInfoQuery.getVirtualGwId() != null){
            query.eq(RouteRuleProxyPO::getVirtualGwId, ruleInfoQuery.getVirtualGwId());
        }
        if (ruleInfoQuery.getProjectId() != null){
            query.eq(RouteRuleProxyPO::getProjectId, ruleInfoQuery.getProjectId());
        }
        if (ruleInfoQuery.getServiceId() != null){
            query.eq(RouteRuleProxyPO::getServiceId, ruleInfoQuery.getServiceId());
        }
        if (!CollectionUtils.isEmpty(ruleInfoQuery.getRouteRuleIds())){
            query.in(RouteRuleProxyPO::getRouteRuleId, ruleInfoQuery.getRouteRuleIds());
        }
        if (ruleInfoQuery.getMirrorServiceId() != null){
            query.eq(RouteRuleProxyPO::getMirrorServiceId, ruleInfoQuery.getMirrorServiceId());
        }
    }
}