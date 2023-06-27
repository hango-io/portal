package org.hango.cloud.common.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hango.cloud.common.infra.base.meta.PageQuery;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/10/26
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
public class QueryVirtualGatewayDto extends PageQuery implements Serializable {

    private static final long serialVersionUID = 6163983923024228367L;

    /**
     * 查询的项目范围
     */
    @JSONField(name = "ProjectIdList")
    private List<Long> projectIdList;

    /**
     * 服务id
     */
    @JSONField(name = "ServiceId")
    private Long serviceId;

    /**
     * 模糊匹配条件
     */
    @JSONField(name = "Pattern")
    private String pattern;

    /**
     * 虚拟网关类型
     */
    @JSONField(name = "Type")
    private String type;


    /**
     * 网关id
     */
    @JSONField(name = "GwId")
    private Long gwId;

    /**
     * 虚拟网关ID
     */
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

    /**
     * 是否被管理（Kubernetes虚拟网关不进行管理）
     */
    @JSONField(name = "Managed")
    private Boolean managed;

    @JSONField(serialize = false)
    private Long domainId;

}
