package org.hango.cloud.envoy.advanced.metric.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/7/8
 */
@JSONType(ignores = "timestamp")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class MetricDataDto {

    @JSONField(name = "Date")
    private String date;

    @JSONField(name = "Time")
    private String time;
    @JSONField(name = "MetricValue")
    private String metricValue;

    private long timestamp;

}
