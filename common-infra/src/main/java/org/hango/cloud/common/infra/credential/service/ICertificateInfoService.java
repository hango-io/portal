package org.hango.cloud.common.infra.credential.service;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoDTO;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoViewDTO;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/10/26
 */
public interface ICertificateInfoService extends CommonService<CertificateInfoPO, CertificateInfoDTO> {

    /**
     * 基于证书名/域名 分页吗，模糊查询证书列表
     * @param pattern 模糊查询条件 证书名/域名
     */
    List<CertificateInfoViewDTO> getCertificateInfos(String pattern);


    CertificateInfoViewDTO getCertificateInfoById(long id);
}
