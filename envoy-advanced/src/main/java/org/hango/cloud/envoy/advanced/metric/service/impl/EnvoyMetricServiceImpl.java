package org.hango.cloud.envoy.advanced.metric.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.advanced.base.meta.AdvanceErrorCode;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.common.advanced.gateway.service.IGatewayAdvancedService;
import org.hango.cloud.common.advanced.metric.dto.MetricDataDto;
import org.hango.cloud.common.advanced.metric.dto.MetricDataQueryDto;
import org.hango.cloud.common.advanced.metric.dto.MetricRankDto;
import org.hango.cloud.common.advanced.metric.dto.MetricStatisticsDto;
import org.hango.cloud.common.advanced.metric.meta.CountDataQuery;
import org.hango.cloud.common.advanced.metric.meta.MetricDataQuery;
import org.hango.cloud.common.advanced.metric.meta.RankDataQuery;
import org.hango.cloud.common.advanced.metric.service.IMetricService;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.advanced.metric.meta.DimensionType;
import org.hango.cloud.envoy.advanced.metric.service.IEnvoyMetricService;
import org.hango.cloud.envoy.advanced.metric.service.builder.AbstractMetricBuilder;
import org.hango.cloud.envoy.infra.base.config.EnvoyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/10
 */
@Service
@SuppressWarnings({"java:S1192"})
public class EnvoyMetricServiceImpl implements IEnvoyMetricService {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyMetricServiceImpl.class);

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IGatewayAdvancedService gatewayAdvancedService;


    @Autowired
    private IMetricService metricService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private EnvoyConfig envoyConfig;

    @Autowired
    private IRouteService routeService;

