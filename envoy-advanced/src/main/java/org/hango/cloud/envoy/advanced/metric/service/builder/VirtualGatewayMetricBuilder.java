package org.hango.cloud.envoy.advanced.metric.service.builder;

import com.google.common.collect.Iterables;
import org.hango.cloud.envoy.advanced.metric.meta.MetricBaseQuery;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/1
 */
public class VirtualGatewayMetricBuilder extends GatewayMetricBuilder {

    protected static final String LABEL_VIRTUAL_GATEWAY_CODE = "envoy_http_conn_manager_prefix";


    @Override
    protected List<String> sumBy() {
        List<String> sumList = super.sumBy();
        sumList.add(LABEL_VIRTUAL_GATEWAY_CODE);
        return sumList;
    }


    @Override
    protected <T extends MetricBaseQuery> List<Map<String, String>> targets(T query) {
        List<Map<String, String>> targets = super.targets(query);
        Iterables.getLast(targets).put(LABEL_VIRTUAL_GATEWAY_CODE, ".*" + query.getVirtualGatewayPort());
        return targets;
    }
}
