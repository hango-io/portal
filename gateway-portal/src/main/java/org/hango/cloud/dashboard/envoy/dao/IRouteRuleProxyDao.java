package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;

import java.util.List;

/**
 * 路由规则发布信息dao层接口
 *
 * @author hzchenzhongyang 2019-09-18
 */
public interface IRouteRuleProxyDao extends IBaseDao<RouteRuleProxyInfo> {

    /**
     * 分页查询已发布路由规则列表
     *
     * @param gwId      网关id，若为0则不加入查询条件
     * @param serviceId 服务id，若为0则不加入查询条件
     * @param projectId 分页查询项目id，加入项目id,如果未指定项目网关id和serviceId，则查询当前项目下的路由规则
     * @param sortKey   查询sortKey
     * @param sortValue 查询sortValue
     * @param limit     分页查询参数limit
     * @param offset    分页查询参数offset
     * @return {@link List< RouteRuleProxyInfo >} 已发布路由规则列表
     */
    List<RouteRuleProxyInfo> getRouteRuleProxyList(long gwId, long serviceId, long projectId, String sortKey, String sortValue, long offset, long limit);

    List<RouteRuleProxyInfo> getRouteRuleProxyList(long gwId, long serviceId, List<Long> routeRuleId, long projectId, String sortKey, String sortValue, long offset, long limit);

    /**
     * 分页查询已发布路由授权列表
     *
     * @param gwId        网关id，非0
     * @param serviceId   服务id，若为0则查询所有路由
     * @param projectId   项目id
     * @param routeId     路由id
     * @param routeAuthId 已授权路由id
     * @param offset      分页查询offset
     * @param limit       分页查询limit
     * @return
     */
    List<RouteRuleProxyInfo> getRouteRuleProxyList(long gwId, long serviceId, long projectId, long routeId,
                                                   List<Long> routeAuthId,
                                                   long offset, long limit);

    /**
     * 查询已发布路由规则列表
     *
     * @param serviceId 服务id，若为0则不加入查询条件
     * @return {@link List< RouteRuleProxyInfo >} 已发布路由规则列表
     */
    List<RouteRuleProxyInfo> getRouteRuleProxyList(long serviceId);

    /**
     * 根据请求参数，查询已发布路由规则数量
     *
     * @param gwId      网关id，若为0则不加入查询条件
     * @param serviceId 服务id，若为0，则不加入查询条件
     * @param projectId 项目id,若不存在网关id和服务id，则查询当前项目下的路由规则
     * @return count，已发布路由规则数量
     */
    long getRouteRuleProxyCount(long gwId, long serviceId, long projectId);

    long getRouteRuleProxyCount(long gwId, long serviceId, long projectId, List<Long> routeRuleId);

    /**
     * 查询已发布路由授权数量
     *
     * @param gwId        网关id
     * @param serviceId   服务id
     * @param projectId   项目id
     * @param routeAuthId 路由授权id
     * @return count
     */
    long getRouteRuleProxyCount(long gwId, long serviceId, long projectId, long routeId, List<Long> routeAuthId);
}