//
//    /**
//     * prometheus表达式模板存储集
//     * <p
//     * Map<MetricType, Prometheus_QL_template>
//     */
//    public static final Map<String, String> promTemplateMap = Maps.newHashMap();
//
//
//    static {
//        promTemplateMap.put(AdvancedConst.SUCCESS_COUNT, "sum(increase(envoy_detailed_route_requests_total{response_code!~\"4|5\",<filter>}[<time_interval>s]))");
//        promTemplateMap.put(AdvancedConst.QPS, "ceil(sum(increase(envoy_detailed_route_requests_total{<filter>}[<time_interval>s]))/<step>)");
//        promTemplateMap.put(AdvancedConst.TOTAL_COUNT, "ceil(sum(increase(envoy_detailed_route_requests_total{<filter>}[<time_interval>s])))");
//        promTemplateMap.put(AdvancedConst.DURATION_AVG, "ceil(sum(rate(envoy_detailed_route_request_duration_milliseconds_sum{<filter>}[<time_interval>s]))/sum(rate(envoy_detailed_route_requests_total{<filter>}[<time_interval>s])))");
//        promTemplateMap.put(AdvancedConst.BAD_REQUEST, "ceil(sum(increase(envoy_detailed_route_requests_total{response_code=~\"4\\\\d{2}\",<filter>}[<time_interval>s])))");
//        promTemplateMap.put(AdvancedConst.FAILED_RATE, "sum(rate(envoy_detailed_route_requests_total{response_code=~\"5\\\\d{2}\",<filter>}[<time_interval>s]))/sum(rate(envoy_detailed_route_requests_total{<filter>}[<time_interval>s]))");
//        promTemplateMap.put(AdvancedConst.ERROR_REQUEST, "ceil(sum(increase(envoy_detailed_route_requests_total{response_code=~\"5\\\\d{2}\",<filter>}[<time_interval>s])))");
//        promTemplateMap.put(AdvancedConst.DURATION_95, "(histogram_quantile(0.95, sum by(<sumBy>, le) (increase(envoy_detailed_route_request_duration_milliseconds_bucket{<filter>}[<time_interval>s]))))");
//        promTemplateMap.put(AdvancedConst.DURATION_99, "(histogram_quantile(0.99, sum by(<sumBy>, le) (increase(envoy_detailed_route_request_duration_milliseconds_bucket{<filter>}[<time_interval>s]))))");
//
//        promTemplateMap.put(AdvancedConst.RANK_FAILED_COUNT, "topk(<topN>,(sum by(<sumBy>)(increase(envoy_detailed_route_requests_total{response_code=~\"5\\\\d{2}\",<filter>}[<time_interval>s]))))");
//        promTemplateMap.put(AdvancedConst.RANK_BAD_REQUEST_COUNT, "topk(<topN>,(sum by(<sumBy>)(increase(envoy_detailed_route_requests_total{response_code=~\"4\\\\d{2}\",<filter>}[<time_interval>s]))))");
//        promTemplateMap.put(AdvancedConst.RANK_TOTAL_COUNT, "topk(<topN>,(sum by(<sumBy>)(increase(envoy_detailed_route_requests_total{<filter>}[<time_interval>s]))))");
//    }
//
//    @Override
//    public Map<String, List<MetricDataDto>> describeMetricDatas(MetricDataQueryDto query) {
//        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(query.getVirtualGwId());
//        if (virtualGatewayDto == null){
//            return null;
//        }
//        logger.info("metric = {} ", JSON.toJSONString(query));
//        GatewayDto gatewayDto1 = gatewayService.get(virtualGatewayDto.getGwId());
//        logger.info("gatewayDto1 = {}" , JSON.toJSONString(gatewayDto1));
//        GatewayAdvancedDto gatewayDto = ((GatewayAdvancedDto) gatewayDto1);
//        if (gatewayDto == null){
//            return null;
//        }
//
//        Map<String, String> target = Maps.newHashMap();
//        PromUtils.PromExpTemplate promExpTemplate = new PromUtils.PromExpTemplate();
//        target.put("cluster_name", gatewayDto.getGwClusterName());
//        target.put("virtual_gateway_code", CommonUtil.genGatewayStrForRoute(virtualGatewayDto));
//        DimensionType dimension = DimensionType.getByDimensionType(query.getDimensionType());
//        if (dimension == null) {
//            logger.info("未找到维度类型对应的数据，dimensionType = {}", query.getDimensionType());
//            return null;
//        }
//        List<String> sumBy = Lists.newArrayList();
//        sumBy.add("cluster_name");
//        sumBy.add("virtual_gateway_code");
//        promExpTemplate.baseParams = PromUtils.params().p("time_interval", query.getStep()).p("step", query.getStep());
//        if (DimensionType.SERVICE.equals(dimension)) {
//            sumBy.add("service_name");
//            target.put("service_name", String.valueOf(query.getDimensionId()));
//        } else if (DimensionType.ROUTE.equals(dimension)) {
//            RouteDto routeDto = routeService.get(query.getDimensionId());
//            if (routeDto == null) {
//                return null;
//            }
//            sumBy.add("route_rule_id");
//            target.put("route_rule_id", routeDto.getName());
//        } else if (query.getProjectDivided()) {
//            sumBy.add("project_id");
//            target.put("project_id", String.valueOf(ProjectTraceHolder.getProId()));
//        }
//        Map<String, List<MetricDataDto>> result = Maps.newHashMap();
//
//        promExpTemplate.baseParams.p("sumBy", StringUtils.join(sumBy, ","));
//        for (String metricType : query.getMetricTypes()) {
//            if (promTemplateMap.containsKey(metricType)) {
//                promExpTemplate.template =promTemplateMap.get(metricType);
//                String promQL = PromUtils.makeExpr(promExpTemplate, (List) Lists.newArrayList((Object[]) new Map[]{target}));
//                Map<String, List<MetricDataDto>> metricData = PromUtils.getMetricDataForProm(metricType, gatewayDto.getMetricUrl(), promQL, query.getStartTime(), query.getEndTime(), query.getStep());
//                result.putAll(metricData);
//            }
//        }
//        return result;
//    }

    @Override
    public Map<String, List<MetricDataDto>> describeMetricData(MetricDataQuery query) {
        DimensionType dimension = DimensionType.getByDimensionType(query.getDimensionType());
        AbstractMetricBuilder builder = dimension.getBuilder();
        return builder.metrics(query);
    }

    @Override
    public Map<String, List<MetricRankDto>> getServiceRank(RankDataQuery query) {
        DimensionType service = DimensionType.SERVICE;
        AbstractMetricBuilder builder = service.getBuilder();
        Map<String, List<MetricRankDto>> rank = builder.rank(query);
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.getByCode(query.getVirtualGwCode());
        if (virtualGatewayDto != null) {
            List<ServiceProxyDto> serviceProxy = serviceProxyService.getServiceProxy(
                    ServiceProxyQuery.builder()
                            .projectId(query.getProjectId()).
                            virtualGwId(virtualGatewayDto.getId())
                            .build());
            Map<String, Long> serviceMap = serviceProxy.stream()
                    .collect(Collectors.toMap(ServiceProxyDto::getName, ServiceProxyDto::getId));
            rank.values().stream()
                    .flatMap(Collection::stream)
                    .forEach(a -> a.setId(serviceMap.getOrDefault(a.getName(), NumberUtils.LONG_ZERO)));
        }
        return rank;
    }


    @Override
    public MetricStatisticsDto describeMetricStatistics(CountDataQuery query) {
        MetricStatisticsDto metricStatistics = new MetricStatisticsDto();
        String[] metricTypes = {
                AdvancedConst.SUCCESS_COUNT,
                AdvancedConst.BAD_REQUEST,
                AdvancedConst.ERROR_REQUEST,
                AdvancedConst.DURATION_AVG};
        query.setMetricTypes(metricTypes);
        DimensionType dimension = DimensionType.getByDimensionType(query.getDimensionType());
        if(dimension == null){
            return metricStatistics;
        }
        AbstractMetricBuilder builder = dimension.getBuilder();
        Map<String, Long> countMap = builder.count(query);
        //success count 2xx
        metricStatistics.setSuccessCount(countMap.get(AdvancedConst.SUCCESS_COUNT));
        metricStatistics.setFailedCount(countMap.get(AdvancedConst.ERROR_REQUEST));
        metricStatistics.setBadRequestCount(countMap.get(AdvancedConst.BAD_REQUEST));
        metricStatistics.setAverageDuration(countMap.get(AdvancedConst.DURATION_AVG).intValue());
        return metricStatistics;

    }

    @Override
    public ErrorCode validMetricQueryParam(MetricDataQueryDto query) {
        DimensionType dimensionType = DimensionType.getByDimensionType(query.getDimensionType());
        if (dimensionType == null) {
            logger.info("维度类型不符合预期，dimensionType = {}", query.getDimensionType());
            return AdvanceErrorCode.invalidParameterDimensionType(query.getDimensionType());
        }
        if (DimensionType.GATEWAY.equals(dimensionType)) {
            GatewayDto gatewayDto = gatewayService.getByClusterName(query.getDimensionCode());
            if (gatewayDto == null) {
                return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
            }
            return CommonErrorCode.SUCCESS;
        }
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.getByCode(query.getVirtualGwCode());
        if (virtualGateway == null) {
            logger.info("虚拟网关未找到");
            return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }
}
