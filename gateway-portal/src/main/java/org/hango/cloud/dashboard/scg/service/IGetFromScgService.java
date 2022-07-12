package org.hango.cloud.dashboard.scg.service;

import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2021/11/18
 */
public interface IGetFromScgService {

    /**
     * 向SCG发布服务(该接口应幂等)，适用于发布及发布更新
     *
     * @param serviceProxyDto
     * @return
     */
    boolean publishServiceToScgGw(ServiceProxyDto serviceProxyDto);


    /**
     * 向SCG下线服务(该接口应幂等)
     *
     * @param serviceProxyDto
     * @return
     */
    boolean offlineServiceToScgGw(ServiceProxyDto serviceProxyDto);


    /**
     * 向SCG发布路由(该接口应幂等)，适用于发布及发布更新
     *
     * @param routeRuleProxyInfo
     * @return
     */
    boolean publishRouteToScgGw(RouteRuleProxyInfo routeRuleProxyInfo);


    /**
     * 向SCG下线路由(该接口应幂等)
     *
     * @param routeRuleProxyInfo
     * @return
     */
    boolean offlineRouteToScgGw(RouteRuleProxyInfo routeRuleProxyInfo);

}
