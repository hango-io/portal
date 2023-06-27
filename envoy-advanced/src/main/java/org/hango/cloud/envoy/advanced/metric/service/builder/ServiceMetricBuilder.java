package org.hango.cloud.envoy.advanced.metric.service.builder;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.advanced.metric.meta.CountDataQuery;
import org.hango.cloud.common.advanced.metric.meta.MetricBaseQuery;
import org.hango.cloud.common.advanced.metric.meta.MetricDataQuery;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/1
 */
public class ServiceMetricBuilder extends ProjectGatewayMetricBuilder {

    protected static final String LABEL_SERVICE_NAME = "service_name";

    @Override
    public List<String> sumBy() {
        List<String> sumList = super.sumBy();
        sumList.add(LABEL_SERVICE_NAME);
        return sumList;
    }

    @Override
    protected <T extends MetricBaseQuery> List<Map<String, String>> targets(T query) {
        List<Map<String, String>> targets = super.targets(query);
        Map<String, String> target = Iterables.getLast(targets);
        if (query instanceof CountDataQuery
                && StringUtils.isNotBlank(((CountDataQuery) query).getDimensionCode())) {
            target.put(LABEL_SERVICE_NAME, ((CountDataQuery) query).getDimensionCode());
        }
        if (query instanceof MetricDataQuery) {
            target.put(LABEL_SERVICE_NAME, ((MetricDataQuery) query).getDimensionCode());
        }
        return targets;
    }
}
