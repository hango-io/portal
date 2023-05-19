package org.hango.cloud.common.infra.virtualgateway.meta;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.*;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

import java.io.Serializable;

/**
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "hango_virtual_gateway", autoResultMap = true)
public class VirtualGateway extends CommonExtension implements Serializable {

    private static final Long serialVersionUID = 7147341067988626279L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;

    /**
     * 虚拟网关id
     */
    private Long gwId;

    /**
     * 虚拟网关名称
     */
    private String name;

    /**
     * 虚拟网关标识
     */
    private String code;

    /**
     * 访问地址
     */
    private String addr;

    /**
     * 项目id列表，以逗号分隔
     */
    private String projectId;

    /**
     * 虚拟网关类型
     */
    private String type;

    /**
     * 虚拟网关协议
     */
    private String protocol;

    /**
     * 虚拟网关端口
     */
    private Integer port;

    /**
     * 域名id列表，以逗号分隔
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String domainId;

    /**
     * 高级设置
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private VirtualGatewaySetting advancedSetting;

    /**
     * 描述
     */
    private String description;

}
