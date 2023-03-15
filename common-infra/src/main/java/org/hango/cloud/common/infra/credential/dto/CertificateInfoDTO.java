package org.hango.cloud.common.infra.credential.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

import static org.hango.cloud.common.infra.base.meta.BaseConst.REGEX_DNS_1123;

@Getter
@Setter
public class CertificateInfoDTO extends CommonExtensionDto implements Serializable{
    private static final long serialVersionUID = -5112989418802761677L;

    /**
     * 证书id
     */
    @JSONField(name = "CertificateId")
    private Long certificateId;

    /**
     * 证书名称，不能相同  "yx_certificat"
     */
    @JSONField(name = "CertificateName")
    @NotBlank
    @Pattern(regexp = REGEX_DNS_1123, message = "证书名称必须符合dns_1123规范")
    private String name;

    /**
     * 证书类型 serverCert/caCert
     */
    @JSONField(name = "CertificateType")
    @NotBlank
    private String type;

    /**
     * 证书信息
     */
    @JSONField(name = "Content")
    @NotBlank
    private String content;

    /**
     * 私钥
     */
    @JSONField(name = "PrivateKey")
    private String privateKey;

}
