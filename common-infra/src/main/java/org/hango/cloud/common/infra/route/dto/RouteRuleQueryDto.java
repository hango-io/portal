package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/6
 */
@Getter
@Setter
@Builder
public class RouteRuleQueryDto {

    /**
     * 模糊匹配条件route_rule_name/uri/host
     */
    @JSONField(name = "Pattern")
    private String pattern;
    /**
     * 发布状态
     */
    @JSONField(name = "PublishStatus")
    private Integer publishStatus;

    /**
     * 服务id
     */
    @Min(0)
    @JSONField(name = "ServiceId")
    private Long serviceId;
    /**
     * 虚拟网关id
     */
    @Min(0)
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;

    /**
     * 路由id列表
     */
    @JSONField(name = "RouteRuleIds")
    private List<Long> routeRuleIds;
    /**
     * 项目id
     */
    @JSONField(name = "ProjectId")
    private Long projectId;
    /**
     * 排序条件
     */
    @JSONField(name = "SortByKey")
    @Pattern(regexp = "|create_time|priority")
    private String sortByKey;
    /**
     * 顺序 asc/desc
     */
    @JSONField(name = "SortByValue")
    @Pattern(regexp = "|asc|desc")
    private String sortByValue;

    @JSONField(name = "Limit")
    @Range(min = 1, max = 1000, message = "limit格式不合法")
    private Integer limit;

    @JSONField(name = "Offset")
    @Min(0)
    private Integer offset;
}
