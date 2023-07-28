package org.hango.cloud.envoy.advanced.metric.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.advanced.base.meta.AdvanceErrorCode;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.common.advanced.gateway.dto.GatewayAdvancedDto;
import org.hango.cloud.common.advanced.gateway.service.IGatewayAdvancedService;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGatewayQuery;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.advanced.metric.dto.CountDataQueryDto;
import org.hango.cloud.envoy.advanced.metric.dto.MetricDataDto;
import org.hango.cloud.envoy.advanced.metric.dto.MetricDataQueryDto;
import org.hango.cloud.envoy.advanced.metric.dto.MetricRankDto;
import org.hango.cloud.envoy.advanced.metric.dto.MetricStatisticsDto;
import org.hango.cloud.envoy.advanced.metric.dto.RankDataQueryDto;
import org.hango.cloud.envoy.advanced.metric.meta.CountDataQuery;
import org.hango.cloud.envoy.advanced.metric.meta.DimensionType;
import org.hango.cloud.envoy.advanced.metric.meta.MetricDataQuery;
import org.hango.cloud.envoy.advanced.metric.meta.MetricTypeEnum;
import org.hango.cloud.envoy.advanced.metric.meta.RankDataQuery;
import org.hango.cloud.envoy.advanced.metric.service.IEnvoyMetricService;
import org.hango.cloud.envoy.advanced.metric.service.builder.AbstractMetricBuilder;
import org.hango.cloud.gdashboard.api.util.Const;
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
    private IServiceProxyService serviceProxyService;


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
        for (String metricType : query.getMetricTypes()) {
            MetricTypeEnum metricTypeEnum = MetricTypeEnum.get(metricType);
            if (metricTypeEnum == null) {
                return AdvanceErrorCode.invalidParameterMetricType(metricType);
            }
        }
        ErrorCode errorCode = validTimeParam(query.getStartTime(), query.getEndTime());
        if (errorCode != CommonErrorCode.SUCCESS) {
            return errorCode;
        }
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

    @Override
    public MetricDataQuery transMetric(MetricDataQueryDto query) {
        MetricDataQuery metricDataQuery = new MetricDataQuery();
        metricDataQuery.setStartTime(query.getStartTime());
        metricDataQuery.setEndTime(query.getEndTime());
        metricDataQuery.setStep(query.getStep());
        metricDataQuery.setMetricTypes(query.getMetricTypes());
        metricDataQuery.setDimensionType(query.getDimensionType());
        metricDataQuery.setDimensionCode(query.getDimensionCode());
        if (BaseConst.GATEWAY.equals(query.getDimensionType())){
            metricDataQuery.setClusterName(query.getDimensionCode());
            GatewayDto gatewayDto = gatewayService.getByClusterName(query.getDimensionCode());
            GatewayAdvancedDto gatewayAdvancedDto = gatewayAdvancedService.get(gatewayDto.getId());
            metricDataQuery.setClusterName(gatewayDto.getGwClusterName());
            metricDataQuery.setMetricAddress(gatewayAdvancedDto.getMetricUrl());
        }else {
            VirtualGatewayDto virtualGateway = virtualGatewayInfoService.getByCode(query.getVirtualGwCode());
            GatewayAdvancedDto gatewayDto = ((GatewayAdvancedDto) gatewayService.get(virtualGateway.getGwId()));
            metricDataQuery.setClusterName(virtualGateway.getGwClusterName());
            metricDataQuery.setMetricAddress(gatewayDto.getMetricUrl());
            metricDataQuery.setVirtualGwCode(virtualGateway.getCode());
            metricDataQuery.setVirtualGatewayPort(virtualGateway.getPort());
        }
        return metricDataQuery;
    }

    @Override
    public CountDataQuery transCount(CountDataQueryDto query) {
        CountDataQuery countQuery = new CountDataQuery();
        countQuery.setStartTime(query.getStartTime());
        countQuery.setEndTime(query.getEndTime());
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.getByCode(query.getVirtualGwCode());
        GatewayAdvancedDto gatewayDto = ((GatewayAdvancedDto) gatewayService.get(virtualGateway.getGwId()));
        countQuery.setClusterName(virtualGateway.getGwClusterName());
        countQuery.setMetricAddress(gatewayDto.getMetricUrl());
        countQuery.setVirtualGatewayPort(virtualGateway.getPort());
        countQuery.setDimensionCode(query.getDimensionCode());
        countQuery.setDimensionType(query.getDimensionType());
        countQuery.setVirtualGwCode(query.getVirtualGwCode());
        return countQuery;
    }

    @Override
    public RankDataQuery transRank(RankDataQueryDto query) {
        RankDataQuery rankQuery = new RankDataQuery();
        rankQuery.setRankFields(query.getDimensionTypes());
        rankQuery.setTopN(query.getTopN());
        rankQuery.setMetricTypes(query.getMetricTypes());
        rankQuery.setStartTime(query.getStartTime());
        rankQuery.setEndTime(query.getEndTime());
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.getByCode(query.getVirtualGwCode());
        GatewayAdvancedDto gatewayDto = ((GatewayAdvancedDto) gatewayService.get(virtualGateway.getGwId()));
        rankQuery.setClusterName(virtualGateway.getGwClusterName());
        rankQuery.setVirtualGwCode(virtualGateway.getCode());
        rankQuery.setVirtualGatewayPort(virtualGateway.getPort());
        rankQuery.setMetricAddress(gatewayDto.getMetricUrl());
        return rankQuery;
    }

    @Override
    public ErrorCode checkServiceRankQueryParam(RankDataQueryDto query) {
        return validCommonParam(query.getStartTime(), query.getEndTime(),query.getVirtualGwCode());
    }

    public ErrorCode validCommonParam(long startTime, long endTime ,String virtualGwCode) {
        ErrorCode errorCode = validTimeParam(startTime, endTime);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        Boolean exist = virtualGatewayInfoService.exist(VirtualGatewayQuery.builder().code(virtualGwCode).build());
        if (!exist) {
            logger.info("虚拟网关未找到");
            return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }

    public ErrorCode validTimeParam(long startTime, long endTime) {
        if (NumberUtils.INTEGER_ZERO == startTime || NumberUtils.INTEGER_ZERO == endTime) {
            endTime = System.currentTimeMillis();
            startTime = endTime - Const.MS_OF_HOUR;
        }
        if (endTime - startTime > AdvancedConst.QUERY_MAX_DAY * Const.MS_OF_DAY) {
            return CommonErrorCode.timeRangeTooLarge(String.valueOf(AdvancedConst.QUERY_MAX_DAY));
        }
        if (startTime >= endTime) {
            return CommonErrorCode.QUERY_TIME_ILLEGAL;
        }
        return CommonErrorCode.SUCCESS;
    }



    /**
     * 校验参数
     *
     * @param
     * @return
     */
    @Override
    public ErrorCode validStatisticsParam(CountDataQueryDto query) {
        return  validCommonParam(query.getStartTime(), query.getEndTime(),query.getVirtualGwCode());
    }
}
