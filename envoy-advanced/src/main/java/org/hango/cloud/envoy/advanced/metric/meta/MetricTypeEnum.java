package org.hango.cloud.envoy.advanced.metric.meta;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 监控指标相关
 * @date 2019/7/8
 */
public enum MetricTypeEnum {
    /**
     * 告警类型
     */
    QPS("QPS", "count", "每秒查询率", "次 / 秒"),
    TOTAL_COUNT("TOTAL_COUNT", "count", "总请求次数", "次"),
    DURATION_AVG("DURATION_AVG", "duration_avg", "平均时延", "毫秒"),
    FAILED_RATE("FAILED_RATE", "fail_ratio", "请求失败率", "%"),
    BAD_REQUEST("BAD_REQUEST", "count_4xx", "每分钟4xx数", "次"),
    ERROR_REQUEST("ERROR_REQUEST", "count_5xx", "每分钟5xx数", "次"),
    DURATION_95("DURATION_95", "duration_95", "请求耗时95值", "毫秒"),
    DURATION_99("DURATION_99", "duration_95", "请求耗时99值", "毫秒");
    /**
     * 监控类型
     */
    private String metricType;

    /**
     * 对应Prometheus类型
     */
    private String promType;

    /**
     * 描述
     */
    private String description;

    /**
     * 单位
     */
    private String unit;

    MetricTypeEnum(String metricType, String promType, String description, String unit) {
        this.metricType = metricType;
        this.promType = promType;
        this.description = description;
        this.unit = unit;
    }

    public static String getPromType(String metricType) {
        for (MetricTypeEnum value : MetricTypeEnum.values()) {
            if (value.metricType.equals(metricType)) {
                return value.promType;
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getMetricType(String promType) {
        for (MetricTypeEnum value : MetricTypeEnum.values()) {
            if (value.promType.equals(promType)) {
                return value.metricType;
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getDescription(String metricType) {
        for (MetricTypeEnum value : MetricTypeEnum.values()) {
            if (value.metricType.equals(metricType)) {
                return value.description;
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getUnit(String metricType) {
        for (MetricTypeEnum value : MetricTypeEnum.values()) {
            if (value.metricType.equals(metricType)) {
                return value.unit;
            }
        }
        return StringUtils.EMPTY;
    }

    public static MetricTypeEnum get(String metricType) {
        for (MetricTypeEnum value : MetricTypeEnum.values()) {
            if (value.metricType.equals(metricType)) {
                return value;
            }
        }
        return null;
    }

    public static String getPromTypePattern() {
        StringBuilder builder = new StringBuilder();
        for (MetricTypeEnum value : MetricTypeEnum.values()) {
            builder.append(value.promType).append("|");
        }
        return builder.substring(0, builder.length() - 1);
    }

    public String getMetricType() {
        return metricType;
    }

    public String getPromType() {
        return promType;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

}
