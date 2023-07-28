package org.hango.cloud.envoy.advanced.metric.service.builder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.envoy.advanced.metric.meta.MetricBaseQuery;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/5/30
 */
public class GatewayMetricBuilder extends AbstractMetricBuilder {

    protected static final String LABEL_CLUSTER_NAME = "cluster_name";

    /**
     * 网关维度指标项
     */
    private static final Map<String, String> METRIC_TEMPLATE = Maps.newHashMap();

    static {
        METRIC_TEMPLATE.put(AdvancedConst.SUCCESS_COUNT, "sum(increase(envoy_http_downstream_rq_xx{envoy_response_code_class!~\"4|5\",<filter>}[<time_interval>s]))");
        METRIC_TEMPLATE.put(AdvancedConst.QPS, "ceil(sum(increase(envoy_http_downstream_rq_xx{<filter>}[<time_interval>s]))/<step>)");
        METRIC_TEMPLATE.put(AdvancedConst.TOTAL_COUNT, "ceil(sum(increase(envoy_http_downstream_rq_xx{<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.DURATION_AVG, "ceil(sum(rate(envoy_http_downstream_rq_time_sum{<filter>}[<time_interval>s]))/sum(rate(envoy_http_downstream_rq_xx{<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.BAD_REQUEST, "ceil(sum(increase(envoy_http_downstream_rq_xx{envoy_response_code_class=~\"4\",<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.FAILED_RATE, "sum(rate(envoy_http_downstream_rq_xx{envoy_response_code_class=~\"5\",<filter>}[<time_interval>s]))/sum(rate(envoy_http_downstream_rq_xx{<filter>}[<time_interval>s]))");
        METRIC_TEMPLATE.put(AdvancedConst.ERROR_REQUEST, "ceil(sum(increase(envoy_http_downstream_rq_xx{envoy_response_code_class=~\"5\",<filter>}[<time_interval>s])))");
        METRIC_TEMPLATE.put(AdvancedConst.DURATION_95, "(histogram_quantile(0.95, sum by(<sumBy>, le) (increase(envoy_http_downstream_rq_time_count{<filter>}[<time_interval>s]))))");
        METRIC_TEMPLATE.put(AdvancedConst.DURATION_99, "(histogram_quantile(0.99, sum by(<sumBy>, le) (increase(envoy_http_downstream_rq_time_count{<filter>}[<time_interval>s]))))");
    }

    @Override
    public String template(String metricType) {
        return METRIC_TEMPLATE.get(metricType);
    }

    @Override
    protected List<String> sumBy() {
        List<String> sumList = super.sumBy();
        sumList.add(LABEL_CLUSTER_NAME);
        return sumList;
    }

    @Override
    protected <T extends MetricBaseQuery> List<Map<String, String>> targets(T query) {
        List<Map<String, String>> targets = super.targets(query);
        Iterables.getLast(targets).put(LABEL_CLUSTER_NAME, query.getClusterName());
        return targets;
    }
}
