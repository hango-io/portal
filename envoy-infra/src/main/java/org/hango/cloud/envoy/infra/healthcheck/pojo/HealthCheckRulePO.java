package org.hango.cloud.envoy.infra.healthcheck.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author zhufengwei
 * @Date 2023/1/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("hango_health_check_rule")
public class HealthCheckRulePO {
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
     * 服务id
     */
    private Long serviceId;


    /**
     * 主动检查开关
     */
    private Integer activeSwitch;

    /**
     * 检查接口path，长度限制200
     */
    private String path;

    /**
     * 超时时间，单位ms
     */
    private Integer timeout;

    /**
     * 健康状态码集合，默认仅包含200
     */
    private String expectedStatuses;

    /**
     * 健康实例检查间隔，单位毫秒
     */
    private Integer healthyInterval;

    /**
     * 健康阈值
     */
    private Integer healthyThreshold;

    /**
     * 异常实例检查间隔，单位毫秒
     */
    private Integer unhealthyInterval;

    /**
     * 异常阈值
     */
    private Integer unhealthyThreshold;

    /**
     * 被动检查开关
     */
    private Integer passiveSwitch;

    /**
     * 连续网关失败次数	，统计返回code为502、503、504的情况
     */
    private Integer consecutiveErrors;

    /**
     * 驱逐时间，单位s
     */
    private Integer baseEjectionTime;

    /**
     * 最多可驱逐的实例比
     */
    private Integer maxEjectionPercent;

    /**
     * 最小健康实例比
     */
    private Integer minHealthPercent;
}
