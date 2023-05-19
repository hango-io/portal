package org.hango.cloud.envoy.infra.domain.hooker;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.hooker.AbstractDomainProxyHooker;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.virtualgateway.service.IEnvoyVgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @Author zhufengwei
 * @Date 2022/11/08
 */
@Slf4j
@Component
public class EnvoyDomainProxyHooker extends AbstractDomainProxyHooker<DomainInfo, DomainInfoDTO> {

    @Autowired
    private IEnvoyVgService envoyVgService;

    @Autowired
    private DomainInfoMapper domainInfoMapper;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;


    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    protected void preUpdateHook(DomainInfoDTO domainInfoDTO) {
        if (!envoyVgService.refreshToGateway(domainInfoDTO)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
