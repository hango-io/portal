package org.hango.cloud.envoy.infra.serviceproxy.service;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.envoy.infra.serviceproxy.dto.ResultDTO;
import org.hango.cloud.envoy.infra.serviceproxy.dto.ServiceRefreshDTO;

import java.util.Set;

/**
 * @Author zhufengwei
 * @Date 2023/4/17
 */
public interface IEnvoyServiceRefreshService {
    /**
     * 刷新服务域名
     */
    ResultDTO refreshServiceHost(ServiceRefreshDTO serviceRefreshDTO);

    /**
     * 刷新域名校验
     */
    ErrorCode checkRefrshParam(ServiceRefreshDTO serviceRefreshDTO);


    /**
     * 刷新路由域名
     */
    Boolean refreshRoute(Long vgId, Long serviceId, Set<String> hosts);
}
