package org.hango.cloud.common.infra.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @Author zhufengwei
 * @Date 2022/10/24
 */
@Getter
@Setter
public class DomainInfoDTO extends CommonExtensionDto implements Serializable {

    private static final long serialVersionUID = -5073400516041694364L;

    /**
     * id
     */
    @JSONField(name = "DomainId")
    private Long id;
    /**
     * 域名
     */
    @NotBlank
    @Pattern(regexp = BaseConst.REGEX_DOMAIN)
    @JSONField(name = "Host")
    private String host;


    /**
     * 协议 HTTP/HTTPS
     */
    @Pattern(regexp = BaseConst.PROTOCOL_SCHEME_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE)
    @JSONField(name = "Protocol")
    private String protocol;
    /**
     * 域名状态
     */
    @JSONField(name = "Status")
    private String status;

    /**
     * 域名状态描述
     */
    @JSONField(name = "StatusDesc")
    private String statusDesc;

    /**
     * 证书id
     */
    @JSONField(name = "CertificateId")
    private Long certificateId;

    /**
     * 证书名称
     */
    @JSONField(name = "CertificateName")
    private String certificateName;

    /**
     * 环境信息
     */
    @JSONField(name = "Env")
    private String env;

    /**
     * 备注信息
     */
    @JSONField(name = "Description")
    @Size(max = 200, message = "备注信息过长")
    private String description;


    /**
     * 项目id
     */
    @JSONField(name = "ProjectId")
    private long projectId;

    /**
     * 域名关联id
     */
    @JSONField(name = "RelevanceId")
    private Long relevanceId;
}
