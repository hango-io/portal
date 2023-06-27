package org.hango.cloud.envoy.advanced.metric.service.builder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.common.advanced.metric.meta.MetricBaseQuery;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/1
 */
public class ProjectGatewayMetricBuilder extends GatewayMetricBuilder {

    protected static final String LABEL_PROJECT_ID = "project_id";
    protected static final String LABEL_VIRTUAL_GATEWAY_CODE = "virtual_gateway_code";

    private static final Map<String, String> METRIC_TEMPLATE = Maps.newHashMap();

    static {
        METRIC_TEMPLATE.put(AdvancedConst.SUCCESS_COUNT, "sum(increase(envoy_detailed_cluster_requests_total{response_code!~\"4|5\",<filter>}[<time_interval>s]))");
        METRIC_TEMPLATE.put(AdvancedConst.QPS, "ceil(sum(increase(envoy_detailed_cluster_requests_total{<filter>}[<time_interval>s]))/<step>)");
        METRIC_TEMPLATE.put(AdvancedConst.TOTAL_COUNT, "ceil(sum(increase(envoy_detailed_cluster_requests_total{<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.DURATION_AVG, "ceil(sum(rate(envoy_detailed_cluster_request_duration_milliseconds_sum{<filter>}[<time_interval>s]))/sum(rate(envoy_detailed_cluster_requests_total{<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.BAD_REQUEST, "ceil(sum(increase(envoy_detailed_cluster_requests_total{response_code=~\"4\\\\d{2}\",<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.FAILED_RATE, "sum(rate(envoy_detailed_cluster_requests_total{response_code=~\"5\\\\d{2}\",<filter>}[<time_interval>s]))/sum(rate(envoy_detailed_cluster_requests_total{<filter>}[<time_interval>s]))");
        METRIC_TEMPLATE.put(AdvancedConst.ERROR_REQUEST, "ceil(sum(increase(envoy_detailed_cluster_requests_total{response_code=~\"5\\\\d{2}\",<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.DURATION_95, "(histogram_quantile(0.95, sum by(<sumBy>, le) (increase(envoy_detailed_cluster_request_duration_milliseconds_bucket{<filter>}[<time_interval>s]))))");
        METRIC_TEMPLATE.put(AdvancedConst.DURATION_99, "(histogram_quantile(0.99, sum by(<sumBy>, le) (increase(envoy_detailed_cluster_request_duration_milliseconds_bucket{<filter>}[<time_interval>s]))))");

        METRIC_TEMPLATE.put(AdvancedConst.RANK_FAILED_COUNT, "topk(<topN>,(sum by(<sumBy>)(increase(envoy_detailed_cluster_requests_total{response_code=~\"5\\\\d{2}\",<filter>}[<time_interval>s]))))");
        METRIC_TEMPLATE.put(AdvancedConst.RANK_BAD_REQUEST_COUNT, "topk(<topN>,(sum by(<sumBy>)(increase(envoy_detailed_cluster_requests_total{response_code=~\"4\\\\d{2}\",<filter>}[<time_interval>s]))))");
        METRIC_TEMPLATE.put(AdvancedConst.RANK_TOTAL_COUNT, "topk(<topN>,(sum by(<sumBy>)(increase(envoy_detailed_cluster_requests_total{<filter>}[<time_interval>s]))))");

    }

    @Override
    public String template(String metricType) {
        return METRIC_TEMPLATE.get(metricType);
    }

    @Override
    public List<String> sumBy() {
        List<String> sumList = super.sumBy();
        sumList.add(LABEL_PROJECT_ID);
        return sumList;
    }

    @Override
    protected <T extends MetricBaseQuery> List<Map<String, String>> targets(T query) {
        List<Map<String, String>> targets = super.targets(query);
        Map<String, String> target = Iterables.getLast(targets);
        target.put(LABEL_PROJECT_ID, String.valueOf(query.getProjectId()));
        target.put(LABEL_VIRTUAL_GATEWAY_CODE, query.getVirtualGwCode());
        return targets;
    }
}
