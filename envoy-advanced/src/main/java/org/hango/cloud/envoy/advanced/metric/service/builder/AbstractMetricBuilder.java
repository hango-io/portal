package org.hango.cloud.envoy.advanced.metric.service.builder;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.advanced.base.util.PromUtils;
import org.hango.cloud.common.advanced.metric.dto.MetricDataDto;
import org.hango.cloud.common.advanced.metric.dto.MetricRankDto;
import org.hango.cloud.common.advanced.metric.meta.CountDataQuery;
import org.hango.cloud.common.advanced.metric.meta.MetricBaseQuery;
import org.hango.cloud.common.advanced.metric.meta.MetricDataQuery;
import org.hango.cloud.common.advanced.metric.meta.MetricTypeEnum;
import org.hango.cloud.common.advanced.metric.meta.PromResponse;
import org.hango.cloud.common.advanced.metric.meta.RankDataQuery;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.envoy.advanced.metric.meta.DimensionType;
import org.hango.cloud.envoy.advanced.metric.service.MetricFunction;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/5/30
 */
public abstract class AbstractMetricBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMetricBuilder.class);

    private static final String SUM_BY = "sumBy";
    private static final String TIME_INTERVAL = "time_interval";
    private static final String QUERY = "query";
    private static final String START = "start";
    private static final String END = "end";
    private static final String STEP = "step";

    /**
     * 毫秒转秒换算单位
     */
    private static final long MILL_TO_SEC = 1000L;


    /**
     * 指标查询异步线程
     * 其中设置核心线程为查询指标的种类数，避免同时查询所有指标时出现线程不够，任务等待的场景
     *
     * @see MetricTypeEnum
     */
    private static final ExecutorService METRIC_THREAD = new ThreadPoolExecutor(
            MetricTypeEnum.values().length,
            MetricTypeEnum.values().length,
            NumberUtils.LONG_ZERO,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("metric-thread-%d").build());

    /**
     * 异步获取指定指标数据
     *
     * @param address  监控地址
     * @param query    多个查询条件，Map<METRIC_NAME,QUERY_CONDITION>
     * @param function 调用的查询方法
     * @param <T>      查询条件 QUERY_CONDITION
     * @param <R>      返回结果对象
     * @return 查询结果
     */
    final <T, R> Map<String, R> execute(String address, Map<String, T> query, MetricFunction<T, R> function) {
        Map<String, R> result = Maps.newHashMap();
        try {
            CountDownLatch latch = new CountDownLatch(query.size());
            Map<String, Future<R>> futures = Maps.newHashMap();
            for (Map.Entry<String, T> entry : query.entrySet()) {
                futures.put(entry.getKey(), METRIC_THREAD.submit(() -> {
                    R apply = function.apply(address, entry.getValue());
                    latch.countDown();
                    return apply;
                }));
            }
            latch.await();
            for (Map.Entry<String, Future<R>> entry : futures.entrySet()) {
                result.put(entry.getKey(), entry.getValue().get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取该维度的指标查询语句
     *
     * @param metricType 指标类型
     * @return 查询模板
     */
    public abstract String template(String metricType);

    /**
     * 获取查询该指标的聚合维度
     *
     * @return 聚合函数
     */
    protected List<String> sumBy() {
        return Lists.newArrayList();
    }

    /**
     * 获取查询该指标的查询label
     *
     * @param query 查询条件
     * @return 查询label
     */
    protected <T extends MetricBaseQuery> List<Map<String, String>> targets(T query) {
        return Lists.newArrayList(Maps.newHashMap());
    }

    /**
     * 获取监控趋势
     *
     * @param query
     * @return
     */
    public final Map<String, List<MetricDataDto>> metrics(MetricDataQuery query) {
        Map<String, Map<String, Object>> queryMaps = Maps.newHashMap();
        Map<String, Object> param = Maps.newHashMap();
        param.put(SUM_BY, StringUtils.join(sumBy(), BaseConst.SYMBOL_COMMA));
        param.put(TIME_INTERVAL, query.getStep());
        param.put(STEP, query.getStep());

        for (String metricType : query.getMetricTypes()) {
            Map<String, Object> queryMap = Maps.newHashMap();
            queryMap.put(QUERY, getPromQl(metricType, param, targets(query)));
            queryMap.put(START, String.valueOf(query.getStartTime() / MILL_TO_SEC));
            queryMap.put(END, String.valueOf(query.getEndTime() / MILL_TO_SEC));
            queryMap.put(STEP, String.valueOf(query.getStep()));
            queryMaps.put(metricType, queryMap);
        }
        return execute(query.getMetricAddress(), queryMaps, AbstractMetricBuilder::readMetricData);
    }

    /**
     * 获取排行榜
     *
     * @param query
     * @return
     */
    public final Map<String, List<MetricRankDto>> rank(RankDataQuery query) {
        Map<String, Map<String, Object>> queryMaps = Maps.newHashMap();
        Map<String, Object> param = Maps.newHashMap();
        if (query.getRankFields() == null) {
            return Collections.emptyMap();
        }
        //由于前方不知道Prometheus中指标的label 名称，因此，只能让前方传入维度名称，通过维度获取对应的label名称
        List<String> metrics = new ArrayList<>();
        for (String rankField : query.getRankFields()) {
            DimensionType dimension = DimensionType.getByDimensionType(rankField);
            if (dimension != null) {
                metrics.add(dimension.getMetricLabel());
            }
        }
        param.put(SUM_BY, StringUtils.join(metrics, BaseConst.SYMBOL_COMMA));
        param.put(TIME_INTERVAL, (query.getEndTime() - query.getStartTime()) / MILL_TO_SEC);
        param.put("topN", query.getTopN());

        for (String metricType : query.getMetricTypes()) {
            Map<String, Object> queryMap = Maps.newHashMap();
            queryMap.put(QUERY, getPromQl(metricType, param, targets(query)));
            queryMap.put(START, String.valueOf(query.getStartTime() / MILL_TO_SEC));
            queryMap.put(END, String.valueOf(query.getEndTime() / MILL_TO_SEC));
            queryMaps.put(metricType, queryMap);
        }
        return execute(query.getMetricAddress(), queryMaps, AbstractMetricBuilder::readRankData);
    }

    /**
     * 获取统计数据
     *
     * @param query
     * @return
     */
    public final Map<String, Long> count(CountDataQuery query) {
        Map<String, Map<String, Object>> queryMaps = Maps.newHashMap();
        Map<String, Object> param = Maps.newHashMap();
        param.put(SUM_BY, StringUtils.join(sumBy(), BaseConst.SYMBOL_COMMA));
        param.put(TIME_INTERVAL, (query.getEndTime() - query.getStartTime()) / MILL_TO_SEC);

        for (String metricType : query.getMetricTypes()) {
            Map<String, Object> queryMap = Maps.newHashMap();
            queryMap.put(QUERY, getPromQl(metricType, param, targets(query)));
            queryMap.put("time", String.valueOf(query.getEndTime() /MILL_TO_SEC));
            queryMaps.put(metricType, queryMap);
        }
        return execute(query.getMetricAddress(), queryMaps, AbstractMetricBuilder::readCountData);
    }

    private String getPromQl(String metricType, Map<String, Object> queryMap, List<Map<String, String>> targets) {
        PromUtils.PromExpTemplate promExpTemplate = new PromUtils.PromExpTemplate();
        promExpTemplate.template = template(metricType);
        promExpTemplate.baseParams = PromUtils.params();
        queryMap.forEach((k, v) -> promExpTemplate.baseParams.p(k, v));
        return PromUtils.makeExpr(promExpTemplate, targets);
    }

    /**
     * 获取指标
     * <p>
     * /**
     * 获取指标趋势数据
     *
     * @param address
     * @param queryParam
     * @return
     */
    public static List<MetricDataDto> readMetricData(String address, Map<String, Object> queryParam) {
        PromResponse promResponse = PromUtils.readPromData(address + "/api/v1/query_range", queryParam);
        long start = NumberUtils.toLong(String.valueOf(queryParam.get(START)));
        long end = NumberUtils.toLong(String.valueOf(queryParam.get(END)));
        long step = NumberUtils.toLong(String.valueOf(queryParam.get(STEP)));
        List<MetricDataDto> metricDataList = initMetricData(start, end, step);
        if (promResponse == null || promResponse.getData() == null || CollectionUtils.isEmpty(promResponse.getData().getResult())) {
            logger.warn("查询数据为空");
            return metricDataList;
        }
        for (PromResponse.Data.Result result : promResponse.getData().getResult()) {
            List<List<String>> values = result.getValues();
            int t = 1;
            for (int i = metricDataList.size() - 1; i >= 0; i--) {
                int index = values.size() - t;
                if (index < 0) {
                    break;
                }
                String mVal = values.get(index).get(1);
                metricDataList.get(i).setMetricValue("NaN".equals(mVal) ? "0" : mVal);
                t++;
            }
        }
        return metricDataList;
    }

    /**
     * 初始指标趋势数据
     *
     * @return
     */
    public static List<MetricDataDto> initMetricData(long start, long end, long step) {
        List<MetricDataDto> metricDataDtoList = new ArrayList<>();
        long timeInterval = end - start;
        SimpleDateFormat simpleDateFormat;
        if (timeInterval > Const.SEC_OF_DAY) {
            simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm");
        } else {
            simpleDateFormat = new SimpleDateFormat("HH:mm");
        }
        long frequency = timeInterval / step + 1;
        for (int i = 0; i < frequency; i++) {
            long calKey = end - i * step;
            metricDataDtoList.add(new MetricDataDto(simpleDateFormat.format(new Date(calKey * MILL_TO_SEC)), "0.0"));
        }
        return Lists.reverse(metricDataDtoList);
    }

    /**
     * 获取排行数据
     *
     * @param address
     * @param queryParams
     * @return
     */
    public static List<MetricRankDto> readRankData(String address, Map<String, Object> queryParams) {
        PromResponse promResponse = PromUtils.readPromData(address + "/api/v1/query", queryParams);
        if (promResponse == null || promResponse.getData() == null || CollectionUtils.isEmpty(promResponse.getData().getResult())) {
            logger.warn("查询数据为空");
            return Collections.emptyList();
        }
        List<MetricRankDto> rankList = new ArrayList<>();
        for (PromResponse.Data.Result result : promResponse.getData().getResult()) {
            rankList.add(MetricRankDto.builder()
                    .name(result.getMetric().values().stream().collect(Collectors.joining(BaseConst.SYMBOL_HYPHEN)))
                    .count((int) NumberUtils.toDouble(result.getValue().get(1)))
                    .build());
        }
        return rankList;
    }


    /**
     * 获取计数数据
     *
     * @param address
     * @param queryParams
     * @return
     */
    public static long readCountData(String address, Map<String, Object> queryParams) {
        PromResponse promResponse = PromUtils.readPromData(address + "/api/v1/query", queryParams);
        if (promResponse == null || promResponse.getData() == null || CollectionUtils.isEmpty(promResponse.getData().getResult())) {
            logger.warn("查询数据为空，{}", JSON.toJSONString(promResponse));
            return NumberUtils.LONG_ZERO;
        }
        List<PromResponse.Data.Result> result = promResponse.getData().getResult();
        List<String> values = result.get(0).getValue();
        if (CollectionUtils.isEmpty(values)) {
            logger.warn("查询数据为空，{}", JSON.toJSONString(promResponse));
            return NumberUtils.LONG_ZERO;
        }
        Double d = NumberUtils.toDouble(values.get(1));
        return d.longValue();
    }

}


