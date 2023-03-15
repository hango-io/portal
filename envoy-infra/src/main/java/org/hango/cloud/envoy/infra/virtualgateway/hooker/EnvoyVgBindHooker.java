package org.hango.cloud.envoy.infra.virtualgateway.hooker;

import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayBindDto;
import org.hango.cloud.common.infra.virtualgateway.hooker.AbstractVgBindHooker;
import org.hango.cloud.envoy.infra.virtualgateway.service.IEnvoyVgService;
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
public class EnvoyVgBindHooker extends AbstractVgBindHooker<CommonExtension, VirtualGatewayBindDto> {

    @Autowired
    private IEnvoyVgService envoyVgService;


    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    protected void bindProject(VirtualGatewayBindDto virtualGatewayBindDto) {
        boolean result = envoyVgService.publishToGateway(virtualGatewayBindDto);
        if (!result){
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void unbindProject(long virtualGwId, long projectId) {
        boolean result = envoyVgService.offlineToGateway(virtualGwId,projectId);
        if (!result){
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
