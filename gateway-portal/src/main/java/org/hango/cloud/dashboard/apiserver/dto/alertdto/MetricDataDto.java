package org.hango.cloud.dashboard.apiserver.dto.alertdto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/7/8
 */
@JSONType(ignores = "timestamp")
public class MetricDataDto {

    @JSONField(name = "Date")
    private String date;

    @JSONField(name = "MetricValue")
    private String metricValue;

    private long timestamp;

    public MetricDataDto(String date, String metricValue, long timestamp) {
        this.date = date;
        this.metricValue = metricValue;
        this.timestamp = timestamp;
    }

    public MetricDataDto(String date, String metricValue) {
        this.date = date;
        this.metricValue = metricValue;
    }

    public MetricDataDto() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }
}
