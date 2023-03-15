package org.hango.cloud.common.infra.credential.hooker;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoDTO;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
import org.hango.cloud.common.infra.credential.service.impl.CertificateInfoServiceImpl;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/7
 */
@Slf4j
public abstract class AbstractSecretProxyHooker<T extends CertificateInfoPO, S extends CertificateInfoDTO> extends AbstractInvokeHooker<T, S> {


    @Override
    public Class aimAt() {
        return CertificateInfoServiceImpl.class;
    }
}
