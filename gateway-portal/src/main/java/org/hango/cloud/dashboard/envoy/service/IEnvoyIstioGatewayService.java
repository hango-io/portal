package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.envoy.meta.GatewayInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyGatewaySettingDto;

/**
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/1/9
 */
public interface IEnvoyIstioGatewayService {

	/**
	 * 修改 Envoy 网关资源
	 */
	boolean updateGatewaySetting(EnvoyGatewaySettingDto setting, GatewayInfo gatewayInfo);

	/**
	 * 获取 Envoy 网关资源
	 */
	EnvoyGatewaySettingDto getGatewaySetting(GatewayInfo gatewayInfo);

}
