package org.hango.cloud.common.infra.routeproxy.service;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.routeproxy.dto.RouteProxySyncDto;
import org.hango.cloud.common.infra.routeproxy.dto.SyncRouteRuleGwDto;

import java.util.List;

public interface ISyncRouteProxyService {
    /**
     * 同步元数据基本信息
     *
     * @return
     */
    List<String>  syncRouteProxy(RouteProxySyncDto routeProxySyncDto);

    /**
     * 同步元数据基本信息校验
     *
     */
    ErrorCode checkSyncRouteProxy(RouteProxySyncDto routeProxySyncDto);

    /**
     * 根据路由id查询同步路由网关信息，包含是否和当前配置相同标识
     *
     * @param routeId 路由id
     * @return 同步SyncRouteRuleGwDtoList
     */
    List<SyncRouteRuleGwDto> describeGatewayForSyncRule(long routeId);
}
