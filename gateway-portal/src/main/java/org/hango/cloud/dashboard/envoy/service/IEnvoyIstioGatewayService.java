package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyGatewaySettingDto;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/1/9
 */
public interface IEnvoyIstioGatewayService {

    /**
     * 修改 Envoy 网关资源
     *
     * @param setting
     * @param gatewayInfo
     * @return
     */
    boolean updateGatewaySetting(EnvoyGatewaySettingDto setting, GatewayInfo gatewayInfo);

    /**
     * 获取 Envoy 网关资源
     *
     * @param gatewayInfo
     * @return
     */
    EnvoyGatewaySettingDto getGatewaySetting(GatewayInfo gatewayInfo);
}
