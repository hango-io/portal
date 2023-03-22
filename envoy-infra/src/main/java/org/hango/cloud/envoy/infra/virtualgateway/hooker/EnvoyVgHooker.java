package org.hango.cloud.envoy.infra.virtualgateway.hooker;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.hooker.AbstractVirtualGatewayHooker;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.hango.cloud.envoy.infra.virtualgateway.service.IEnvoyVgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;



/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/8
 */
@Slf4j
@Component
public class EnvoyVgHooker extends AbstractVirtualGatewayHooker<VirtualGateway, VirtualGatewayDto> {

    @Autowired
    private IPluginManagerService pluginManagerService;

    @Autowired
    private IEnvoyVgService envoyVgService;

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
    protected void preDeleteHook(VirtualGatewayDto virtualGatewayDto) {
        if (!pluginManagerService.offlinePluginManager(virtualGatewayDto)) {
            throw ErrorCodeException.of(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        //删除ip配置
        if (!envoyVgService.deleteIpSource(virtualGatewayDto.getId())){
            throw ErrorCodeException.of(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected Object findSingleEnhancement(Object o) {
        if (o instanceof VirtualGatewayDto) {
            VirtualGatewayDto virtualGatewayDto = (VirtualGatewayDto)o;
            try {
                List<String> listenerAddr = envoyVgService.getEnvoyListenerAddr(virtualGatewayDto);
                virtualGatewayDto.setListenerAddr(listenerAddr);
            }catch (Exception e){
                log.error("get listener addr error, gwClusterName:{}", virtualGatewayDto.getGwClusterName(), e);
            }
        }
        return o;
    }
}
