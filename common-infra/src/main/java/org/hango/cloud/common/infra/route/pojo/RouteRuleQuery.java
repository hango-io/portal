package org.hango.cloud.common.infra.route.pojo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/6
 */
@Getter
@Setter
@Builder
public class RouteRuleQuery {
    /**
     * 模糊匹配条件route_rule_name/uri/host
     */
    private String pattern;
    /**
     * 发布状态
     */
    private Integer publishStatus;

    /**
     * 发布状态
     */
    private Long virtualGwId;

    /**
     * 路由id
     */
    private List<Long> routeRuleIds;
    /**
     * 服务id
     */
    private Long serviceId;
    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 服务镜像id
     */
    private Long mirrorServiceId;
    /**
     * 排序条件
     */
    private String sortKey;
    /**
     * 顺序 asc/desc
     */
    private String sortValue;
}
