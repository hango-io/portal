package org.hango.cloud.common.infra.route.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * @Author zhufengwei
 * @Date 2023/1/6
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@TableName(value = "hango_route_rule", autoResultMap = true)
public class RouteRuleInfoPO extends RouteRuleMatchInfoPO {

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
     * 项目id
     */
    private Long projectId;

    /**
     * 服务id
     */
    private Long serviceId;

    /**
     * 发布状态，0代表未发布，1代表已发布
     */
    private Integer publishStatus;

    /**
     * 路由规则名称，控制台展示使用
     */
    private String routeRuleName;

    /**
     * 路由规则描述
     */
    private String description;

}
