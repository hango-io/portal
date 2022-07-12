package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;

import java.util.List;

/**
 * 路由规则Dao层接口
 *
 * @author hzchenzhongyang 2019-09-11
 */
public interface IRouteRuleInfoDao extends IBaseDao<RouteRuleInfo> {
    /**
     * 根据ruleName分页查询路由规则
     *
     * @param pattern       路由规则模糊匹配，包括路由规则名称，path，host
     * @param publishStatus 路由规则发布状态
     * @param projectId     项目id
     * @param sortKey       路由规则查询sortKey
     * @param sortValue     路由规则查询sortValue
     * @param offset        分页查询offset
     * @param limit         分页查询limit
     * @return {@link List< RouteRuleInfo >} 路由规则
     */
    List<RouteRuleInfo> getRuleInfoByLimit(String pattern, int publishStatus, long projectId,
                                           String sortKey, String sortValue, long offset, long limit);

    /**
     * 根据ruleName以及服务id分页查询路由规则
     *
     * @param pattern       路由规则模糊匹配，包括路由规则名称，path,host
     * @param publishStatus 路由规则发布状态
     * @param serviceId     服务id
     * @param sortKey       路由规则查询sortKey
     * @param sortValue     路由规则查询sortValue
     * @param offset        分页查询offset
     * @param limit         分页查询limit
     * @return {@link List< RouteRuleInfo >} 路由规则列表
     */
    List<RouteRuleInfo> getRuleInfoByServiceLimit(String pattern, int publishStatus, long serviceId,
                                                  String sortKey, String sortValue, long offset, long limit);

    /**
     * 查询路由规则数量
     *
     * @param pattern       路由规则模糊匹配，包括路由规则名称，path,host
     * @param publishStatus 路由规则发布状态
     * @param projectId     项目id
     * @return 路由规则数量
     */
    long getRuleInfoCount(String pattern, int publishStatus, long projectId);

    /**
     * 查询路由规则数量
     *
     * @param pattern       路由规则模糊匹配，包括路由规则名称，path,host
     * @param publishStatus 路由规则发布状态
     * @param serviceId     服务id
     * @return 路由规则数量
     */
    long getRuleInfoByServiceCount(String pattern, int publishStatus, long serviceId);

    /**
     * 根据路由规则名称模糊查询满足匹配条件的id列表
     *
     * @param routeRuleName 路由规则名称，支持模糊查询
     * @param projectId     项目id
     * @return {@link List<Long>} 满足条件的id列表
     */
    List<Long> getRouteRuleIdListByNameFuzzy(String routeRuleName, long projectId);

    /**
     * 根据路由规则id列表查询路由规则详情列表
     *
     * @param routeRuleIdList 路由规则id列表
     * @return {@link List< RouteRuleInfo >} 路由规则详情列表
     */
    List<RouteRuleInfo> getRouteRuleList(List<Long> routeRuleIdList);
}
