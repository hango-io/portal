//package org.hango.cloud.envoy.advanced.metric.hooker;
//
//import com.google.common.collect.Lists;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.tuple.MutableTriple;
//import org.apache.commons.lang3.tuple.Triple;
//import org.hango.cloud.envoy.advanced.metric.dto.MetricDataDto;
//import org.hango.cloud.envoy.advanced.metric.dto.MetricDataQueryDto;
//import org.hango.cloud.envoy.advanced.metric.dto.MetricRankDto;
//import org.hango.cloud.envoy.advanced.metric.dto.MetricStatisticsDto;
//import org.hango.cloud.envoy.advanced.metric.meta.CountDataQuery;
//import org.hango.cloud.envoy.advanced.metric.meta.MetricDataQuery;
//import org.hango.cloud.envoy.advanced.metric.meta.RankDataQuery;
//import org.hango.cloud.envoy.advanced.metric.service.impl.MetricServiceImpl;
//import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
//import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
//import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
//import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
//import org.hango.cloud.common.infra.base.invoker.MethodAroundHolder;
//import org.hango.cloud.common.infra.base.meta.CommonExtension;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
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
//public abstract class AbstractMetricHooker extends AbstractInvokeHooker<CommonExtension, CommonExtensionDto> {
//
//    private static final Logger logger = LoggerFactory.getLogger(AbstractMetricHooker.class);
//
//    @Override
//    public Class aimAt() {
//        return MetricServiceImpl.class;
//    }
//
//    @Override
//    public int getOrder() {
//        return 0;
//    }
//
//
//    @Override
//    protected List<Triple<String, String, String>> put() {
//        List<Triple<String, String, String>> triples = Lists.newArrayList();
//        triples.add(MutableTriple.of("describeMetricData", StringUtils.EMPTY, "doDescribeMetricData"));
//        triples.add(MutableTriple.of("getServiceRank", StringUtils.EMPTY, "doGetServiceRank"));
//        triples.add(MutableTriple.of("describeMetricStatistics", StringUtils.EMPTY, "doDescribeMetricStatistics"));
//        triples.add(MutableTriple.of("validMetricQueryParam", StringUtils.EMPTY, "doValidMetricQueryParam"));
//        return triples;
//    }
//
//    /**
//     * 获取监控统计数据
//     *
//     * @param query
//     * @return
//     */
//    protected abstract MetricStatisticsDto describeMetricStatistics(CountDataQuery query);
//
//
//    /**
//     * 获取服务排行榜数据
//     *
//     * @param query
//     * @return
//     */
//    protected abstract Map<String, List<MetricRankDto>> getServiceRank(RankDataQuery query);
//
//    /**
//     * 获取监控趋势数据后置
//     *
//     * @param query
//     * @return
//     */
//    protected abstract Map<String, List<MetricDataDto>> describeMetricData(MetricDataQuery query);
//
//    /**
//     * 校验监控趋势查询参数
//     *
//     * @param query
//     * @return
//     */
//    protected ErrorCode validMetricQueryParam(MetricDataQueryDto query) {
//        return CommonErrorCode.SUCCESS;
//    }
//
//    ;
//
//    /**
//     * 执行获取监控统计数据后置Hook
//     */
//    @SuppressWarnings("unused")
//    public final Object doDescribeMetricStatistics(Object returnData) {
//        logger.debug("execute post describeMetricStatistics hook ,hook is {}", this.getClass().getName());
//        if (nextHooker != null && nextHooker instanceof AbstractMetricHooker) {
//            ((AbstractMetricHooker) nextHooker).doDescribeMetricStatistics(returnData);
//        }
//        CountDataQuery query = MethodAroundHolder.getNextParam(CountDataQuery.class);
//        return describeMetricStatistics(query);
//    }
//
//    /**
//     * 执行获取服务排行榜数据后置Hook
//     */
//    @SuppressWarnings("unused")
//    public final Object doGetServiceRank(Object returnData) {
//        logger.debug("execute post describeMetricStatistics hook ,hook is {}", this.getClass().getName());
//        if (nextHooker != null && nextHooker instanceof AbstractMetricHooker) {
//            ((AbstractMetricHooker) nextHooker).doGetServiceRank(returnData);
//        }
//        RankDataQuery query = MethodAroundHolder.getNextParam(RankDataQuery.class);
//        return getServiceRank(query);
//    }
//
//    /**
//     * 执行获取监控趋势数据后置Hook
//     */
//    @SuppressWarnings("unused")
//    public final Object doDescribeMetricData(Object returnData) {
//        logger.debug("execute post DescribeMetricData hook ,hook is {}", this.getClass().getName());
//        if (nextHooker != null && nextHooker instanceof AbstractMetricHooker) {
//            ((AbstractMetricHooker) nextHooker).doDescribeMetricData(returnData);
//        }
//        MetricDataQuery query = MethodAroundHolder.getNextParam(MetricDataQuery.class);
//        return describeMetricData(query);
//    }
//
//    /**
//     * 执行校验监控趋势查询参数后置
//     *
//     * @param returnCode
//     * @return
//     */
//    @SuppressWarnings("unused")
//    public final Object doValidMetricQueryParam(ErrorCode returnCode) {
//        if (!CommonErrorCode.SUCCESS.equals(returnCode)) {
//            return returnCode;
//        }
//        logger.debug("execute validMetricQueryParam hook ,hook is {}", this.getClass().getName());
//        if (nextHooker != null && nextHooker instanceof AbstractMetricHooker) {
//            ((AbstractMetricHooker) nextHooker).doValidMetricQueryParam(returnCode);
//            if (!CommonErrorCode.SUCCESS.equals(returnCode)) {
//                return returnCode;
//            }
//        }
//        MetricDataQueryDto query = MethodAroundHolder.getNextParam(MetricDataQueryDto.class);
//        return validMetricQueryParam(query);
//    }
//
//
//}
