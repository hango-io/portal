package org.hango.cloud.common.infra.virtualgateway.meta;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.PageQuery;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/3/28
 */
@Getter
@Setter
@Builder
public class VirtualGatewayQuery extends PageQuery {

    /**
     * 物理网关id
     */
    private List<Long> gwIds;
    /**
     * 虚拟网关名称
     */
    private String name;

    /**
     * 虚拟网关标识
     */
    private String code;

    /**
     * 虚拟网关类型
     */
    private String type;

    /**
     * 虚拟网关端口
     */
    private Integer port;

    /**
     * 域名
     */
    private Long domainId;

    /**
     * 项目列表
     */
    private List<Long> projectIds;


    /**
     * 模糊匹配条件
     */
    private String pattern;

    /**
     * 是否被管理（Kubernetes虚拟网关不进行管理）
     */
    private Boolean managed;
}
