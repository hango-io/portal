package org.hango.cloud.common.advanced.audit.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.exception.CouldNotConnectException;
import io.searchbox.core.Cat;
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
import org.hango.cloud.common.advanced.audit.config.ElasticSearchConfig;
import org.hango.cloud.common.advanced.audit.dto.AuditDto;
import org.hango.cloud.common.advanced.audit.dto.AuditQueryDto;
import org.hango.cloud.common.advanced.audit.meta.AuditPageResult;
import org.hango.cloud.common.advanced.audit.service.IAuditService;
import org.hango.cloud.common.advanced.base.config.CommonAdvanceConfig;
import org.hango.cloud.common.advanced.base.meta.AdvanceErrorCode;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.PageResult;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
 * @date 2022/11/16
 */
@Service
public class AuditServiceImpl implements IAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    @Autowired
    private CommonAdvanceConfig commonAdvanceConfig;

    @Autowired
    private ElasticSearchConfig elasticSearchConfig;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IRouteService routeService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    public static final String RESP_CODE = "respCode";

    public static final String  DURATION = "duration";

    @Override
    public ErrorCode checkQueryParam(AuditQueryDto auditQuery) {
        if (NumberUtils.LONG_ZERO == (auditQuery.getEndTime() & auditQuery.getStartTime())) {
            return CommonErrorCode.MISSING_PARAMETER_QUERY_TIME;
        }
        if (StringUtils.isNotBlank(auditQuery.getRespCode()) &&
                !StringUtils.equalsAny(auditQuery.getRespCode(), AdvancedConst.AUDIT_RESP_CODE_4XX,
                        AdvancedConst.AUDIT_RESP_CODE_5XX, AdvancedConst.AUDIT_RESP_CODE_ALL)
                && !NumberUtils.isDigits(auditQuery.getRespCode())) {
            return AdvanceErrorCode.invalidParameterRespCode(auditQuery.getRespCode());
        }
        int min = NumberUtils.min(auditQuery.getDuration(), auditQuery.getMinDuration(), auditQuery.getMaxDuration());
        if (min < NumberUtils.INTEGER_ZERO) {
            return CommonErrorCode.invalidDuration(String.valueOf(min));
        }
        if (auditQuery.getMaxDuration() < auditQuery.getMinDuration()) {
            return CommonErrorCode.QUERY_TIME_ILLEGAL;
        }
        if (auditQuery.getEndTime() - auditQuery.getStartTime() > AdvancedConst.QUERY_MAX_DAY * Const.MS_OF_DAY) {
            return CommonErrorCode.timeRangeTooLarge(String.valueOf(AdvancedConst.QUERY_MAX_DAY));
        }
        if (auditQuery.getStartTime() > auditQuery.getEndTime()) {
            return CommonErrorCode.QUERY_TIME_ILLEGAL;
        }
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(auditQuery.getVirtualGwId());
        if (virtualGatewayDto == null) {
            return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public PageResult<List<AuditDto>> getAuditInfoList(AuditQueryDto auditQuery) {
        if (auditQuery == null) {
            return new PageResult<>();
        }
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(auditQuery.getVirtualGwId());
        if (virtualGateway == null) {
            return new PageResult<>();
        }
        try {
            JestClient jestClient = elasticSearchConfig.getElasticsearchTemplateByGwId(virtualGateway.getGwId());
            Set<String> indices = getSearchIndices(auditQuery.getStartTime(), auditQuery.getEndTime(), virtualGateway.getGwClusterName(), jestClient);
            if (CollectionUtils.isEmpty(indices)) {
                return new PageResult<>();
            }
            JestResult jestResult = null;
            if (StringUtils.isBlank(auditQuery.getScrollId())) {
                String query = new SearchSourceBuilder().query(buildQueryBuilder(auditQuery ,virtualGateway)).size(auditQuery.getLimit()).toString();
                logger.info("ElasticSearch QL : {}", query);
                Search search = new Search.Builder(query).addSort(new Sort("time", Sort.Sorting.DESC)).setParameter(Parameters.SCROLL,commonAdvanceConfig.getSearchScroll()).addIndices(indices).build();
                logger.info("SearchUri = {}", search.toString());
                jestResult = jestClient.execute(search);
            } else {
                SearchScroll searchScroll = new SearchScroll.Builder(auditQuery.getScrollId(), commonAdvanceConfig.getSearchScroll()).build();
                logger.info("SearchUri = {}", searchScroll.toString());
                jestResult = jestClient.execute(searchScroll);
            }
            if (jestResult == null) {
                logger.info("查询结果为空");
                return new PageResult<>();
            }
            if (!jestResult.isSucceeded()) {
                logger.info("查询错误结果为 : {}", jestResult.getJsonString());
                String error = jestResult.getJsonObject().getAsJsonObject("error").get("type").getAsString();
                if (AdvancedConst.AUDIT_SCROLL_TIMEOUT.equals(error)) {
                    throw ErrorCodeException.of(AdvanceErrorCode.SCROLL_TIME_OUT);
                }
                logger.info("查询失败，ErrorMessage = {}", jestResult.getErrorMessage());
                return new PageResult<>();
            }
            logger.info("查询结果为 : {}", jestResult.getJsonString());
            JSONObject jsonObject = JSON.parseObject(jestResult.getJsonString());
            if (jsonObject.getBooleanValue("timed_out")) {
                logger.info("审计数据查询超时");
                throw ErrorCodeException.of(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }
            AuditPageResult<List<AuditDto>> auditPageResult = new AuditPageResult<>();
            auditPageResult.setScrollId(String.valueOf(jestResult.getValue("_scroll_id")));
            return parseAuditData(jsonObject,auditPageResult);
        } catch (IOException e) {
            logger.error("查询审计数据异常，{}", e.getMessage());
            e.printStackTrace();
            throw ErrorCodeException.of(e instanceof CouldNotConnectException ? CommonErrorCode.INTERNAL_SERVER_ERROR : CommonErrorCode.READ_TIME_OUT);
        }
    }

    @Override
    public String getAuditDetail(String id, String auditIndex, long virtualGwId) {
        if (StringUtils.isBlank(id) || virtualGwId == NumberUtils.INTEGER_ZERO) {
            return StringUtils.EMPTY;
        }
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGateway == null) {
            return StringUtils.EMPTY;
        }

        try {
            JestClient jestClient = elasticSearchConfig.getElasticsearchTemplateByGwId(virtualGateway.getGwId());
            Get build = new Get.Builder(auditIndex, id).build();
            logger.info("ElasticSearch QL : {}", build.toString());
            DocumentResult documentResult = jestClient.execute(build);
            if (documentResult != null) {
                return documentResult.getSourceAsString();
            }
        } catch (IOException e) {
            logger.error("查询审计数据异常，{}", e.getMessage());
            e.printStackTrace();
            throw ErrorCodeException.of(e instanceof CouldNotConnectException ? CommonErrorCode.INTERNAL_SERVER_ERROR : CommonErrorCode.READ_TIME_OUT);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取索引检索条件
     *
     * @param fromTime
     * @param toTime
     * @param gatewayClusterName
     * @param jestClient
     * @return
     */
    private Set<String> getSearchIndices(long fromTime, long toTime, String gatewayClusterName, JestClient jestClient) {
        Set<String> indices = Sets.newHashSet();
        Cat build = new Cat.IndicesBuilder().build();
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
                indices.add(jsonObject.getString("index"));
            }
        } catch (Exception e) {
            logger.warn("ElasticSearch查询出错，ErrorMessage = {}", e.getMessage());
            e.printStackTrace();
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return makeUpIndices(fromTime, toTime, gatewayClusterName, indices);
    }

    /**
     * index 查询补偿机制
     * 由于审计数据采集存在延迟，因此索引范围向后+2
     * @param fromTime
     * @param toTime
     * @param gatewayClusterName
     * @param indices
     * @return
     */
    private Set<String> makeUpIndices(long fromTime, long toTime, String gatewayClusterName, Set<String> indices) {
        Set<String> realIndices = Sets.newHashSet();

        long fromTimeForHours = fromTime / Const.MS_OF_HOUR;
        //由于审计数据采集存在延迟，因此索引范围向后+2
        long toTimeForHours = toTime / Const.MS_OF_HOUR + 2;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(commonAdvanceConfig.getIndexPattern());
        Map<String, Set<String>> countMap = Maps.newHashMap();
        for (int i = 0; i < toTimeForHours - fromTimeForHours; i++) {
            String index = String.format(commonAdvanceConfig.getIndexFormat(), gatewayClusterName, simpleDateFormat.format(fromTime + i * Const.MS_OF_HOUR));
            if (!indices.contains(index)) {
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
            if (subIndices.size() == BaseConst.HOUR_OF_DAY) {
                realIndices.add(next.getKey());
            } else {
                realIndices.addAll(subIndices);
            }
        }
        logger.info("Search Index is " + JSON.toJSONString(realIndices));
        return realIndices;
    }

    @SuppressWarnings({"java:S3776"})
    private QueryBuilder buildQueryBuilder(AuditQueryDto auditQuery, VirtualGatewayDto virtualGateway) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("time").gte(auditQuery.getStartTime()).lte(auditQuery.getEndTime()));
        if (auditQuery.getVirtualGwId()!= NumberUtils.LONG_ZERO){
            queryBuilder.must(QueryBuilders.boolQuery()
                    .should(QueryBuilders.termQuery("virtualGatewayCode", virtualGateway.getCode()))
                    .should(QueryBuilders.termQuery("virtualGatewayPort", virtualGateway.getPort()))
            );
        }
        if (auditQuery.getMinDuration() != NumberUtils.INTEGER_ZERO) {
            queryBuilder.must(QueryBuilders.rangeQuery(DURATION).gt(auditQuery.getMinDuration()));
        }
        if (auditQuery.getMaxDuration() != NumberUtils.INTEGER_ZERO) {
            queryBuilder.must(QueryBuilders.rangeQuery(DURATION).lt(auditQuery.getMaxDuration()));
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
        if (AdvancedConst.AUDIT_RESP_CODE_4XX.equals(auditQuery.getRespCode())) {
            queryBuilder.must(QueryBuilders.rangeQuery(RESP_CODE).gte(400));
            queryBuilder.must(QueryBuilders.rangeQuery(RESP_CODE).lt(500));
        } else if (AdvancedConst.AUDIT_RESP_CODE_5XX.equals(auditQuery.getRespCode())) {
            queryBuilder.must(QueryBuilders.rangeQuery(RESP_CODE).gte(500));
            queryBuilder.must(QueryBuilders.rangeQuery(RESP_CODE).lt(600));
        } else if (NumberUtils.isDigits(auditQuery.getRespCode())) {
            queryBuilder.must(QueryBuilders.termQuery(RESP_CODE, auditQuery.getRespCode()));
        }
        if (auditQuery.getApiId() != NumberUtils.LONG_ZERO) {
            RouteDto routeDto = routeService.get(auditQuery.getApiId());
            queryBuilder.must(QueryBuilders.termQuery("apiId", routeDto.getName()));
        }
        if (!NumberUtils.LONG_ZERO.equals(auditQuery.getServiceId())) {
            ServiceProxyDto serviceProxyDto = serviceProxyService.get(auditQuery.getServiceId());
            queryBuilder.must(QueryBuilders.termQuery("serviceName",serviceProxyDto.getName()));
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
        return queryBuilder;
    }

    private PageResult<List<AuditDto>> parseAuditData(JSONObject jsonObject,AuditPageResult<List<AuditDto>> auditPageResult) {

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
        if (total == NumberUtils.INTEGER_ZERO) {
            logger.info("查询数据量为0");
            return auditPageResult;
        }
        JSONArray hitList = hits.getJSONArray("hits");
        List<AuditDto> auditList = hitList.stream().map(this::formatEsData).collect(Collectors.toList());
        auditPageResult.setResult(auditList);
        auditPageResult.setTotal(total);
        return auditPageResult;
    }

    /**
     * 组装ES返回的数据
     *
     * @param hit
     * @return
     */
    private AuditDto formatEsData(Object hit) {
        AuditDto auditInfo = new AuditDto();
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
        auditInfo.setRespCode(NumberUtils.toInt(source.getString(RESP_CODE)));
        auditInfo.setTenantId(source.getString("tenantId"));
        auditInfo.setUserIp(source.getString("userIp"));
        auditInfo.setDuration(NumberUtils.toLong(source.getString(DURATION)));
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
        auditInfo.setApiName(source.getString("apiName"));
        return auditInfo;
    }
}
