package org.hango.cloud.envoy.advanced.metric.service.builder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.envoy.advanced.metric.meta.MetricBaseQuery;
import org.hango.cloud.envoy.advanced.metric.meta.MetricDataQuery;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/1
 */
public class RouteMetricBuilder extends ProjectGatewayMetricBuilder {


    private static final Map<String, String> METRIC_TEMPLATE = Maps.newHashMap();
    protected static final String LABEL_ROUTE_RULE_ID = "route_rule_id";

    static {
        METRIC_TEMPLATE.put(AdvancedConst.SUCCESS_COUNT, "sum(increase(envoy_detailed_route_requests_total{response_code!~\"4\\\\d{2}|5\\\\d{2}\",<filter>}[<time_interval>s]))");
        METRIC_TEMPLATE.put(AdvancedConst.QPS, "ceil(sum(increase(envoy_detailed_route_requests_total{<filter>}[<time_interval>s]))/<step>)");
        METRIC_TEMPLATE.put(AdvancedConst.TOTAL_COUNT, "ceil(sum(increase(envoy_detailed_route_requests_total{<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.DURATION_AVG, "ceil(sum(rate(envoy_detailed_route_request_duration_milliseconds_sum{<filter>}[<time_interval>s]))/sum(rate(envoy_detailed_route_requests_total{<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.BAD_REQUEST, "ceil(sum(increase(envoy_detailed_route_requests_total{response_code=~\"4\\\\d{2}\",<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.FAILED_RATE, "sum(rate(envoy_detailed_route_requests_total{response_code=~\"5\\\\d{2}\",<filter>}[<time_interval>s]))/sum(rate(envoy_detailed_route_requests_total{<filter>}[<time_interval>s]))");
        METRIC_TEMPLATE.put(AdvancedConst.ERROR_REQUEST, "ceil(sum(increase(envoy_detailed_route_requests_total{response_code=~\"5\\\\d{2}\",<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.DURATION_95, "(histogram_quantile(0.95, sum by(<sumBy>, le) (increase(envoy_detailed_route_request_duration_milliseconds_bucket{<filter>}[<time_interval>s]))))");
        METRIC_TEMPLATE.put(AdvancedConst.DURATION_99, "(histogram_quantile(0.99, sum by(<sumBy>, le) (increase(envoy_detailed_route_request_duration_milliseconds_bucket{<filter>}[<time_interval>s]))))");
    }

    @Override
    public String template(String metricType) {
        return METRIC_TEMPLATE.get(metricType);
    }

    @Override
    public List<String> sumBy() {
        List<String> sumList = super.sumBy();
        sumList.add(LABEL_ROUTE_RULE_ID);
        return sumList;
    }

    @Override
    protected <T extends MetricBaseQuery> List<Map<String, String>> targets(T query) {
        List<Map<String, String>> targets = super.targets(query);
        String clusterName = query.getClusterName();
        String virtualGwCode = query.getVirtualGwCode();
        Iterables.getLast(targets).put(LABEL_VIRTUAL_GATEWAY_CODE, clusterName + "-" + virtualGwCode);
        if (query instanceof MetricDataQuery) {
            Iterables.getLast(targets).put(LABEL_ROUTE_RULE_ID, ((MetricDataQuery) query).getDimensionCode());
        }
        return targets;
    }

}
