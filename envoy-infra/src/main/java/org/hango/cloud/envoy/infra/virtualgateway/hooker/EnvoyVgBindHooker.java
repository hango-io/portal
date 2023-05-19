package org.hango.cloud.envoy.infra.virtualgateway.hooker;

import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.hango.cloud.common.infra.domain.dto.DomainBindDTO;
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
    protected void bindDomain(DomainBindDTO domainBindDTO) {
        boolean result = envoyVgService.bindDomain(domainBindDTO);
        if (!result){
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void unbindDomain(DomainBindDTO domainBindDTO) {
        boolean result = envoyVgService.unBindDomain(domainBindDTO);
        if (!result){
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
