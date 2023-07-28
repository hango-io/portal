package org.hango.cloud.envoy.advanced.metric.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/12
 */
@Getter
@Setter
public class MetricStatisticsDto {

    @JSONField(name = "TotalCount")
    @Getter(AccessLevel.NONE)
    private long totalCount;

    @JSONField(name = "SuccessCount")
    private long successCount;

    @JSONField(name = "BadRequestCount")
    private long badRequestCount;

    @JSONField(name = "FailedCount")
    private long failedCount;

    @JSONField(name = "AverageDuration")
    private int averageDuration;

    public long getTotalCount() {
        return successCount + badRequestCount + failedCount;
    }
}
