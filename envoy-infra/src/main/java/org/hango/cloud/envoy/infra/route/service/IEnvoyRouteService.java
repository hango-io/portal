package org.hango.cloud.envoy.infra.route.service;

import org.hango.cloud.common.infra.route.dto.RouteDto;

import java.util.List;

/**
 * @author xin li
 * @date 2022/9/8 20:09
 */
public interface IEnvoyRouteService {

    /**
     * 通过 api-plane 发布路由
     *
     * @param routeDto             路由信息
     * @param pluginConfigurations 插件配置
     * @return 发布结果
     */
    boolean publishRoute(RouteDto routeDto, List<String> pluginConfigurations);

    /**
     * 通过 api-plane 发布路由
     *
     * @param routeDto 路由信息
     * @return 删除结果
     */
    boolean deleteRoute(RouteDto routeDto);

    /**
     * 通过 api-plane 发布路由
     * @param routeRuleProxyInfo 路由信息
     * @return 删除结果
     */
    boolean deleteRouteRuleByApiPlane(RouteDto routeRuleProxyInfo);


    /**
     * 删除所有路由绑定插件
     *
     * @param virtualGwId 虚拟网关ID
     * @param routeId     路由ID
     * @return 删除结果
     */
    boolean deleteAllRoutePlugins(long virtualGwId, long routeId);

    /**
     * 通过 api-plane 更新路由
     *
     * @param routeDto 路由信息
     * @return 更新结果
     */
    long updateRoute(RouteDto routeDto);
}
