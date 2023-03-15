package org.hango.cloud.common.infra.route.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.mapper.RouteRuleInfoMapper;
import org.hango.cloud.common.infra.base.util.PageUtil;
import org.hango.cloud.common.infra.route.dao.IRouteRuleInfoDao;
import org.hango.cloud.common.infra.route.pojo.RouteRuleInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleMatchInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.CONST_ASC;

/**
 * @Author zhufengwei
 * @Date 2023/1/6
 */
@Repository
public class RouteRuleInfoDaoImpl implements IRouteRuleInfoDao {

    @Autowired
    private RouteRuleInfoMapper routeRuleInfoMapper;



    @Override
    public Page<RouteRuleInfoPO> getRuleInfoListPage(RouteRuleQuery ruleInfoQuery, Page<RouteRuleInfoPO> page) {
        PageUtil.sortHandle(ruleInfoQuery);
        //构建分页信息
        QueryWrapper<RouteRuleInfoPO> wrapper = new QueryWrapper<>();
        wrapper.orderBy(true, CONST_ASC.equals(ruleInfoQuery.getSortValue()), ruleInfoQuery.getSortKey());
        //构建查询信息
        LambdaQueryWrapper<RouteRuleInfoPO> query = wrapper.lambda();
        buildQueryWrapper(query, ruleInfoQuery);
        return routeRuleInfoMapper.selectPage(page, query);
    }

    @Override
    public List<RouteRuleInfoPO> getRuleInfoList(RouteRuleQuery ruleInfoQuery) {
        LambdaQueryWrapper<RouteRuleInfoPO> query = Wrappers.lambdaQuery();
        buildQueryWrapper(query, ruleInfoQuery);
        return routeRuleInfoMapper.selectList(query);
    }

    @Override
    public List<RouteRuleInfoPO> getRuleInfoListByMatchInfo(RouteRuleMatchInfoPO routeRuleMatchInfoPO) {
        QueryWrapper<RouteRuleInfoPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("priority", routeRuleMatchInfoPO.getPriority());
        buildMatchQueryWrapper(queryWrapper, "uri", routeRuleMatchInfoPO.getUri());
        buildMatchQueryWrapper(queryWrapper, "method", routeRuleMatchInfoPO.getMethod());
        buildMatchQueryWrapper(queryWrapper, "host", routeRuleMatchInfoPO.getHost());
        buildMatchQueryWrapper(queryWrapper, "query_param", routeRuleMatchInfoPO.getQueryParam());
        buildMatchQueryWrapper(queryWrapper, "header", routeRuleMatchInfoPO.getHeader());
        return routeRuleInfoMapper.selectList(queryWrapper);
    }

    private void buildMatchQueryWrapper(QueryWrapper<RouteRuleInfoPO> queryWrapper, String key, Object value){
        if (value == null){
            queryWrapper.isNull(key);
        }else {
            queryWrapper.eq(key, value.toString());
        }
    }


    private void buildQueryWrapper(LambdaQueryWrapper<RouteRuleInfoPO> query, RouteRuleQuery ruleInfoQuery){
        if (ruleInfoQuery.getProjectId() != null){
            query.eq(RouteRuleInfoPO::getProjectId, ruleInfoQuery.getProjectId());
        }
        if (ruleInfoQuery.getServiceId() != null){
            query.eq(RouteRuleInfoPO::getServiceId, ruleInfoQuery.getServiceId());
        }
        if (ruleInfoQuery.getPublishStatus() != null){
            query.eq(RouteRuleInfoPO::getPublishStatus, ruleInfoQuery.getPublishStatus());
        }
        String pattern = ruleInfoQuery.getPattern();
        if (StringUtils.isNotBlank(pattern)){
            query.and(q -> q
                    .like(RouteRuleInfoPO::getRouteRuleName, pattern)
                    .or()
                    .like(RouteRuleInfoPO::getUri, pattern)
                    .or()
                    .like(RouteRuleInfoPO::getHost, pattern)
            );
        }
    }

}
