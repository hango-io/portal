package org.hango.cloud.common.infra.domain.hooker;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.credential.service.impl.CertificateInfoServiceImpl;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.pojo.DomainInfoPO;
import org.hango.cloud.common.infra.domain.service.impl.DomainInfoServiceImpl;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/7
 */
@Slf4j
public abstract class AbstractDomainProxyHooker<T extends DomainInfoPO, S extends DomainInfoDTO> extends AbstractInvokeHooker<T, S> {


    @Override
    public Class aimAt() {
        return DomainInfoServiceImpl.class;
    }
}
