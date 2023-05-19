package org.hango.cloud.envoy.infra.credential.service;

import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoDTO;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoViewDTO;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/7
 */
public interface IEnvoySecretProxyService extends CommonService<CertificateInfoPO, CertificateInfoViewDTO> {


    /**
     * 发布到网关数据面
     */
    boolean publishToGateway(CertificateInfoDTO certificateInfoDTO);

    /**
     * 从网关数据面下线
     */
    boolean offlineToGateway(CertificateInfoDTO certificateInfoDTO);

}
