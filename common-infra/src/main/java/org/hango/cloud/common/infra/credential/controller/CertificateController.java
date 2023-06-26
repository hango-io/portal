package org.hango.cloud.common.infra.credential.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoDTO;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoViewDTO;
import org.hango.cloud.common.infra.credential.service.ICertificateInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/10/24 14:07
 * 证书管理控制台
 */
@Slf4j
@RestController
@RequestMapping(value = BaseConst.HANGO_DASHBOARD_PREFIX, params = {"Version=2022-10-30"})
public class CertificateController extends AbstractController {

    @Autowired
    private ICertificateInfoService certificateInfoService;

    /**
     * 创建证书
     * @param certificateInfoDTO 证书信息
     * @return 证书id
     */
    @RequestMapping(params = {"Action=CreateCertificate"}, method = RequestMethod.POST)
    public Object addCertificate(@RequestBody @Validated CertificateInfoDTO certificateInfoDTO) {
        log.info("start create certificate param:{}", JSONObject.toJSONString(certificateInfoDTO));
        ErrorCode errorCode = certificateInfoService.checkCreateParam(certificateInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        long id = certificateInfoService.create(certificateInfoDTO);
        return apiReturn(new Result(id));
    }


    /**
     * 删除证书
     * @param id 证书id
     * @return 删除结果
     */
    @RequestMapping(params = {"Action=DeleteCertificate"}, method = RequestMethod.GET)
    public Object deleteCertificate(@RequestParam(value = "CertificateId") long id) {
        log.info("start delete certificate id:{}", id);
        CertificateInfoDTO certificateInfoDTO = new CertificateInfoDTO();
        certificateInfoDTO.setCertificateId(id);
        ErrorCode errorCode = certificateInfoService.checkDeleteParam(certificateInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        certificateInfoService.delete(certificateInfoDTO);
        return apiReturn(new Result(id));
    }

    /**
     * 查询证书
     * @param pattern 证书名或域名
     */
    @RequestMapping(params = {"Action=DescribeCertificateList"}, method = RequestMethod.GET)
    public Object describeCertificateList(@RequestParam(value = "Pattern", required = false) String pattern) {
        log.info("describeCertificateList, pattern:{}", pattern);
        List<CertificateInfoViewDTO> certificateInfos = certificateInfoService.getCertificateInfos(pattern);
        return apiReturn(new Result(certificateInfos));
    }


    /**
     * 基于证书id查询证书信息
     */
    @RequestMapping(params = {"Action=DescribeCertificateInfo"}, method = RequestMethod.GET)
    public Object describeCertificate(@RequestParam(value = "CertificateId") long id) {
        log.info("describeCertificate, id:{}", id);
        CertificateInfoViewDTO certificateInfoViewDTO =  certificateInfoService.getCertificateInfoById(id);
        return apiReturn(new Result(certificateInfoViewDTO));
    }

}
