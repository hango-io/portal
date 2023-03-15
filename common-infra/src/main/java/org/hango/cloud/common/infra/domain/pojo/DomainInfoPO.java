package org.hango.cloud.common.infra.domain.pojo;

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
@TableName("hango_domain_info")
public class DomainInfoPO extends CommonExtension {

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
     * 域名
     */
    private String host;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 域名状态
     */
    private String status;

    /**
     * 证书id
     */
    private Long certificateId;

    /**
     * 环境信息
     */
    private String env;

    /**
     * 备注信息
     */
    private String description;


    /**
     * 域名关联id
     */
    private Long relevanceId;
}
