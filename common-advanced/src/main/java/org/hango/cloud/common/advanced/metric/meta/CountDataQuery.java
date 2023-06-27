package org.hango.cloud.common.advanced.metric.meta;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/6
 */
@Getter
@Setter
public class CountDataQuery extends MetricBaseQuery {

    private long time;

    private String dimensionCode;

    private String dimensionType;
}
