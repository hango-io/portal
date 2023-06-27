package org.hango.cloud.envoy.advanced.metric.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hango.cloud.envoy.advanced.metric.service.builder.AbstractMetricBuilder;
import org.hango.cloud.envoy.advanced.metric.service.builder.GatewayMetricBuilder;
import org.hango.cloud.envoy.advanced.metric.service.builder.ProjectGatewayMetricBuilder;
import org.hango.cloud.envoy.advanced.metric.service.builder.RouteMetricBuilder;
import org.hango.cloud.envoy.advanced.metric.service.builder.ServiceMetricBuilder;
import org.hango.cloud.envoy.advanced.metric.service.builder.VirtualGatewayMetricBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 监控维度
 * @date 2019/7/8
 */
@Getter
@AllArgsConstructor
public enum DimensionType {

    /**
     * 网关维度
     */
    GATEWAY("Gateway", "cluster_name", new GatewayMetricBuilder()),
    /**
     * 虚拟网关维度
     */
    VIRTUAL_GATEWAY("VirtualGateway",  "virtual_gateway_code", new VirtualGatewayMetricBuilder()),
    /**
     * 项目网关维度
     */
    PROJECT_GATEWAY("ProjectGateway",  "project_id", new ProjectGatewayMetricBuilder()),

    /**
     * 服务维度
     */
    SERVICE("Service", "service_name", new ServiceMetricBuilder()),

    /**
     * 路由
     */
    ROUTE("Route", "route_rule_id", new RouteMetricBuilder());

    /**
     * 维度类型
     */
    private String dimensionType;

    /**
     * 该维度对应指标的label name
     */
    private String metricLabel;

    /**
     * 监控处理器
     */
    private AbstractMetricBuilder builder;

    public static Set<String> getDimensionTypeSet() {
        Set<String> dimensionTypeSet = new HashSet<>();
        for (DimensionType value : DimensionType.values()) {
            dimensionTypeSet.add(value.dimensionType);
        }
        return dimensionTypeSet;
    }

    public static String getDimensionTypePattern() {
        StringBuilder builder = new StringBuilder();
        for (DimensionType value : DimensionType.values()) {
            builder.append(value.dimensionType).append("|");
        }
        return builder.substring(0, builder.length() - 1);
    }

    public static DimensionType getByDimensionType(String dimensionType) {
        for (DimensionType value : DimensionType.values()) {
            if (value.dimensionType.equals(dimensionType)) {
                return value;
            }
        }
        return null;
    }
}
