package org.hango.cloud.dashboard.apiserver.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.dto.alertdto.MetricDataDto;
import org.hango.cloud.dashboard.apiserver.meta.DimensionType;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.MetricTypeEnum;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.ServiceRankInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IMetricService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.PromUtils;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.meta.PromResponse;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/28
 */
@Service
public class MetricServiceImpl implements IMetricService {

    public static final Map<String, String> promTemplateMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(MetricServiceImpl.class);
    /**
     * 监控名称模板gateway:{DimensionType}:{MetricType}:minute:
     */
    private static final String METRIC_NAME_TEMPLATE = "gateway:%s:%s:minute";

    static {
        promTemplateMap.put(Const.QPS, "ceil(sum(increase(envoy_cluster_upstream_rq_total{<filter>}[<time_interval>s]))/<step>)");
        promTemplateMap.put(Const.TOTAL_COUNT, "ceil(sum(increase(envoy_cluster_upstream_rq_xx{<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.DURATION_AVG, "ceil(sum(rate(envoy_cluster_upstream_rq_time_sum{<filter>}[<time_interval>s]))/sum(rate(envoy_cluster_upstream_rq_time_count{<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.BAD_REQUEST, "ceil(sum(increase(envoy_cluster_upstream_rq_xx{envoy_response_code_class=~\"4\",<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.FAILED_RATE, "sum(rate(envoy_cluster_upstream_rq_xx{envoy_response_code_class=~\"5\",<filter>}[<time_interval>s]))/sum(rate(envoy_cluster_upstream_rq_xx{<filter>}[<time_interval>s]))");
        promTemplateMap.put(Const.ERROR_REQUEST, "ceil(sum(increase(envoy_cluster_upstream_rq_xx{envoy_response_code_class=~\"5\",<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.HYSTRIX, "ceil(sum(rate(envoy_cluster_circuit_breakers_remaining_rq{<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.RANK_FAILED_COUNT, "sort_desc(sum by(<sumBy>)(increase(envoy_cluster_upstream_rq_xx{envoy_response_code_class=~\"5\",<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.RANK_BAD_REQUEST_COUNT, "sort_desc(sum by(<sumBy>)(increase(envoy_cluster_upstream_rq_xx{envoy_response_code_class=~\"4\",<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.RANK_TOTAL_COUNT, "sort_desc(sum by(<sumBy>)(increase(envoy_cluster_upstream_rq_xx{<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.DURATION_95, "(histogram_quantile(0.95, sum by(<sumBy>, le) (increase(envoy_cluster_upstream_rq_time_bucket{<filter>}[<time_interval>s]))))");
        promTemplateMap.put(Const.DURATION_99, "(histogram_quantile(0.99, sum by(<sumBy>, le) (increase(envoy_cluster_upstream_rq_time_bucket{<filter>}[<time_interval>s]))))");

    }

    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IRouteRuleProxyService envoyRouteRuleProxyService;

    private static Map<String, List<MetricDataDto>> getDefaultTurbineData(long start, long end, long step, String... metricType) {
        if (ArrayUtils.isEmpty(metricType)) {
            return Collections.emptyMap();
        }
        Map<String, List<MetricDataDto>> result = new HashMap<>();
        for (String mt : metricType) {
            result.put(mt, initMetricData(start, end, step));
        }
        return result;
    }

    /**
     * 格式化数据
     *
     * @return
     */
    private static List<MetricDataDto> formMetricDataForProm(long start, long end, long step, List<PromResponse.Data.Result> resultList) {

        List<MetricDataDto> metricDataDtoList = initMetricData(start, end, step);
        if (CollectionUtils.isEmpty(resultList)) {
            return metricDataDtoList;
        }
        for (PromResponse.Data.Result result : resultList) {
            List<List<String>> values = result.getValues();
            int t = 1;
            for (int i = metricDataDtoList.size() - 1; i >= 0; i--) {
                int index = values.size() - t;
                if (index < 0) {
                    break;
                }
                String mVal = values.get(index).get(1);
                metricDataDtoList.get(i).setMetricValue("NaN".equals(mVal) ? "0" : mVal);
                t++;
            }
        }
        return metricDataDtoList;
    }

    /**
     * 初始数据
     *
     * @return
     */
    private static List<MetricDataDto> initMetricData(long start, long end, long step) {
        List<MetricDataDto> metricDataDtoList = new ArrayList<>();
        long timeInterval = end - start;
        SimpleDateFormat simpleDateFormat = null;
        if (timeInterval > Const.MS_OF_DAY) {
            simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm");
        } else {
            simpleDateFormat = new SimpleDateFormat("HH:mm");
        }
        long timeIntervalForSecond = timeInterval / 1000;
        long frequency = timeIntervalForSecond / step + 1;
        for (int i = 0; i < frequency; i++) {
            long calKey = end - i * step * 1000;
            metricDataDtoList.add(new MetricDataDto(simpleDateFormat.format(new Date(calKey)), "0.0"));
        }
        return Lists.reverse(metricDataDtoList);
    }

    @Override
    public List<ServiceRankInfo> getServiceRank(long startTime, long endTime, GatewayInfo gatewayInfo, long projectId, String rankType) {
        if (!promTemplateMap.containsKey(rankType)) {
            return Collections.emptyList();
        }
        Map<String, String> target = Maps.newHashMap();
        PromUtils.PromExpTemplate promExpTemplate = new PromUtils.PromExpTemplate();
        promExpTemplate.baseParams = PromUtils.params().p("time_interval", (endTime - startTime) / 1000)
                .p("sumBy", "envoy_cluster_name");
        target.put("cluster_name", gatewayInfo.getGwClusterName());
        List<ServiceProxyInfo> serviceProxyInfoList = serviceProxyService.getEnvoyServiceProxy(gatewayInfo.getId(), NumberUtils.LONG_ZERO, projectId, 0, 1000);
        if (CollectionUtils.isEmpty(serviceProxyInfoList)) {
            return Collections.emptyList();
        }
        Map<String, String> service = serviceProxyInfoList.stream().map(e -> serviceInfoService.getServiceByServiceId(e.getServiceId()))
                .collect(Collectors.toMap(ServiceInfo::getServiceName, ServiceInfo::getDisplayName));
        target.put("envoy_cluster_name", StringUtils.join(service.keySet(), "|"));
        promExpTemplate.template = promTemplateMap.get(rankType);

        String promQL = PromUtils.makeExpr(promExpTemplate, Lists.newArrayList(target));

        String queryUrl = gatewayInfo.getPromAddr() + "/api/v1/query";
        Map<String, Object> queryParam = Maps.newHashMap();
        queryParam.put("query", promQL);
        queryParam.put("time", String.valueOf(endTime / 1000));
        PromResponse promResponse = PromUtils.readPromData(queryUrl, queryParam);
        if (promResponse == null || CollectionUtils.isEmpty(promResponse.getData().getResult())) {
            logger.warn("查询数据为空");
            return formatServiceRnkData(Collections.emptyList(), service);
        }
        return formatServiceRnkData(promResponse.getData().getResult(), service);
    }

    private List<ServiceRankInfo> formatServiceRnkData(List<PromResponse.Data.Result> resultList, Map<String, String> service) {
        List<ServiceRankInfo> serviceRankInfoList = resultList.stream().map(r -> {
            String serviceTag = r.getMetric().get("envoy_cluster_name");
            String name = service.get(serviceTag);
            service.remove(serviceTag);
            return new ServiceRankInfo(name, ((int) NumberUtils.toDouble(r.getValue().get(1))));
        }).collect(Collectors.toList());
        for (String value : service.values()) {
            serviceRankInfoList.add(new ServiceRankInfo(value, NumberUtils.INTEGER_ZERO));
        }
        return serviceRankInfoList;
    }

    @Override
    public ErrorCode validMetricQueryParam(String dimensionType, long dimensionId, GatewayInfo gatewayInfo, String... metricTypes) {
        if (DimensionType.getByTarget(dimensionType) == null) {
            logger.info("维度类型不符合预期，dimensionType = {}", dimensionType);
            return CommonErrorCode.InvalidParameterDimensionType(dimensionType);
        }
        if (!ArrayUtils.isNotEmpty(metricTypes)) {
            logger.info("查询类型为空");
            return CommonErrorCode.MissingParameterMetricType;
        }

        for (String metricType : metricTypes) {
            MetricTypeEnum metricTypeEnum = MetricTypeEnum.get(metricType);
            if (metricTypeEnum == null) {
                return CommonErrorCode.InvalidParameterMetricType(metricType);
            }
        }
        return validPublishInfo(dimensionType, dimensionId, gatewayInfo);
    }

    private ErrorCode validPublishInfo(String dimensionType, long dimensionId, GatewayInfo gatewayInfo) {
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType())) {
            if (DimensionType.SERVICE.getTarget().equals(dimensionType)) {
                ServiceProxyInfo serviceProxy = serviceProxyService.getServiceProxyByServiceIdAndGwId(gatewayInfo.getId(), dimensionId);
                if (serviceProxy == null) {
                    logger.info("该服务暂未发布 , ServiceId = {}", dimensionId);
                    return CommonErrorCode.ServiceNotPublished;
                }
            } else if (DimensionType.API.getTarget().equals(dimensionType)) {
                RouteRuleProxyInfo routeRuleProxy = envoyRouteRuleProxyService.getRouteRuleProxy(gatewayInfo.getId(), dimensionId);
                if (routeRuleProxy == null) {
                    logger.info("该API暂未发布 , ServiceId = {}", dimensionId);
                    return CommonErrorCode.NotPublishedApi;
                }
            }
        }
        return CommonErrorCode.Success;
    }

    @Override
    public Map<String, List<MetricDataDto>> describeMetricData(String dimensionType, long dimensionId, GatewayInfo gatewayInfo, long start, long end, long step, boolean projectDivided, String... metricTypes) {
        if (ArrayUtils.isEmpty(metricTypes) || gatewayInfo == null) {
            return Collections.emptyMap();
        }
        Map<String, List<MetricDataDto>> metricData = null;
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType())) {
            metricData = describeMetricDataForEnvoy(dimensionType, dimensionId, gatewayInfo, start, end, step, projectDivided, metricTypes);
        }
        return metricData;
    }

    private Map<String, List<MetricDataDto>> describeMetricDataForEnvoy(String dimensionType, long dimensionId, GatewayInfo gatewayInfo, long start, long end, long step, boolean projectDivided, String... metricTypes) {
        Map<String, List<MetricDataDto>> result = Maps.newHashMap();
        Map<String, String> target = Maps.newHashMap();
        PromUtils.PromExpTemplate promExpTemplate = new PromUtils.PromExpTemplate();
        target.put("cluster_name", gatewayInfo.getGwClusterName());

        List<String> sumBy = Lists.newArrayList();
        sumBy.add("cluster_name");
        promExpTemplate.baseParams = PromUtils.params().p("time_interval", step)
                .p("step", step);
        if (DimensionType.SERVICE.getTarget().equals(dimensionType)) {
            ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(dimensionId);
            if (serviceInfo != null) {
                sumBy.add("envoy_cluster_name");
                target.put("envoy_cluster_name", serviceInfo.getServiceName());
            }
        } else {
            List<ServiceProxyInfo> serviceProxyInfoList = null;
            if (projectDivided) {
                serviceProxyInfoList = serviceProxyService.getEnvoyServiceProxy(gatewayInfo.getId(), NumberUtils.LONG_ZERO, ProjectTraceHolder.getProId(), 0, Integer.MAX_VALUE);
            } else {
                serviceProxyInfoList = serviceProxyService.getServiceProxyListByGwId(gatewayInfo.getId());
            }
            if (CollectionUtils.isEmpty(serviceProxyInfoList)) {
                for (String metricType : metricTypes) {
                    result.put(metricType, initMetricData(start, end, step));
                }
                return result;
            }
            target.put("envoy_cluster_name", StringUtils.join(serviceProxyInfoList.stream().map(
                            e -> serviceInfoService.getServiceByServiceId(e.getServiceId()).getServiceName())
                    .collect(Collectors.toList()), "|"));
        }
        promExpTemplate.baseParams.p("sumBy", StringUtils.join(sumBy, ","));
        for (String metricType : metricTypes) {
            if (!promTemplateMap.containsKey(metricType)) {
                continue;
            }
            promExpTemplate.template = promTemplateMap.get(metricType);
            String promQL = PromUtils.makeExpr(promExpTemplate, Lists.newArrayList(target));
            Map<String, List<MetricDataDto>> metricData = getMetricDataForProm(metricType, gatewayInfo.getPromAddr(), promQL, start, end, step);
            result.putAll(metricData);
        }

        return result;
    }

    private Map<String, List<MetricDataDto>> getMetricDataForProm(String metricType, String promUrl, String promQL, long start, long end, long step) {
        String queryUrl = promUrl + "/api/v1/query_range";
        Map<String, Object> queryParam = Maps.newHashMap();

        queryParam.put("query", promQL);

        Map<String, List<MetricDataDto>> resultMap = Maps.newHashMap();

        queryParam.put("start", String.valueOf(start / 1000));
        queryParam.put("end", String.valueOf(end / 1000));
        queryParam.put("step", String.valueOf(step));
        PromResponse promResponse = PromUtils.readPromData(queryUrl, queryParam);
        if (promResponse == null) {
            logger.warn("查询数据为空");
            resultMap.put(metricType, initMetricData(start, end, step));
            return resultMap;
        }
        resultMap.put(metricType, formMetricDataForProm(start, end, step, promResponse.getData().getResult()));
        return resultMap;
    }
}
