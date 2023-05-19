package org.hango.cloud.envoy.infra.credential.hooker;

import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoDTO;
import org.hango.cloud.common.infra.credential.hooker.AbstractSecretProxyHooker;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
import org.hango.cloud.common.infra.credential.service.ICertificateInfoService;
import org.hango.cloud.envoy.infra.credential.service.IEnvoySecretProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/7
 */
@Component
public class EnvoySecretProxyHooker extends AbstractSecretProxyHooker<CertificateInfoPO, CertificateInfoDTO> {

    @Autowired
    private IEnvoySecretProxyService envoySecretProxyService;

    @Autowired
    private ICertificateInfoService certificateInfoService;

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    protected void preCreateHook(CertificateInfoDTO certificateInfoDTO) {
        if (!envoySecretProxyService.publishToGateway(certificateInfoDTO)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void preDeleteHook(CertificateInfoDTO certificateInfoDTO) {
        if (!envoySecretProxyService.offlineToGateway(certificateInfoDTO)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
