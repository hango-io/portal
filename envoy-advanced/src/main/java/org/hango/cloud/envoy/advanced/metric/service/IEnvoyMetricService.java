package org.hango.cloud.envoy.advanced.metric.service;

import org.hango.cloud.envoy.advanced.metric.dto.CountDataQueryDto;
import org.hango.cloud.envoy.advanced.metric.dto.MetricDataDto;
import org.hango.cloud.envoy.advanced.metric.dto.MetricDataQueryDto;
import org.hango.cloud.envoy.advanced.metric.dto.MetricRankDto;
import org.hango.cloud.envoy.advanced.metric.dto.MetricStatisticsDto;
import org.hango.cloud.envoy.advanced.metric.dto.RankDataQueryDto;
import org.hango.cloud.envoy.advanced.metric.meta.CountDataQuery;
import org.hango.cloud.envoy.advanced.metric.meta.MetricDataQuery;
import org.hango.cloud.envoy.advanced.metric.meta.RankDataQuery;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/10
 */
public interface IEnvoyMetricService {

    /**
     * 获取监控数据
     *
     * @param query
     * @return
     */
    Map<String, List<MetricDataDto>> describeMetricData(MetricDataQuery query);

    /**
     * 获取服务排行
     *
     * @param query
     * @return 服务排行
     */
    Map<String,List<MetricRankDto>> getServiceRank(RankDataQuery query);


    /**
     * 查看流量统计
     *
     * @param query
     * @return
     */
    MetricStatisticsDto describeMetricStatistics(CountDataQuery query);

    /**
     * 验证查询参数
     *
     * @param query
     * @return
     */
    ErrorCode validMetricQueryParam(MetricDataQueryDto query);

    /**
     * 转换查询参数
     *
     * @param query
     * @return
     */
    MetricDataQuery transMetric(MetricDataQueryDto query);

    /**
     * 排行榜参数校验
     *
     * @param query
     * @return
     */
    RankDataQuery transRank(RankDataQueryDto query);

    /**
     * 统计查询参数校验
     *
     * @param query
     * @return
     */
    CountDataQuery transCount(CountDataQueryDto query);

    /**
     * 参数校验
     *
     * @param query
     * @return
     */
    ErrorCode checkServiceRankQueryParam(RankDataQueryDto query);


    /**
     * 参数校验
     *
     * @param query
     * @return
     */
    ErrorCode validStatisticsParam(CountDataQueryDto query);
}
