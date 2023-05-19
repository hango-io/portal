package org.hango.cloud.common.infra.route.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.route.pojo.RoutePO;

import java.util.List;

/**
 * @author xin li
 * @date 2022/9/6 15:54
 */
public interface IRouteDao {

    Page<RoutePO> getRoutePage(RouteQuery ruleInfoQuery, Page<RoutePO> page);

    List<RoutePO> getRouteList(RouteQuery ruleInfoQuery);

    List<RoutePO> getRuleListByMatchInfo(RoutePO routePO);
}
