package org.hango.cloud.common.infra.healthcheck.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
     * 服务id
     */
    @JSONField(name = "ServiceId")
    private long serviceId;

    /**
     * 网关id
     */
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

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
    @Pattern(regexp = "([\\s\\S]){1,200}", message = "参数 Path 不能为空且长度不能超过200")
    private String path;

    /**
     * 超时时间，单位ms
     */
    @Min(value = 1, message = "超时时间不合法")
    @JSONField(name = "Timeout")
    private int timeout;

    /**
     * 健康状态码集合，默认仅包含200
     */
    @Size(min = 1, max = 10, message = "健康状态码不合法")
    @JSONField(name = "ExpectedStatuses")
    private List<Integer> expectedStatuses;

    /**
     * 健康实例检查间隔，单位毫秒
     */
    @Range(min = 1, max = 1000000000, message = "健康实例检查间隔不合法")
    @JSONField(name = "HealthyInterval")
    private int healthyInterval;

    /**
     * 健康阈值
     */
    @Range(min = 1, max = 1000000000, message = "健康阈值不合法")
    @JSONField(name = "HealthyThreshold")

    private int healthyThreshold;

    /**
     * 异常实例检查间隔，单位毫秒
     */
    @Range(min = 1, max = 1000000000, message = "异常实例检查间隔不合法")
    @JSONField(name = "UnhealthyInterval")
    private int unhealthyInterval;

    /**
     * 异常阈值
     */
    @Range(min = 1, max = 1000000000, message = "异常阈值不合法")
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
    @Range(min = 0, max = 100, message = "最小健康实例比不合法")
    private int minHealthPercent;
}
