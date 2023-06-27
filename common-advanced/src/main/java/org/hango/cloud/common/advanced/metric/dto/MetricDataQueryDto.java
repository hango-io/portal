package org.hango.cloud.common.advanced.metric.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.dto.TimeQueryDto;

import javax.validation.constraints.NotNull;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/11
 */
@Getter
@Setter
public class MetricDataQueryDto extends TimeQueryDto {
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

    /**
     * 虚拟网关Code,检索网关指标时不必填，其他场景必填
     */
    @JSONField(name = "VirtualGwCode")
    private String virtualGwCode;

    /**
     * 监控类型
     *
     * @see org.hango.cloud.common.advanced.metric.meta.MetricTypeEnum
     */
    @JSONField(name = "MetricTypes")
    private String[] metricTypes;
}
