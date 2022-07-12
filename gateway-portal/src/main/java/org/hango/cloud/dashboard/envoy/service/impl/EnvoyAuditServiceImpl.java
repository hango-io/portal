package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.exception.CouldNotConnectException;
import io.searchbox.core.Cat;
import io.searchbox.core.Count;
import io.searchbox.core.CountResult;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;
import io.searchbox.core.Search;
import io.searchbox.core.SearchScroll;
import io.searchbox.core.search.sort.Sort;
import io.searchbox.params.Parameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.AuditInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.AuditQuery;
import org.hango.cloud.dashboard.apiserver.meta.audit.CallStatisticsInfo;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.service.impl.AuditServiceImpl;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.PromUtils;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.RequestContextHolder;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/15
 */
@Service
@Qualifier("envoyAuditServiceImpl")
public class EnvoyAuditServiceImpl extends AuditServiceImpl implements IEnvoyAuditService {

    public static final Logger logger = LoggerFactory.getLogger(EnvoyAuditServiceImpl.class);
    public static final Map<String, String> promTemplateMap = new HashMap<>();
    private static final String AUDIT_INDICES = "envoy_gateway_audit_%s-%s";
    private static final String AUDIT_TYPE = "doc";

    static {
        promTemplateMap.put(Const.TOTAL_COUNT, "sum(increase(envoy_cluster_upstream_rq_total{<filter>}[<time_interval>s]))");
        promTemplateMap.put(Const.SUCCESS_COUNT, "sum(increase(envoy_cluster_upstream_rq_xx{envoy_response_code_class!~\"4|5\",<filter>}[<time_interval>s]))");
        promTemplateMap.put(Const.BAD_REQUEST, "sum(increase(envoy_cluster_upstream_rq_xx{envoy_response_code_class=~\"4\",<filter>}[<time_interval>s]))");
        promTemplateMap.put(Const.ERROR_REQUEST, "sum(increase(envoy_cluster_upstream_rq_xx{envoy_response_code_class=~\"5\",<filter>}[<time_interval>s]))");
        promTemplateMap.put(Const.DURATION_95, "histogram_quantile(0.95,sum by(<sumBy>,le)(increase(envoy_cluster_upstream_rq_time_bucket{<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.DURATION_99, "histogram_quantile(0.99,sum by(<sumBy>,le)(increase(envoy_cluster_upstream_rq_time_bucket{<filter>}[<time_interval>s])))");
        promTemplateMap.put(Const.DURATION_AVG, "sum by(<sumBy>)(increase(envoy_cluster_upstream_rq_time_sum{<filter>}[<time_interval>s]))/sum by(<sumBy>)(increase(envoy_cluster_upstream_rq_time_count{<filter>}[<time_interval>s]))");
    }

    private JestClient jestClient;
    @Autowired
    private IRouteRuleInfoService envoyRouteRuleInfoService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Value("${elasticsearch.index.pattern:yyyy.MM.dd}")
    private String pattern;
    @Value("${elasticsearch.search.scroll:5m}")
    private String scroll;

    private static QueryBuilder buildQueryBuilder(AuditQuery auditQuery, String clusterName) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        if (auditQuery.getMinDuration() != NumberUtils.INTEGER_ZERO) {
            queryBuilder.must(QueryBuilders.rangeQuery("duration").gt(auditQuery.getMinDuration()));
        }
        if (auditQuery.getMaxDuration() != NumberUtils.INTEGER_ZERO) {
            queryBuilder.must(QueryBuilders.rangeQuery("duration").lt(auditQuery.getMaxDuration()));
        }
        if (StringUtils.isNotBlank(auditQuery.getRequestId())) {
            queryBuilder.must(QueryBuilders.regexpQuery("requestId", auditQuery.getRequestId() + ".*"));
        }
        if (StringUtils.isNotBlank(auditQuery.getUpstreamHost())) {
            queryBuilder.must(QueryBuilders.regexpQuery("upstreamHost", auditQuery.getUpstreamHost() + ".*"));
        }
        if (StringUtils.isNotBlank(auditQuery.getUserIp())) {
            queryBuilder.must(QueryBuilders.regexpQuery("userIp", auditQuery.getUserIp() + ".*"));
        }
        if (StringUtils.isNotBlank(auditQuery.getAccount())) {
            queryBuilder.must(QueryBuilders.fuzzyQuery("account", auditQuery.getAccount()));
        }
        if (StringUtils.isNotBlank(auditQuery.getResponseFlag())) {
            queryBuilder.must(QueryBuilders.regexpQuery("responseFlags", ".*" + auditQuery.getResponseFlag() + ".*"));
        }
        if (StringUtils.isNotBlank(auditQuery.getUri())) {
            queryBuilder.must(QueryBuilders.regexpQuery("uri", auditQuery.getUri() + ".*"));
        }
        if (Const.AUDIT_RESP_CODE_4XX.equals(auditQuery.getRespCode())) {
            queryBuilder.must(QueryBuilders.rangeQuery("respCode").gte(400));
            queryBuilder.must(QueryBuilders.rangeQuery("respCode").lt(500));
        } else if (Const.AUDIT_RESP_CODE_5XX.equals(auditQuery.getRespCode())) {
            queryBuilder.must(QueryBuilders.rangeQuery("respCode").gte(500));
            queryBuilder.must(QueryBuilders.rangeQuery("respCode").lt(600));
        } else if (NumberUtils.isDigits(auditQuery.getRespCode())) {
            queryBuilder.must(QueryBuilders.termQuery("respCode", auditQuery.getRespCode()));
        }

        if (auditQuery.getApiId() != NumberUtils.LONG_ZERO) {
            queryBuilder.must(QueryBuilders.termQuery("apiId", auditQuery.getApiId()));
        }
        if (StringUtils.isNotBlank(auditQuery.getServiceTag())) {
            queryBuilder.must(QueryBuilders.termQuery("serviceName", auditQuery.getServiceTag()));
        } else if (auditQuery.getProjectDivided()) {
            queryBuilder.must(QueryBuilders.termQuery("projectId", ProjectTraceHolder.getProId()));
        }

        if (StringUtils.isNotBlank(auditQuery.getOriginPath())) {
            queryBuilder.must(QueryBuilders.regexpQuery("originPath", auditQuery.getOriginPath() + ".*"));
        }

        if (StringUtils.isNotBlank(auditQuery.getOriginHost())) {
            queryBuilder.must(QueryBuilders.regexpQuery("originHost", auditQuery.getOriginHost() + ".*"));
        }
        if (StringUtils.isNotBlank(auditQuery.getPodName())) {
            queryBuilder.must(QueryBuilders.regexpQuery("pod_name", auditQuery.getPodName() + ".*"));
        }

        if (StringUtils.isNotBlank(auditQuery.getHostName())) {
            queryBuilder.must(QueryBuilders.regexpQuery("hostname", auditQuery.getHostName() + ".*"));
        }
        queryBuilder.must(QueryBuilders.rangeQuery("time").gte(auditQuery.getStartTime()).lte(auditQuery.getEndTime()));
        return queryBuilder;
    }

