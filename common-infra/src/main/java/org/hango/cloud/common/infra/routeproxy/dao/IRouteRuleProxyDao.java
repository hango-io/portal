package org.hango.cloud.common.infra.routeproxy.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyPO;

import java.util.List;

/**
 * @author xin li
 * @date 2022/9/6 15:54
 */
public interface IRouteRuleProxyDao {

    Page<RouteRuleProxyPO> getRouteRuleProxyPage(RouteRuleQuery ruleInfoQuery, Page<RouteRuleProxyPO> page);

    List<RouteRuleProxyPO> getRouteRuleProxyList(RouteRuleQuery ruleInfoQuery);

    List<RouteRuleProxyPO> getRuleProxyListByMatchInfo(RouteRuleProxyPO routeRuleProxyPO);

}
