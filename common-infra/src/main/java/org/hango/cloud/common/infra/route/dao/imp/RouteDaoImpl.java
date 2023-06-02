package org.hango.cloud.common.infra.route.dao.imp;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.route.dao.RouteMapper;
import org.hango.cloud.common.infra.base.util.PageUtil;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.route.dao.IRouteDao;
import org.hango.cloud.common.infra.route.pojo.RoutePO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.CONST_ASC;

/**
 * @author xin li
 * @date 2022/9/6 15:56
 */
@Repository
public class RouteDaoImpl implements IRouteDao {

    @Autowired
    RouteMapper routeMapper;

    @Override
    public Page<RoutePO> getRoutePage(RouteQuery ruleInfoQuery, Page<RoutePO> page) {
        PageUtil.sortHandle(ruleInfoQuery);
        //构建分页信息
        QueryWrapper<RoutePO> wrapper = new QueryWrapper<>();
        wrapper.orderBy(true, CONST_ASC.equals(ruleInfoQuery.getSortValue()), ruleInfoQuery.getSortKey());
        //构建查询信息
        LambdaQueryWrapper<RoutePO> query = wrapper.lambda();
        buildQueryWrapper(query, ruleInfoQuery);
        return routeMapper.selectPage(page, query);
    }

    @Override
    public List<RoutePO> getRouteList(RouteQuery ruleInfoQuery) {
        //构建查询信息
        LambdaQueryWrapper<RoutePO> query = Wrappers.lambdaQuery();
        buildQueryWrapper(query, ruleInfoQuery);
        query.last("limit 1000");
        return routeMapper.selectList(query);
    }

    @Override
    public List<RoutePO> getRuleListByMatchInfo(RoutePO routePO) {
        QueryWrapper<RoutePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", routePO.getProjectId());
        queryWrapper.eq("virtual_gw_id", routePO.getVirtualGwId());
        queryWrapper.eq("priority", routePO.getPriority());
        buildMatchQueryWrapper(queryWrapper, "uri", routePO.getUri());
        buildMatchQueryWrapper(queryWrapper, "method", routePO.getMethod());
        buildMatchQueryWrapper(queryWrapper, "query_param", routePO.getQueryParam());
        buildMatchQueryWrapper(queryWrapper, "header", routePO.getHeader());
        return routeMapper.selectList(queryWrapper);
    }

    private void buildMatchQueryWrapper(QueryWrapper<RoutePO> queryWrapper, String key, Object value) {
        if (value == null) {
            queryWrapper.isNull(key);
        } else {
            queryWrapper.eq(key, JSON.toJSONString(value));
        }
    }

    private void buildQueryWrapper(LambdaQueryWrapper<RoutePO> query, RouteQuery ruleInfoQuery) {
        if (ruleInfoQuery.getVirtualGwId() != null) {
            query.eq(RoutePO::getVirtualGwId, ruleInfoQuery.getVirtualGwId());
        }
        if (ruleInfoQuery.getProjectId() != null) {
            query.eq(RoutePO::getProjectId, ruleInfoQuery.getProjectId());
        }
        if (!CollectionUtils.isEmpty(ruleInfoQuery.getRouteIds())) {
            query.in(RoutePO::getId, ruleInfoQuery.getRouteIds());
        }
        if (ruleInfoQuery.getMirrorServiceId() != null) {
            query.eq(RoutePO::getMirrorServiceId, ruleInfoQuery.getMirrorServiceId());
        }
        if (ruleInfoQuery.getEnableStatus() != null) {
            query.eq(RoutePO::getEnableState, ruleInfoQuery.getEnableStatus());
        }
        if (StringUtils.hasText(ruleInfoQuery.getPattern())) {
            query.and(
                    i -> i.like(RoutePO::getName, ruleInfoQuery.getPattern())
                            .or()
                            .like(RoutePO::getAlias, ruleInfoQuery.getPattern())
                            .or()
                            .like(RoutePO::getUri, ruleInfoQuery.getPattern())
            );
        }
        if (ruleInfoQuery.getServiceId() != null) {
            query.apply("find_in_set ('" + ruleInfoQuery.getServiceId() + "', service_ids )");
        }
    }
}