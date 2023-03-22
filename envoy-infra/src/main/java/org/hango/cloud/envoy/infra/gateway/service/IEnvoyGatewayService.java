package org.hango.cloud.envoy.infra.gateway.service;

import org.hango.cloud.envoy.infra.gateway.dto.EnvoyServiceDTO;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/3/21
 */
public interface IEnvoyGatewayService {
    //获取envoy proxy service ep 信息
    List<EnvoyServiceDTO> getEnvoyService(long gwId);
}
