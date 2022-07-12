package org.hango.cloud.dashboard.apiserver.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.exception.HostUnReachableException;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.envoy.meta.PromResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public static final String PROM_ALERT_TEMPLATE = "gateway:<level>:<type>:minute{<filter>} <algorithm>";

    public static final String ALERT_MSG_TEMPLATE = "您的<dimension_desc> {{label.<dimension_name>}} 的<metric_desc>已 <op> <value> <unit>, 当前值{{value}} <unit>, 链接: <domain><suffix>?tenantId=<tenantId>&projectId=<projectId>";

    private final static String OR = " \tor\t ";

    private final static String ALL = ".*";

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
                    final PromUtils.Params params = ((PromUtils.Params) typeInfo.baseParams.clone())
                            .p("filter", filter.toArray(new String[0][]));
                    return PromUtils.interpolate(typeInfo.template, params);
                })
                .collect(Collectors.toList());

        return String.join(OR, vars);
    }

    private static String mkString(Object value) {
        if (value == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        if (value instanceof String | value instanceof Long | value instanceof Integer | value instanceof Double) {
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

    /**
     * 获取第一组匹配的数据
     *
     * @param exprStr
     * @param regex
     * @return
     */
    public static String firstMatch(String exprStr, String regex) {
        final Matcher matcher = Pattern.compile(regex).matcher(exprStr);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * 将单条表达式转换成告警目标, 此处仅使用{}内的信息
     *
     * @param targetStr eg. (gateway:service:count:minute{GatewayId=~\"1\",ApiId=~\"0\",ServiceId=~\"0\",project=\"177\",})>22.0
     * @return
     */
    public static Map<String, String> parseTarget(String targetStr, Collection<String> labelNames) {
        // eg. service="provider1",
        final String LABEL = "\\w+=(~?)\"[^\"]+\",";
        // eg. {gw="dev1",service="provider1",}
        final String labels = PromUtils.firstMatch(targetStr, "\\{(" + LABEL + ")+}");
        final HashMap<String, String> result = new HashMap<>();
        final Matcher mth = Pattern.compile(LABEL).matcher(labels);
        while (mth.find()) {
            final String[] labelAndValue = mth.group(0).split("=(~?)");
            if (labelNames.contains(labelAndValue[0])) {
                final String labelStr = labelAndValue[1];
                result.put(labelAndValue[0], labelStr.substring(1, labelStr.length() - 2));
            }
        }
        return result;
    }

    public static long readCountData(String queryAddr, Map<String, Object> queryParams) {
        PromResponse promResponse = readPromData(queryAddr, queryParams);
        if (promResponse == null || CollectionUtils.isEmpty(promResponse.getData().getResult())) {
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

    public static PromResponse readPromData(String queryAddr, Map<String, Object> queryParams) {
        logger.info("prometheus query params is {}", JSON.toJSONString(queryParams));
        ResultActionWithMessage resultActionWithMessage = null;
        try {
            HttpClientResponse httpClientResponse = HttpClientUtil.httpRequestWithFormData(Const.POST_METHOD, queryAddr, queryParams, null);
            resultActionWithMessage = AccessUtil.convertResponse(httpClientResponse);
            if (resultActionWithMessage.getStatusCode() != HttpStatus.SC_OK) {
                logger.warn("查询Prometheus数据失败，Prometheus 地址: {},查询条件 :{}", queryAddr, JSON.toJSONString(queryParams));
                return null;
            }
            PromResponse promResponse = JSON.parseObject(String.valueOf(resultActionWithMessage.getResult()), PromResponse.class);
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
        } catch (HostUnReachableException e) {
            logger.warn("请求失败，{}", e.getMessage());
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            logger.warn("返回解析失败，{}", throwable.getMessage());
            throwable.printStackTrace();
        }
        return null;
    }

    public static class Labels {
        public static final String CLUSTER_NAME = "cluster_name";
        public static final String ENVOY_CLUSTER_NAME = "envoy_cluster_name";
        public static final String INSTANCE_NAME = "instance_name";
    }

    public static class Metric {
        public static final String TOTAL_COUNT = "envoy_cluster_upstream_rq_total";
        public static final String XX_COUNT = "envoy_cluster_upstream_rq_xx";
        public static final String TIME_BUCKET = "envoy_cluster_upstream_rq_time_bucket";
        public static final String TIME_SUM = "envoy_cluster_upstream_rq_time_sum";
        public static final String TIME_COUNT = "envoy_cluster_upstream_rq_time_count";
    }

    public static class PromExpTemplate {
        public String template;
        public PromUtils.Params baseParams;
        public List<String[]> baseFilter;
    }

    public static class Params extends HashMap<String, Object> {
        public Params p(String key, Object value) {
            put(key, value);
            return this;
        }
    }
}
