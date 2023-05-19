package org.hango.cloud.common.infra.healthcheck.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.util.List;

/**
 * 健康检查规则DTO
 *
 * @author TC_WANG
 * @date 2019/11/19 下午3:09.
 */
@Getter
@Setter
public class HealthCheckRuleDto implements Serializable {

    /**
     * id
     */
    @JSONField(name = "Id")
    private long id;

    /**
     * 已发布服务id
     */
    @JSONField(name = "ServiceId")
    private long serviceId;

    /**
     * 主动检查开关
     */
    @Range(min = 0, max = 1, message = "主动检查开关不合法")
    @JSONField(name = "ActiveSwitch")
    private int activeSwitch;

    /**
     * 检查接口path，长度限制200
     */
    @JSONField(name = "Path")
    private String path;

    /**
     * 超时时间，单位s
     */
    @JSONField(name = "Timeout")
    private int timeout;

    /**
     * 健康状态码集合，默认仅包含200
     */
    @JSONField(name = "ExpectedStatuses")
    private List<Integer> expectedStatuses;

    /**
     * 健康实例检查间隔，单位秒
     */
    @JSONField(name = "HealthyInterval")
    private int healthyInterval;

    /**
     * 健康阈值
     */
    @JSONField(name = "HealthyThreshold")

    private int healthyThreshold;

    /**
     * 异常实例检查间隔，单位秒
     */
    @JSONField(name = "UnhealthyInterval")
    private int unhealthyInterval;

    /**
     * 异常阈值
     */
    @JSONField(name = "UnhealthyThreshold")
    private int unhealthyThreshold;

    /**
     * 被动检查开关
     */
    @Range(min = 0, max = 1, message = "被动检查开关不合法")
    @JSONField(name = "PassiveSwitch")
    private int passiveSwitch;

    /**
     * 连续网关失败次数	，统计返回code为502、503、504的情况
     */
    @JSONField(name = "ConsecutiveErrors")
    private int consecutiveErrors;

    /**
     * 驱逐时间，单位秒
     */
    @JSONField(name = "BaseEjectionTime")
    private int baseEjectionTime;

    /**
     * 最多可驱逐的实例比
     */
    @JSONField(name = "MaxEjectionPercent")
    private int maxEjectionPercent;

    /**
     * 最小健康实例比
     */
    @JSONField(name = "MinHealthPercent")
    private int minHealthPercent;
}
