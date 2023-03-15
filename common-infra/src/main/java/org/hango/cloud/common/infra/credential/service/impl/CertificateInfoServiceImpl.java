package org.hango.cloud.common.infra.credential.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.util.SecurityUtil;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoDTO;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoViewDTO;
import org.hango.cloud.common.infra.base.mapper.CertificateInfoMapper;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
import org.hango.cloud.common.infra.credential.service.ICertificateInfoService;
import org.hango.cloud.common.infra.credential.util.CertificateUtil;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.domain.pojo.DomainInfoPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.DATE_FORMAT;
import static org.hango.cloud.common.infra.credential.constant.CredentialConst.*;

@Slf4j
@Service
public class CertificateInfoServiceImpl implements ICertificateInfoService {
    @Autowired
    private CertificateInfoMapper certificateInfoMapper;

    @Autowired
    private DomainInfoMapper domainInfoMapper;

    @Override
    public long create(CertificateInfoDTO certificateInfoDTO){
        /**
         * 证书内容构建
         * 1.证书解析
         * 2.服务器私钥加密
         */
        CertificateInfoPO certificateInfoPO = resolve(certificateInfoDTO);
        if (certificateInfoPO == null) {
            return -1L;
        }
        //证书持久化
        certificateInfoMapper.insert(certificateInfoPO);
        return certificateInfoPO.getId();
    }


    @Override
    public void delete(CertificateInfoDTO certificateInfoDTO){
        certificateInfoMapper.deleteById(certificateInfoDTO.getCertificateId());
    }

    @Override
    public List<CertificateInfoViewDTO> getCertificateInfos(String pattern){
        LambdaQueryWrapper<CertificateInfoPO> query = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(pattern)){
            query
                    .like(CertificateInfoPO::getName, pattern)
                    .or()
                    .like(CertificateInfoPO::getDomain, pattern);
        }
        List<CertificateInfoPO> certificateInfoPOS = certificateInfoMapper.selectList(query);
        return certificateInfoPOS.stream().map(this::toViewDTO).collect(Collectors.toList());
    }


    public CertificateInfoViewDTO toViewDTO(CertificateInfoPO certificateInfoPO){
        if (certificateInfoPO == null){
            return null;
        }
        CertificateInfoViewDTO certificateInfoViewDTO = new CertificateInfoViewDTO();
        certificateInfoViewDTO.setId(certificateInfoPO.getId());
        certificateInfoViewDTO.setDomain(certificateInfoPO.getDomain());
        certificateInfoViewDTO.setSignature(certificateInfoPO.getSignature());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        certificateInfoViewDTO.setExpiredTime(sdf.format(certificateInfoPO.getExpiredTime()));
        certificateInfoViewDTO.setIssuingAgency(certificateInfoPO.getIssuingAgency());
        certificateInfoViewDTO.setIssuingTime(sdf.format(certificateInfoPO.getIssuingTime()));
        certificateInfoViewDTO.setName(certificateInfoPO.getName());
        certificateInfoViewDTO.setType(certificateInfoPO.getType());
        return certificateInfoViewDTO;
    }

    @Override
    public CertificateInfoViewDTO getCertificateInfoById(long id){
        if (id <= 0){
            return null;
        }
        CertificateInfoPO certificateInfoPO = certificateInfoMapper.selectById(id);
        return toViewDTO(certificateInfoPO);
    }

    @Override
    public ErrorCode checkCreateParam(CertificateInfoDTO certificateInfoDTO){
        if (!Arrays.asList(SERVER_CERT, CA_CERT).contains(certificateInfoDTO.getType())){
            return CommonErrorCode.invalidParameter("证书类型错误");
        }
        CertificateInfoPO certificateInfoPO = CertificateInfoPO.builder().name(certificateInfoDTO.getName()).build();
        Long count = certificateInfoMapper.selectCount(new QueryWrapper<>(certificateInfoPO));
        if (count > 0){
            return CommonErrorCode.invalidParameter("证书名已存在，不允许重复创建");
        }
        if (!CertificateUtil.isValidity(certificateInfoDTO.getContent())){
            return CommonErrorCode.invalidParameter("证书格式错误");
        }
        if(SERVER_CERT.equals(certificateInfoDTO.getType()) && StringUtils.isBlank(certificateInfoDTO.getPrivateKey())){
            return CommonErrorCode.invalidParameter("服务器私钥不能为空");
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public CertificateInfoDTO toView(CertificateInfoPO certificateInfoPO) {
        return null;
    }

    public CertificateInfoPO resolve(CertificateInfoDTO certificateInfoDTO) {
        CertificateInfoPO certificateInfoPO;
        try {
            certificateInfoPO = CertificateUtil.resolve(certificateInfoDTO.getContent());
        } catch (Exception e) {
            //上层已进行过校验，这次不会出现异常
            return null;
        }
        certificateInfoPO.setName(certificateInfoDTO.getName());
        certificateInfoPO.setType(certificateInfoDTO.getType());
        //私钥加密
        certificateInfoPO.setPrivateKey(SecurityUtil.AESEncode(certificateInfoDTO.getPrivateKey()));
        return certificateInfoPO;
    }


    @Override
    public ErrorCode checkDeleteParam(CertificateInfoDTO certificateInfoDTO){
        Long id = certificateInfoDTO.getCertificateId();
        CertificateInfoPO certificateInfoPO = certificateInfoMapper.selectById(id);
        if (certificateInfoPO == null){
            return CommonErrorCode.invalidParameter("未找到需要删除的证书");
        }
        DomainInfoPO domainInfoPO = DomainInfoPO.builder().certificateId(id).build();
        Long count = domainInfoMapper.selectCount(new QueryWrapper<>(domainInfoPO));
        if (count > 0){
            return CommonErrorCode.invalidParameter("当前证书已被使用，不允许删除");
        }
        return CommonErrorCode.SUCCESS;
    }
}
