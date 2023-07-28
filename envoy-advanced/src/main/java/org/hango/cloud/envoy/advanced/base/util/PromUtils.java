package org.hango.cloud.envoy.advanced.base.util;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.envoy.advanced.metric.meta.PromResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/7/5
 */
public class PromUtils {

    public static final Logger logger = LoggerFactory.getLogger(PromUtils.class);

    private final static String OR = " \tor\t ";


    public static Params params() {
        return new Params();
    }

    public static String interpolate(String template, Map<String, Object> params) {
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result = result.replaceAll("<" + entry.getKey() + ">", mkString(entry.getValue()));
        }
        result = result.replaceAll("<[\\w]+>", "");
        return result;
    }

    /**
     * 组装Prometheus表达式
     *
     * @param typeInfo
     * @param targets
     * @return
     */
    public static String makeExpr(PromExpTemplate typeInfo, List<Map<String, String>> targets) {
        final List<String> vars = targets.stream()
                .map(target -> {
                    final List<String[]> filter = target.entrySet().stream()
                            .map(entry -> new String[]{entry.getKey(), "=~", entry.getValue()})
                            .collect(Collectors.toList());
                    if (typeInfo.baseFilter != null) {
                        filter.addAll(typeInfo.baseFilter);
                    }
                    final Params params = ((Params) typeInfo.baseParams.clone())
                            .p("filter", filter.toArray(new String[0][]));
                    return PromUtils.interpolate(typeInfo.template, params);
                })
                .collect(Collectors.toList());

        return String.join(OR, vars);
    }

    @SuppressWarnings("java:S3776")
    private static String mkString(Object value) {
        if (value == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        if (value instanceof String || value instanceof Long || value instanceof Integer || value instanceof Double) {
            return value.toString();
        } else if (value instanceof String[]) {
            String[] typedValue = (String[]) value;
            for (String str : typedValue) {
                sb.append(str);
                sb.append(",");
            }
            return sb.toString();
        } else if (value instanceof String[][]) {
            String[][] typedValue = (String[][]) value;
            for (String[] strs : typedValue) {
                if (strs.length == 2) {
                    if (strs[1] == null) {
                        continue;
                    }
                    sb.append(strs[0]);
                    sb.append("=\"");
                    sb.append(strs[1]);
                    sb.append("\",");
                } else if (strs.length == 3) {
                    if (strs[2] == null) {
                        continue;
                    }
                    sb.append(strs[0]);
                    sb.append(strs[1]);
                    sb.append("\"");
                    sb.append(strs[2]);
                    sb.append("\",");
                }
            }
            return sb.toString();
        } else {
            throw new IllegalArgumentException("unsupported type: " + value.getClass().getName());
        }
    }


    public static PromResponse readPromData(String queryAddr, Map<String, Object> queryParams) {
        logger.info("prometheus query params is {}", JSON.toJSONString(queryParams));
        try {
            HttpClientResponse httpClientResponse = HttpClientUtil.postRequest(queryAddr, queryParams, AdvancedConst.MODULE_PLATFORM_PROM);
            if (httpClientResponse.getStatusCode() != HttpStatus.SC_OK) {
                logger.warn("查询Prometheus数据失败，Prometheus 地址: {},查询条件 :{}", queryAddr, JSON.toJSONString(queryParams));
                return null;
            }
            PromResponse promResponse = JSON.parseObject(String.valueOf(httpClientResponse.getResponseBody()), PromResponse.class);
            if (promResponse == null) {
                logger.warn("查询Prometheus数据失败");
                return null;
            }
            if (!promResponse.getStatus().equals("success")) {
                logger.warn("查询Prometheus数据失败，{}", JSON.toJSONString(promResponse));
                return null;
            }
            if (promResponse.getData() == null) {
                logger.warn("查询Prometheus数据失败，{}", JSON.toJSONString(promResponse));
                return null;
            }
            return promResponse;
        } catch (Throwable throwable) {
            logger.warn("返回解析失败，{}", throwable.getMessage());
            throwable.printStackTrace();
        }
        return null;
    }


    public static class PromExpTemplate {
        public String template;
        public Params baseParams;
        public List<String[]> baseFilter;
    }

    public static class Params extends HashMap<String, Object> {
        public Params p(String key, Object value) {
            put(key, value);
            return this;
        }
    }
}
