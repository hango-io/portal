package org.hango.cloud.common.infra.credential.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

import java.io.Serializable;

@Getter
@Setter
public class CertificateInfoViewDTO extends CommonExtensionDto implements Serializable{
    private static final long serialVersionUID = 2059264001216899604L;

    @JSONField(name = "CertificateId")
    private Long id;

    /**
     * 证书名称
     */
    @JSONField(name = "CertificateName")
    private String name;

    /**
     * 证书类型
     */
    @JSONField(name = "CertificateType")
    private String type;

    /**
     * 证书域名
     */
    @JSONField(name = "Domain")
    private String domain;

    /**
     * 公钥指纹
     */
    @JSONField(name = "Signature")
    private String signature;

    /**
     * 签发机构
     */
    @JSONField(name = "IssuingAgency")
    private String issuingAgency;


    /**
     * 签发时间
     */
    @JSONField(name = "IssuingTime")
    private String issuingTime;

    /**
     * 过期时间
     */
    @JSONField(name = "ExpiredTime")
    private String expiredTime;
}
