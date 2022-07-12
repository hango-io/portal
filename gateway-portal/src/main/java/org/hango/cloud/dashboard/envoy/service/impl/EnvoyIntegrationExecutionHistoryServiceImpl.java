package org.hango.cloud.dashboard.envoy.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.sort.Sort;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.config.ElasticSearchConfig;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationExecutionHistoryService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyIntegrationExecutionHistoryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class EnvoyIntegrationExecutionHistoryServiceImpl implements IEnvoyIntegrationExecutionHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationExecutionHistoryServiceImpl.class);
    private volatile static JestClient jestClient;
    @Autowired
    private ElasticSearchConfig elasticSearchConfig;
    @Autowired
    private IEnvoyIntegrationService integrationService;
    @Autowired
    private ApiServerConfig apiServerConfig;
    @Value("${integration.elasticsearch.esPrefix:nsb_tracer_}")
    private String esPrefix;
    @Value("${integration.elasticsearch.esStatusPrefix:nsb_success_}")
    private String esStatusPrefix;
    @Value("${integration.elasticsearch.esExceptionPrefix:nsb_exception_}")
    private String esExceptionPrefix;
    @Value("${integration.elasticsearch.esAddr:http://localhost:9200}")
    private String esAddr;
    @Value("${integration.elasticsearch.username:@null}")
    private String username;
    @Value("${integration.elasticsearch.password:@null}")
    private String password;
    @Value("${integration.elasticsearch.days:15}")
    private int days;

    @Override
    public Map<String, Object> getExecutionHistoryByPage(int offset, int limit, long id) {
        JestClient jestClient = getJestClient();
        List<EnvoyIntegrationExecutionHistoryDto> historyDtos = new ArrayList<>();
        String countStr = "0";
        //构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (id != 0) {
            searchSourceBuilder.query(QueryBuilders.termQuery("integrationId", id));
        }
        searchSourceBuilder.from(offset).size(limit).collapse(new CollapseBuilder("recordId"));
        CardinalityAggregationBuilder aggregation = AggregationBuilders.cardinality("recordId").field("recordId");
        searchSourceBuilder.aggregation(aggregation);
        Sort sort = new Sort("timestamp", Sort.Sorting.DESC);
        Search.Builder builder = new Search.Builder(searchSourceBuilder.toString());
        //获取指定天数的索引
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int time = -1;
        calendar.add(Calendar.DATE, 0);
        builder.addIndex(esPrefix + sdf.format(calendar.getTime()) + "*");
        for (int i = 0; i < days - 1; i++) {
            calendar.add(Calendar.DATE, time);
            builder.addIndex(esPrefix + sdf.format(calendar.getTime()) + "*");
        }
        Search search = builder.addSort(sort).build();
        try {
            SearchResult searchResult = jestClient.execute(search);
            if (searchResult.isSucceeded() && !searchResult.getJsonObject().get("hits").isJsonNull() &&
                    !searchResult.getJsonObject().get("hits").getAsJsonObject().get("hits").isJsonNull()) {
                Iterator<JsonElement> iterator = searchResult.getJsonObject().get("hits").getAsJsonObject().get("hits").getAsJsonArray().iterator();
                JsonObject next;
                while (iterator.hasNext()) {
                    next = iterator.next().getAsJsonObject().get("_source").getAsJsonObject();
                    EnvoyIntegrationExecutionHistoryDto historyDto = new EnvoyIntegrationExecutionHistoryDto();
                    if (!next.get("recordId").isJsonNull()) {
                        historyDto.setExecutionId(next.get("recordId").getAsString());
                    }
                    if (!next.get("integrationId").isJsonNull()) {
                        historyDto.setIntegrationId(next.get("integrationId").getAsLong());
                    }
                    if (!next.get("timestamp").isJsonNull()) {
                        historyDto.setExecutionTime(Long.parseLong(next.get("timestamp").getAsString()));
                    }
                    historyDtos.add(historyDto);
                }
                //取总数
                if (searchResult.getJsonObject().has("aggregations") && !searchResult.getJsonObject().get("aggregations").isJsonNull()
                        && !searchResult.getJsonObject().get("aggregations").getAsJsonObject().get("recordId").getAsJsonObject().get("value").isJsonNull()) {
                    countStr = searchResult.getJsonObject().get("aggregations").getAsJsonObject().get("recordId").
                            getAsJsonObject().get("value").getAsString();
                }
            }
        } catch (IOException e) {
            logger.error("Exception:", e);
        }
        Map<String, Object> res = new HashMap<>();
        getExecutionStatus(historyDtos, limit);
        getIntegrationName(historyDtos);
        res.put("ExecutionHistoryList", historyDtos);
        long count = Long.parseLong(countStr);
        if (count > 10000) {
            count = 10000;
        }
        res.put("TotalCount", count);
        return res;
    }

    @Override
    public List<EnvoyIntegrationExecutionHistoryDto> getIntegrationName(List<EnvoyIntegrationExecutionHistoryDto> historyDtoList) {
        //组合出需要查询的id的列表
        List<Long> idList = new ArrayList<>();
        for (EnvoyIntegrationExecutionHistoryDto next : historyDtoList) {
            if (next.getIntegrationId() != 0) {
                idList.add(next.getIntegrationId());
            }
        }
        //将集成名称添加到原list中
        if (!idList.isEmpty()) {
            List<EnvoyIntegrationInfo> integrationInfoList = integrationService.getByIdlist(idList);
            Map<Long, String> nameMap = new HashMap<>(Const.DEFAULT_MAP_SIZE);
            for (EnvoyIntegrationInfo nextInfo : integrationInfoList) {
                nameMap.put(nextInfo.getId(), nextInfo.getIntegrationName());
            }
            for (EnvoyIntegrationExecutionHistoryDto next : historyDtoList) {
                if (next.getIntegrationId() != 0) {
                    next.setIntegrationName(nameMap.get(next.getIntegrationId()));
                }
            }
        }
        return historyDtoList;
    }

    @Override
    public List<Map<String, Object>> getIntegrationLog(String executionId, String type) {
        JestClient jestClient = getJestClient();
        //构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("recordId", executionId)).sort("timestamp", SortOrder.ASC).size(10000);
        Search.Builder builder = new Search.Builder(searchSourceBuilder.toString());
        //获取指定天数的索引
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int time = -1;
        calendar.add(Calendar.DATE, 0);
        builder.addIndex(type + sdf.format(calendar.getTime()) + "*");
        for (int i = 0; i < days - 1; i++) {
            calendar.add(Calendar.DATE, time);
            builder.addIndex(type + sdf.format(calendar.getTime()) + "*");
        }
        Search search = builder.build();
        List<Map<String, Object>> res = new ArrayList<>();
        try {
            SearchResult searchResult = jestClient.execute(search);
            if (searchResult.isSucceeded() && !searchResult.getJsonObject().get("hits").isJsonNull() &&
                    !searchResult.getJsonObject().get("hits").getAsJsonObject().get("hits").isJsonNull()) {
                JsonArray resArray = searchResult.getJsonObject().get("hits").getAsJsonObject().get("hits").getAsJsonArray();
                if (resArray.size() > 0) {
                    Iterator<JsonElement> iterator = resArray.iterator();
                    JsonObject next;
                    Gson gson = new Gson();
                    while (iterator.hasNext()) {
                        next = iterator.next().getAsJsonObject().get("_source").getAsJsonObject();
                        String jsonString = gson.toJson(next);
                        Map<String, Object> map = gson.fromJson(jsonString, Map.class);
                        res.add(map);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Exception:", e);
        }
        return res;
    }

    private JestClient getJestClient() {
        if (jestClient == null) {
            synchronized (EnvoyIntegrationExecutionHistoryServiceImpl.class) {
                if (jestClient == null) {
                    JestClientFactory factory = new JestClientFactory();
                    factory.setHttpClientConfig(createHttpClientConfig());
                    jestClient = factory.getObject();
                }
            }
        }
        return jestClient;
    }

    private HttpClientConfig createHttpClientConfig() {
        String[] uris = this.esAddr.split(",");
        HttpClientConfig.Builder builder = new HttpClientConfig.Builder(Arrays.asList(uris));
        if (StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password)) {
            builder.defaultCredentials(this.username, this.password);
        }
        return builder.multiThreaded(true).build();
    }

    /**
     * 判断集成执行是否成功
     *
     * @param historyDtoList
     * @return
     */
    public List<EnvoyIntegrationExecutionHistoryDto> getExecutionStatus(List<EnvoyIntegrationExecutionHistoryDto> historyDtoList, int limit) {
        if (historyDtoList.isEmpty()) {
            return historyDtoList;
        }
        JestClient jestClient = getJestClient();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //取出所有集成执行号
        for (EnvoyIntegrationExecutionHistoryDto envoyIntegrationExecutionHistoryDto : historyDtoList) {
            if (envoyIntegrationExecutionHistoryDto.getExecutionId() != null) {
                queryBuilder.should(QueryBuilders.termQuery("recordId", envoyIntegrationExecutionHistoryDto.getExecutionId()));
            }
        }
        //查询是否存在对应的成功log
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(limit).query(queryBuilder);
        Search.Builder builder = new Search.Builder(searchSourceBuilder.toString());
        //获取指定天数的索引
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int time = -1;
        calendar.add(Calendar.DATE, 0);
        builder.addIndex(esStatusPrefix + sdf.format(calendar.getTime()) + "*");
        for (int i = 0; i < days - 1; i++) {
            calendar.add(Calendar.DATE, time);
            builder.addIndex(esStatusPrefix + sdf.format(calendar.getTime()) + "*");
        }
        Search search = builder.build();
        List<String> recordIdList = new ArrayList<>();
        try {
            SearchResult searchResult = jestClient.execute(search);
            if (searchResult.isSucceeded() && !searchResult.getJsonObject().get("hits").isJsonNull() &&
                    !searchResult.getJsonObject().get("hits").getAsJsonObject().get("hits").isJsonNull()) {
                Iterator<JsonElement> jsonElementIterator = searchResult.getJsonObject().get("hits").getAsJsonObject().get("hits").getAsJsonArray().iterator();
                JsonObject next;
                while (jsonElementIterator.hasNext()) {
                    next = jsonElementIterator.next().getAsJsonObject().get("_source").getAsJsonObject();
                    if (!next.get("recordId").isJsonNull()) {
                        recordIdList.add(next.get("recordId").getAsString());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Exception:", e);
            return historyDtoList;
        }
        if (!recordIdList.isEmpty()) {
            for (EnvoyIntegrationExecutionHistoryDto envoyIntegrationExecutionHistoryDto : historyDtoList) {
                if (envoyIntegrationExecutionHistoryDto.getExecutionId() != null) {
                    if (recordIdList.contains(envoyIntegrationExecutionHistoryDto.getExecutionId())) {
                        envoyIntegrationExecutionHistoryDto.setExecutionStatus(1);
                    }
                }
            }
        }
        return historyDtoList;
    }

    @Override
    public ErrorCode checkDescribeParam(long offset, long limit) {
        return CommonUtil.checkOffsetAndLimit(offset, limit);
    }

    @Override
    public List<Map<String, Object>> getIntegrationExceptionLog(String executionId) {
        return getIntegrationLog(executionId, esExceptionPrefix);
    }

    @Override
    public List<Map<String, Object>> getIntegrationTraceLog(String executionId) {
        return getIntegrationLog(executionId, esPrefix);
    }

    @Override
    public Map<String, Object> toStepMap(List<Map<String, Object>> list) {
        Map<String, Object> res = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        Map<String, List<Map<String, Object>>> temp = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        String stepId = null;
        if (list == null || list.size() == 0) {
            return res;
        }
        for (Map<String, Object> map : list) {
            stepId = String.valueOf(map.get("stepId"));
            if (stepId != null) {
                if (temp.containsKey(stepId)) {
                    temp.get(stepId).add(map);
                } else {
                    List<Map<String, Object>> mapList = new ArrayList<>();
                    mapList.add(map);
                    temp.put(stepId, mapList);
                }
            }
        }
        res.putAll(temp);
        return res;
    }

    @Override
    public String getStep(long id) {
        EnvoyIntegrationInfo integrationInfo = integrationService.getIntegrationInfoById(id);
        if (integrationInfo == null) {
            return null;
        } else {
            return integrationInfo.getStep();
        }
    }
}