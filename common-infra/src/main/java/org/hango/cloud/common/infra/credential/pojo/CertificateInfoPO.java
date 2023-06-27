package org.hango.cloud.common.infra.credential.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("hango_certificate_info")
public class CertificateInfoPO extends CommonExtension {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;

    /**
     * 证书名称
     */
    private String name;

    /**
     * 证书类型
     */
    private String type;

    /**
     * 证书域名
     */
    private String host;

    /**
     * 公钥指纹
     */
    private String signature;

    /**
     * 签发机构
     */
    private String issuingAgency;

    /**
     * 签发信息
     */
    private String content;

    /**
     * 签发时间
     */
    private Long issuingTime;

    /**
     * 过期时间
     */
    private Long expiredTime;

    /**
     * AES加密后的私钥
     */
    private String privateKey;

}
