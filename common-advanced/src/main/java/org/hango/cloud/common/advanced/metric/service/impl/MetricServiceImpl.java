package org.hango.cloud.common.advanced.metric.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.advanced.base.meta.AdvanceErrorCode;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.common.advanced.gateway.dto.GatewayAdvancedDto;
import org.hango.cloud.common.advanced.gateway.service.IGatewayAdvancedService;
import org.hango.cloud.common.advanced.metric.dto.CountDataQueryDto;
import org.hango.cloud.common.advanced.metric.dto.MetricDataDto;
import org.hango.cloud.common.advanced.metric.dto.MetricDataQueryDto;
import org.hango.cloud.common.advanced.metric.dto.MetricRankDto;
import org.hango.cloud.common.advanced.metric.dto.MetricStatisticsDto;
import org.hango.cloud.common.advanced.metric.dto.RankDataQueryDto;
import org.hango.cloud.common.advanced.metric.meta.CountDataQuery;
import org.hango.cloud.common.advanced.metric.meta.MetricDataQuery;
import org.hango.cloud.common.advanced.metric.meta.MetricTypeEnum;
import org.hango.cloud.common.advanced.metric.meta.RankDataQuery;
import org.hango.cloud.common.advanced.metric.service.IMetricService;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGatewayQuery;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/28
 */
@Service
public class MetricServiceImpl implements IMetricService {

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IGatewayAdvancedService gatewayAdvancedService;
    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;


    private static final Logger logger = LoggerFactory.getLogger(MetricServiceImpl.class);


    @Override
    public Map<String, List<MetricDataDto>> describeMetricData(MetricDataQuery query) {
        return Collections.emptyMap();
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
    public Map<String, List<MetricRankDto>> getServiceRank(RankDataQuery query) {
        return Collections.emptyMap();
    }

    @Override
    public ErrorCode validMetricQueryParam(MetricDataQueryDto query) {
        for (String metricType : query.getMetricTypes()) {
            MetricTypeEnum metricTypeEnum = MetricTypeEnum.get(metricType);
            if (metricTypeEnum == null) {
                return AdvanceErrorCode.invalidParameterMetricType(metricType);
            }
        }
        return validTimeParam(query.getStartTime(), query.getEndTime());
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

    @Override
    public MetricStatisticsDto describeMetricStatistics(CountDataQuery query) {
        return null;
    }


}


