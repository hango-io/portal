package org.hango.cloud.common.advanced.metric.controller;

import com.alibaba.fastjson.JSON;
import org.hango.cloud.common.advanced.metric.dto.CountDataQueryDto;
import org.hango.cloud.common.advanced.metric.dto.MetricDataDto;
import org.hango.cloud.common.advanced.metric.dto.MetricDataQueryDto;
import org.hango.cloud.common.advanced.metric.dto.MetricRankDto;
import org.hango.cloud.common.advanced.metric.dto.MetricStatisticsDto;
import org.hango.cloud.common.advanced.metric.dto.RankDataQueryDto;
import org.hango.cloud.common.advanced.metric.service.IMetricService;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/10
 */
@RestController
@RequestMapping(value = "/v1/metric")
public class MetricController extends AbstractController {


    @Autowired
    private IMetricService metricService;


    @RequestMapping(params = {"Action=DescribeMetricData"}, method = RequestMethod.GET)
    public String describeMetricData(@Validated MetricDataQueryDto query) {

        logger.info("开始查询监控数据，MetricDataQueryDto = {}", JSON.toJSONString(query));
        ErrorCode errorCode = metricService.validMetricQueryParam(query);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            logger.info("查询失败，ErrorCode = {}", JSON.toJSONString(errorCode));
            return apiReturn(errorCode);
        }

        Map<String, List<MetricDataDto>> metricData = metricService.describeMetricData(metricService.transMetric(query));

        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, metricData);
        return apiReturnSuccess(result);

    }

    /**
     * 获取服务调用统计
     *
     * @param query
     * @return
     */
    @GetMapping(params = {"Action=DescribeMetricStatistics"})
    public Object getAuditStatistics(@Validated CountDataQueryDto query) {
        logger.info("开始查询统计数据，CountQueryDto = {}", JSON.toJSONString(query));
        ErrorCode errorCode = metricService.validStatisticsParam(query);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        MetricStatisticsDto metricStatisticsDto = metricService.describeMetricStatistics(metricService.transCount(query));
        return apiReturnSuccess(metricStatisticsDto);
    }


    @GetMapping(params = {"Action=DescribeRank"})
    public String serviceCallRank(@Validated RankDataQueryDto query) {
        logger.info("开始服务调用排名，RankDataQueryDto = {}", JSON.toJSONString(query));
        ErrorCode errorCode = metricService.checkServiceRankQueryParam(query);
        if (errorCode != CommonErrorCode.SUCCESS) {
            return apiReturn(errorCode);
        }
        Map<String, List<MetricRankDto>> serviceRank = metricService.getServiceRank(metricService.transRank(query));
        return apiReturnSuccess(serviceRank);
    }
}