package org.hango.cloud.envoy.advanced.metric.service;

import org.hango.cloud.common.advanced.metric.dto.MetricDataDto;
import org.hango.cloud.common.advanced.metric.dto.MetricDataQueryDto;
import org.hango.cloud.common.advanced.metric.dto.MetricRankDto;
import org.hango.cloud.common.advanced.metric.dto.MetricStatisticsDto;
import org.hango.cloud.common.advanced.metric.meta.CountDataQuery;
import org.hango.cloud.common.advanced.metric.meta.MetricDataQuery;
import org.hango.cloud.common.advanced.metric.meta.RankDataQuery;
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
}
