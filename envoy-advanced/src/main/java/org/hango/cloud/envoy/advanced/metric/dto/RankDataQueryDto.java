package org.hango.cloud.envoy.advanced.metric.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.dto.TimeQueryDto;

import javax.validation.constraints.Size;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/7
 */
@Getter
@Setter
public class RankDataQueryDto extends TimeQueryDto {

    /**
     * 返回排行榜前N条数据，默认为10条
     */
    @JSONField(name = "TopN")
    private int topN = 10;

    /**
     * 排行维度，即显示到前端的排行数据名称，支持多个，显示效果为多个维度以"-" 链接
     */
    @JSONField(name = "DimensionTypes")
    @Size(min = 1)
    private String[] dimensionTypes;

    /**
     * 排行指标
     */
    @JSONField(name = "MetricTypes")
    @Size(min = 1)
    private String[] metricTypes;

    /**
     * 虚拟网关标志
     */
    private String virtualGwCode;
}
