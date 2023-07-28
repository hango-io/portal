package org.hango.cloud.envoy.advanced.metric.meta;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/5
 */
@Getter
@Setter
public class RankDataQuery extends MetricBaseQuery{

    /**
     * 返回排行榜前N条数据
     */
    private int topN;

    /**
     * 排行维度，即显示到前端的排行数据名称，支持多个，显示效果为多个维度以"-" 链接
     */
    private String[] rankFields;
}
