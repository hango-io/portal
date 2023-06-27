package org.hango.cloud.common.advanced.metric.meta;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/5
 */
@Getter
@Setter
public class MetricDataQuery extends MetricBaseQuery {

    /**
     * 维度类型
     *
     *
     */
    @JSONField(name = "DimensionType")
    @NotNull
    private String dimensionType;

    /**
     * 维度标识，网关为网关GwClusterName,虚拟网关为Code,服务、路由为Name
     */
    @JSONField(name = "DimensionCode")
    @NotNull
    private String dimensionCode;

    /**
     * 趋势间隔
     */
    @JSONField(name = "Step")
    private long step = 60;
}
