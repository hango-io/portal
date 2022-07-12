package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.dto.alertdto.MetricDataDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.ServiceRankInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/28
 */
public interface IMetricService {


    /**
     * 获取监控数据
     *
     * @param dimensionType 维度类型 Gateway/Service/Api
     * @param dimensionId   维度ID
     * @param metricTypes   监控类型，QPS/DURATION_AVG/FAILED_RATE/BAD_REQUEST/ERROR_REQUEST/HYSTRIX/TRAFFIC_CONTROL,对应指标为QPS、平均时延、请求失败率、4xx 数、5xx 数、熔断数、限流数
     * @param gatewayInfo   发布网关信息
     * @param start
     * @param end
     * @param step
     * @return
     * @projectDivided 是否区分项目
     */
    Map<String, List<MetricDataDto>> describeMetricData(String dimensionType, long dimensionId, GatewayInfo gatewayInfo, long start, long end, long step, boolean projectDivided, String... metricTypes);

    /**
     * 获取服务排行
     *
     * @param startTime
     * @param endTime
     * @param projectId
     * @param gatewayInfo
     * @param rankType
     * @return 服务排行
     */
    List<ServiceRankInfo> getServiceRank(long startTime, long endTime, GatewayInfo gatewayInfo, long projectId, String rankType);

    /**
     * 参数校验
     *
     * @param dimensionType
     * @param dimensionId
     * @param gatewayInfo
     * @param metricTypes
     * @return 校验信息
     */
    ErrorCode validMetricQueryParam(String dimensionType, long dimensionId, GatewayInfo gatewayInfo, String... metricTypes);
}
