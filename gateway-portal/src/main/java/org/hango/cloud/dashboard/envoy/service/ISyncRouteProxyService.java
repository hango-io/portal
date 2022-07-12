package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.SyncRouteRuleGwDto;

import java.util.List;


public interface ISyncRouteProxyService {
    /**
     * 同步元数据基本信息
     *
     * @return
     */
    long syncRouteProxy(long gwId, long routeId);

    /**
     * 同步元数据基本信息校验
     *
     * @param routeRuleProxyDto 元数据基本信息
     * @return 参数校验errorcode
     */
    ErrorCode checkSyncRouteProxy(RouteRuleProxyDto routeRuleProxyDto);

    /**
     * 批量同步元数据基本信息
     *
     * @param gwIds             网关列表
     * @param routeRuleProxyDto 元数据基本信息
     * @return 同步失败的网关列表
     */
    List<String> syncRouteRuleBatch(List<Long> gwIds, RouteRuleProxyDto routeRuleProxyDto);

    /**
     * 根据路由id查询同步路由网关信息，包含是否和当前配置相同标识
     *
     * @param routeId 路由id
     * @return 同步SyncRouteRuleGwDtoList
     */
    List<SyncRouteRuleGwDto> describeGatewayForSyncRule(long routeId);

    RouteRuleProxyInfo toSyncMeta(RouteRuleProxyDto routeRuleProxyDto);
}
