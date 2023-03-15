package org.hango.cloud.common.infra.route.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.route.pojo.RouteRuleInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleMatchInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/6
 */
public interface IRouteRuleInfoDao {

    /**
     * 分页查询路由列表
     * @param ruleInfoQuery 查询条件
     * @param page 分页条件
     * @return 列表
     */
    Page<RouteRuleInfoPO> getRuleInfoListPage(RouteRuleQuery ruleInfoQuery, Page<RouteRuleInfoPO> page);


    /**
     * 查询路由列表
     * @param ruleInfoQuery 查询条件
     * @return 列表
     */
    List<RouteRuleInfoPO> getRuleInfoList(RouteRuleQuery ruleInfoQuery);

    /**
     * 基于路由匹配信息查询路由列表
     */
    List<RouteRuleInfoPO> getRuleInfoListByMatchInfo(RouteRuleMatchInfoPO routeRuleMatchInfoPO);

}
