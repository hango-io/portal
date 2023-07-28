//package org.hango.cloud.envoy.advanced.metric.hooker;
//
//import org.hango.cloud.envoy.advanced.metric.dto.MetricDataDto;
//import org.hango.cloud.envoy.advanced.metric.dto.MetricDataQueryDto;
//import org.hango.cloud.envoy.advanced.metric.dto.MetricRankDto;
//import org.hango.cloud.envoy.advanced.metric.dto.MetricStatisticsDto;
//import org.hango.cloud.envoy.advanced.metric.meta.CountDataQuery;
//import org.hango.cloud.envoy.advanced.metric.meta.MetricDataQuery;
//import org.hango.cloud.envoy.advanced.metric.meta.RankDataQuery;
//import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
//import org.hango.cloud.envoy.advanced.metric.service.IEnvoyMetricService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * @author zhangbj
// * @version 1.0
// * @Type
// * @Desc
// * @date 2022/11/10
// */
//@Component
//public class EnvoyMetricHooker extends AbstractMetricHooker {
//
//    @Autowired
//    private IEnvoyMetricService metricService;
//
//    @Override
//    protected MetricStatisticsDto describeMetricStatistics(CountDataQuery query) {
//        return metricService.describeMetricStatistics(query);
//    }
//
//    @Override
//    protected Map<String, List<MetricRankDto>> getServiceRank(RankDataQuery query) {
//        return metricService.getServiceRank(query);
//    }
//
//    @Override
//    protected Map<String, List<MetricDataDto>> describeMetricData(MetricDataQuery query) {
//        return metricService.describeMetricData(query);
//    }
//
//    @Override
//    protected ErrorCode validMetricQueryParam(MetricDataQueryDto query) {
//        return metricService.validMetricQueryParam(query);
//    }
//}
