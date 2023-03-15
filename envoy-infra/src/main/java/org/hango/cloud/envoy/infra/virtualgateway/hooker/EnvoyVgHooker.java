package org.hango.cloud.envoy.infra.virtualgateway.hooker;

import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.hooker.AbstractVirtualGatewayHooker;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/8
 */
@Component
public class EnvoyVgHooker extends AbstractVirtualGatewayHooker<VirtualGateway, VirtualGatewayDto> {

    @Autowired
    private IPluginManagerService pluginManagerService;

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    protected void preCreateHook(VirtualGatewayDto virtualGatewayDto) {
        if (!pluginManagerService.publishPluginManager(virtualGatewayDto)) {
            throw ErrorCodeException.of(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void preUpdateHook(VirtualGatewayDto virtualGatewayDto){
        if (!pluginManagerService.publishPluginManager(virtualGatewayDto)) {
            throw ErrorCodeException.of(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    protected void preDeleteHook(VirtualGatewayDto virtualGatewayDto) {
        if (!pluginManagerService.offlinePluginManager(virtualGatewayDto)) {
            throw ErrorCodeException.of(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