    @Override
    public long countAuditInfo(AuditQuery auditQuery, GatewayInfo gatewayInfo) {
        if (auditQuery == null || gatewayInfo == null) {
            return NumberUtils.LONG_ZERO;
        }
        jestClient = elasticSearchConfig.getElasticsearchTemplateByGwId(String.valueOf(gatewayInfo.getId()));
        Count count = new Count.Builder().addIndex(String.format(AUDIT_INDICES, gatewayInfo.getGwClusterName()))
                //.addType(AUDIT_TYPE)
                .query(
                        new SearchSourceBuilder().query(buildQueryBuilder(auditQuery, gatewayInfo.getGwClusterName()))
                                .toString()).build();
        try {
            CountResult countResult = jestClient.execute(count);
            if (countResult != null) {
                return countResult.getCount().longValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return NumberUtils.LONG_ZERO;
    }

    @Override
    public Page<AuditInfo> getAuditInfoList(AuditQuery auditQuery, GatewayInfo gatewayInfo) {
        if (auditQuery == null || gatewayInfo == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        HttpServletRequest request = RequestContextHolder.getRequest();
        if (request == null) {
            logger.error("查询审计数据异常，request 不存在");
            return new PageImpl<>(Collections.emptyList());
        }
        jestClient = elasticSearchConfig.getElasticsearchTemplateByGwId(String.valueOf(gatewayInfo.getId()));
        try {
            JestResult jestResult = null;
            if (StringUtils.isBlank(auditQuery.getScrollId())) {
                String query = new SearchSourceBuilder().query(buildQueryBuilder(auditQuery, gatewayInfo.getGwClusterName()))
                        .size(auditQuery.getLimit())
                        .toString();
                logger.info("ElasticSearch QL : {}", query);
                Search.Builder builder = new Search.Builder(query)
                        // .addType(AUDIT_TYPE)
                        .addSort(new Sort("time", Sort.Sorting.DESC))
                        .setParameter(Parameters.SCROLL, scroll);
                Set<String> indices = getSearchIndices(auditQuery.getStartTime(), auditQuery.getEndTime(), gatewayInfo.getGwClusterName(), jestClient);
                if (CollectionUtils.isEmpty(indices)) {
                    return new PageImpl<>(Collections.emptyList());
                }
                for (String index : indices) {
                    builder.addIndex(index);
                }
                Search search = builder.build();
                logger.info("SearchUri = {}", search.toString());
                jestResult = jestClient.execute(search);

            } else {
                SearchScroll searchScroll = new SearchScroll.Builder(auditQuery.getScrollId(), scroll).build();
                logger.info("SearchUri = {}", searchScroll.toString());
                jestResult = jestClient.execute(searchScroll);
            }
            if (jestResult == null) {
                logger.info("查询结果为空");
                return new PageImpl<>(Collections.emptyList());
            }
            if (!jestResult.isSucceeded()) {
                String error = jestResult.getJsonObject().getAsJsonObject("error").get("type").getAsString();
                if (Const.AUDIT_SCROLL_TIMEOUT.equals(error)) {
                    request.setAttribute(Const.ATTRIBUTE_FOR_AUDIT, Const.AUDIT_SCROLL_TIMEOUT);
                }
                logger.info("查询失败，ErrorMessage = {}", jestResult.getErrorMessage());
                return new PageImpl<>(Collections.emptyList());
            }
            logger.info("查询结果为 : {}", jestResult.getJsonString());
            JSONObject jsonObject = JSON.parseObject(jestResult.getJsonString());
            if (jsonObject.getBooleanValue("timed_out")) {
                request.setAttribute(Const.ATTRIBUTE_FOR_AUDIT, Const.AUDIT_HOST_UNREACHABLE);
                logger.info("审计数据查询超时");
                return new PageImpl<>(Collections.emptyList());
            }
            RequestContextHolder.getRequest().setAttribute(Const.AUDIT_SCROLL_ID, jestResult.getValue("_scroll_id"));
            return parseAuditData(jsonObject, auditQuery);
        } catch (IOException e) {
            logger.error("查询审计数据异常，{}", e.getMessage());
            e.printStackTrace();
            if (e instanceof CouldNotConnectException) {
                request.setAttribute(Const.ATTRIBUTE_FOR_AUDIT, Const.AUDIT_HOST_UNREACHABLE);
            } else {
                request.setAttribute(Const.ATTRIBUTE_FOR_AUDIT, Const.AUDIT_QUERY_TIMEOUT);
            }
        }
        return new PageImpl<>(Collections.emptyList());
    }

    @Override
    public String getAuditDetail(String id, String auditIndex, GatewayInfo gatewayInfo) {
        if (StringUtils.isBlank(id) || gatewayInfo == null) {
            return StringUtils.EMPTY;
        }
        jestClient = elasticSearchConfig.getElasticsearchTemplateByGwId(String.valueOf(gatewayInfo.getId()));
        Get build = new Get.Builder(auditIndex, id).build();
        logger.info("ElasticSearch QL : {}", build.toString());
        try {
            DocumentResult documentResult = jestClient.execute(build);
            if (documentResult != null) {
                return documentResult.getSourceAsString();
            }
        } catch (IOException e) {
            logger.error("查询审计数据异常，{}", e.getMessage());
            e.printStackTrace();
            HttpServletRequest request = RequestContextHolder.getRequest();
            if (e instanceof CouldNotConnectException) {
                request.setAttribute(Const.ATTRIBUTE_FOR_AUDIT, Const.AUDIT_HOST_UNREACHABLE);
            } else {
                request.setAttribute(Const.ATTRIBUTE_FOR_AUDIT, Const.AUDIT_QUERY_TIMEOUT);
            }
        }
        return StringUtils.EMPTY;
    }

    private Page<AuditInfo> parseAuditData(JSONObject jsonObject, AuditQuery auditQuery) {

        JSONObject hits = jsonObject.getJSONObject("hits");
        //对于6.X ElasticSearch，查询总数数据格式为 {"total":xxx}
        //对于7.X ElasticSearch，查询总数数据格式为 {"total":{"value":8,"relation":"eq"}
        //因此此处需进行判断
        String totalForStr = hits.getString("total");
        long total;
        if (NumberUtils.isDigits(totalForStr)) {
            total = NumberUtils.toLong(totalForStr);
        } else {
            JSONObject totalJson = JSON.parseObject(totalForStr);
            total = totalJson == null ? NumberUtils.LONG_ZERO : NumberUtils.toLong(totalJson.getString("value"));
        }

        if (total == 0) {
            logger.info("查询数据量为0");
            return new PageImpl<>(Collections.emptyList());
        }
        JSONArray hitList = hits.getJSONArray("hits");
        HashMap<Long, String> apiMap = Maps.newHashMap();
        List<AuditInfo> collect = hitList.stream().map(hit -> {
            AuditInfo auditInfo = new AuditInfo();
            JSONObject hitJson = JSON.parseObject(String.valueOf(hit));
            auditInfo.setAuditIndex(hitJson.getString("_index"));
            auditInfo.setId(hitJson.getString("_id"));
            JSONObject source = hitJson.getJSONObject("_source");
            auditInfo.setUri(source.getString("uri"));
            auditInfo.setMethod(source.getString("method"));
            auditInfo.setReqHeaders(source.getString("reqHeaders"));
            auditInfo.setReqBody(source.getString("reqBody"));
            auditInfo.setQueryString(source.getString("queryString"));
            auditInfo.setRespBody(source.getString("respBody"));
            auditInfo.setRespHeaders(source.getString("respHeaders"));
            auditInfo.setRespCode(NumberUtils.toInt(source.getString("respCode")));
            auditInfo.setTenantId(source.getString("tenantId"));
            auditInfo.setUserIp(source.getString("userIp"));
            auditInfo.setDuration(NumberUtils.toLong(source.getString("duration")));
            auditInfo.setRequestId(source.getString("requestId"));
            auditInfo.setTime(NumberUtils.toLong(source.getString("time")));
            auditInfo.setUpstreamHost(source.getString("upstreamHost"));
            auditInfo.setUpstreamServiceTime(source.getString("upstreamServiceTime"));
            auditInfo.setUserAgent(source.getString("userAgent"));
            auditInfo.setRemoteUser(source.getString("remoteUser"));
            auditInfo.setBodyBytesSent(source.getString("bodyBytesSent"));
            auditInfo.setHttpReferer(source.getString("httpReferer"));
            auditInfo.setUpstreamStatus(source.getString("upstreamStatus"));
            auditInfo.setResponseFlag(source.getString("responseFlags"));
            auditInfo.setServiceName(source.getString("serviceName"));


            auditInfo.setOriginPath(source.getString("originPath"));
            auditInfo.setOriginHost(source.getString("originHost"));
            auditInfo.setHostName(source.getString("hostname"));
            auditInfo.setPodName(source.getString("pod_name"));


            long apiId = NumberUtils.toLong(source.getString("apiId"));
            auditInfo.setApiId(apiId);
            String apiName = apiMap.get(apiId);
            if (StringUtils.isBlank(apiName) && NumberUtils.LONG_ZERO != apiId) {
                RouteRuleInfo routeRuleInfoById = envoyRouteRuleInfoService.getRouteRuleInfoById(auditInfo.getApiId());
                apiName = routeRuleInfoById == null ? source.getString("apiName") : routeRuleInfoById.getRouteRuleName();
                apiMap.put(apiId, apiName);
            }
            auditInfo.setApiName(apiName);

            return auditInfo;
        }).collect(Collectors.toList());
        return new PageImpl<>(collect, auditQuery.getPageable(), total);
    }

    private Set<String> getSearchIndices(long fromTime, long ToTime, String gatewayClusterName, JestClient jestClient) {
        Set<String> indices = Sets.newHashSet();

        Cat build = new Cat.IndicesBuilder().build();
        Set<String> realIndices = Sets.newHashSet();
        try {
            JestResult jestResult = jestClient.execute(build);
            if (jestResult == null) {
                logger.info("查询结果为空");
                return indices;
            }
            if (!jestResult.isSucceeded()) {
                logger.info("查询出错，ErrorMessage = {}", jestResult.getErrorMessage());
                return indices;
            }
            List<JSONObject> result = JSON.parseArray(jestResult.getSourceAsString(), JSONObject.class);
            if (CollectionUtils.isEmpty(result)) {
                logger.info("未找到index");
                return indices;
            }
            for (JSONObject jsonObject : result) {
                realIndices.add(jsonObject.getString("index"));
            }
        } catch (IOException e) {
            logger.warn("ElasticSearch查询出错，ErrorMessage = {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.warn("ElasticSearch查询出错，ErrorMessage = {}", e.getMessage());
            e.printStackTrace();
        }

        long fromTimeForHours = fromTime / Const.MS_OF_HOUR;
        //由于审计数据采集存在延迟，因此索引范围向后+2
        long toTimeForHours = ToTime / Const.MS_OF_HOUR + 2;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Map<String, Set<String>> countMap = Maps.newHashMap();
        for (int i = 0; i < toTimeForHours - fromTimeForHours; i++) {
            String index = String.format(AUDIT_INDICES, gatewayClusterName, simpleDateFormat.format(fromTime + i * Const.MS_OF_HOUR));
            if (!realIndices.contains(index)) {
                continue;
            }
            String key = index.substring(0, index.lastIndexOf(".")) + "*";
            Set<String> subIndices = countMap.containsKey(key) ? countMap.get(key) : Sets.newHashSet();
            subIndices.add(index);
            countMap.put(key, subIndices);
        }
        Iterator<Map.Entry<String, Set<String>>> iterator = countMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<String>> next = iterator.next();
            Set<String> subIndices = next.getValue();
            if (subIndices.size() == Const.HOUR_OF_DAY) {
                indices.add(next.getKey());
            } else {
                indices.addAll(subIndices);
            }
        }
        logger.info("Search Index is " + JSON.toJSONString(indices));
        return indices;
    }

    @Override
    public CallStatisticsInfo getAuditStatisticsInfo(AuditQuery auditQuery, GatewayInfo gatewayInfo) {
        CallStatisticsInfo callStatisticsInfo = new CallStatisticsInfo();
        if (gatewayInfo == null) {
            return callStatisticsInfo;
        }
        String queryAddr = gatewayInfo.getPromAddr() + "/api/v1/query";
        PromUtils.PromExpTemplate promExpTemplate = new PromUtils.PromExpTemplate();
        promExpTemplate.baseParams = PromUtils.params()
                .p("time_interval", (auditQuery.getEndTime() - auditQuery.getStartTime()) / 1000)
                .p("sumBy", "cluster_name");
        HashMap<String, String> target = Maps.newHashMap();
        target.put("cluster_name", gatewayInfo.getGwClusterName());
        if (StringUtils.isNotBlank(auditQuery.getServiceTag())) {
            promExpTemplate.baseParams.p("sumBy", "envoy_cluster_name");
            target.put("envoy_cluster_name", auditQuery.getServiceTag());
        } else {
            List<ServiceProxyInfo> serviceProxyInfoList = serviceProxyService.getEnvoyServiceProxy(gatewayInfo.getId(), NumberUtils.LONG_ZERO, ProjectTraceHolder.getProId(), 0, 1000);
            if (CollectionUtils.isEmpty(serviceProxyInfoList)) {
                return callStatisticsInfo;
            }
            target.put("envoy_cluster_name", StringUtils.join(serviceProxyInfoList.stream().map(
                            e -> serviceInfoService.getServiceByServiceId(e.getServiceId()).getServiceName())
                    .collect(Collectors.toList()), "|"));
        }
        List<Map<String, String>> targets = Lists.newArrayList(target);

        Map<String, Object> queryParams = Maps.newHashMap();
        queryParams.put("time", String.valueOf(auditQuery.getEndTime() / 1000));

        //success count 2xx
        promExpTemplate.template = promTemplateMap.get(Const.SUCCESS_COUNT);
        queryParams.put("query", PromUtils.makeExpr(promExpTemplate, targets));
        long successCount = PromUtils.readCountData(queryAddr, queryParams);

        //4xx
        promExpTemplate.template = promTemplateMap.get(Const.BAD_REQUEST);
        queryParams.put("query", PromUtils.makeExpr(promExpTemplate, targets));
        long _4xxCount = PromUtils.readCountData(queryAddr, queryParams);

        //fail count 5xx
        promExpTemplate.template = promTemplateMap.get(Const.ERROR_REQUEST);
        queryParams.put("query", PromUtils.makeExpr(promExpTemplate, targets));
        long failCount = PromUtils.readCountData(queryAddr, queryParams);

        //95 RT
        promExpTemplate.template = promTemplateMap.get(Const.DURATION_95);
        queryParams.put("query", PromUtils.makeExpr(promExpTemplate, targets));
        long _95Duration = PromUtils.readCountData(queryAddr, queryParams);

        //99 RT
        promExpTemplate.template = promTemplateMap.get(Const.DURATION_99);
        queryParams.put("query", PromUtils.makeExpr(promExpTemplate, targets));
        long _99Duration = PromUtils.readCountData(queryAddr, queryParams);


        //avg RT
        promExpTemplate.template = promTemplateMap.get(Const.DURATION_AVG);
        queryParams.put("query", PromUtils.makeExpr(promExpTemplate, targets));
        long avgCount = PromUtils.readCountData(queryAddr, queryParams);

        //total count
        /*promExpTemplate.template = promTemplateMap.get(Const.TOTAL_COUNT);
        queryParams.put("query", PromUtils.makeExpr(promExpTemplate, targets));
        long totalCount = PromUtils.readCountData(queryAddr, queryParams);*/
        long totalCount = successCount + _4xxCount + failCount;


        callStatisticsInfo.setTotalCount(totalCount);
        callStatisticsInfo.setSuccessCount(successCount);
        callStatisticsInfo.setFailedCount(failCount);
        callStatisticsInfo.setBadRequestCount(_4xxCount);
        callStatisticsInfo.setDuration95(_95Duration);
        callStatisticsInfo.setDuration99(_99Duration);
        callStatisticsInfo.setAverageDuration(((int) avgCount));

        return callStatisticsInfo;
    }
}
