package org.hango.cloud.envoy.infra.routeproxy.service;

import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;

import java.util.List;

/**
 * @author xin li
 * @date 2022/9/8 20:09
 */
public interface IEnvoyRouteRuleProxyService {

    /**
     * 通过api-plane 发布服务
     *
     * @param routeRuleProxyInfo   路由ProxyInfo
     * @param pluginConfigurations 插件配置
     * @return 发布结果
     */
    boolean publishRouteProxy(RouteRuleProxyDto routeRuleProxyInfo, List<String> pluginConfigurations);


    boolean deleteRouteProxy(RouteRuleProxyDto routeRuleProxyInfo);



    boolean deleteAllRoutePlugins(long virtualGwId, long routeRuleId);

    long updateRouteProxy(RouteRuleProxyDto routeRuleProxyDto);


}
