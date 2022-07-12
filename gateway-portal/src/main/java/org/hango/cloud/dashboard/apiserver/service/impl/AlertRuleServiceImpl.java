package org.hango.cloud.dashboard.apiserver.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.dto.alertdto.AlertRuleDto;
import org.hango.cloud.dashboard.apiserver.dto.alertdto.AlertRuleDtoExpr;
import org.hango.cloud.dashboard.apiserver.meta.DimensionType;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.MetricTypeEnum;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IAlertRuleService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.AccessUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.PromUtils;
import org.hango.cloud.dashboard.apiserver.util.ResultActionWithMessage;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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
@Component
public class AlertRuleServiceImpl implements IAlertRuleService {

    private static final Logger logger = LoggerFactory.getLogger(AlertRuleServiceImpl.class);

    private final static String OR = " \tor\t ";

    private final static String ALL = ".*";

    private final String baseAlertRuleUrl = "/api/v1/alert/rule/GW";

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private ApiServerConfig apiServerConfig;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IApiInfoService apiInfoService;


    @Override
    public ResultActionWithMessage addRule(AlertRuleDto dto, long tenantId, long projectId, String accountId) {
        Map<String, String> headers = getHttpHeaders(tenantId, projectId, accountId);
        String url = apiServerConfig.getAlertRuleUrl() + baseAlertRuleUrl;
        AlertRuleDtoExpr expr = dto.getExpression();
        ErrorCode errorCode = makeTarget(expr);
        if (!CommonErrorCode.Success.equals(errorCode)) {
            return new ResultActionWithMessage(errorCode);
        }
        dto.setExprStr(makeExpr(expr));
        makeAnnotations(dto, tenantId, projectId);
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(url, new HashMap<>(), JSON.toJSONString(dto), headers, Const.POST_METHOD);
        return AccessUtil.convertResponse(httpClientResponse);
    }

    @Override
    public ResultActionWithMessage updateRule(AlertRuleDto dto, long tenantId, long projectId, String accountId) {
        Map<String, String> headers = getHttpHeaders(tenantId, projectId, accountId);
        String url = apiServerConfig.getAlertRuleUrl() + baseAlertRuleUrl + "/" + URLEncoder.encode(dto.getName());
        AlertRuleDtoExpr expr = dto.getExpression();
        ErrorCode errorCode = makeTarget(expr);
        if (!CommonErrorCode.Success.equals(errorCode)) {
            return new ResultActionWithMessage(errorCode);
        }
        dto.setExprStr(makeExpr(expr));
        makeAnnotations(dto, tenantId, projectId);
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(url, new HashMap<>(), JSON.toJSONString(dto), headers, Const.PUT_METHOD);
        return AccessUtil.convertResponse(httpClientResponse);
    }

    @Override
    public ResultActionWithMessage deleteRule(String name, long tenantId, long projectId, String accountId) {
        Map<String, String> headers = getHttpHeaders(tenantId, projectId, accountId);
        String url = apiServerConfig.getAlertRuleUrl() + baseAlertRuleUrl + "/" + URLEncoder.encode(name);
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(url, new HashMap<>(), null, headers, Const.DELETE_METHOD);
        return AccessUtil.convertResponse(httpClientResponse);
    }

    @Override
    public ResultActionWithMessage getRule(String name, long tenantId, long projectId, String accountId) {
        Map<String, String> headers = getHttpHeaders(tenantId, projectId, accountId);
        String url = apiServerConfig.getAlertRuleUrl() + baseAlertRuleUrl + "/" + URLEncoder.encode(name);
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(url, new HashMap<>(), null, headers, Const.GET_METHOD);
        ResultActionWithMessage ram = AccessUtil.convertResponse(httpClientResponse);
        if (ram.getStatusCode() != HttpStatus.SC_OK || ram.getResult() == null) {
            return ram;
        }
        JSONObject jsonObject = JSON.parseObject(String.valueOf(ram.getResult()));
        AlertRuleDto result = JSON.parseObject(jsonObject.getString("Result"), AlertRuleDto.class);
        parseAnnotations(result);
        AlertRuleDtoExpr expr = parseExpr(result.getExprStr());
        convertTarget(expr, false);
        result.setExpression(expr);
        return new ResultActionWithMessage(CommonErrorCode.Success.statusCode, CommonErrorCode.SUCCESS, CommonErrorCode.SUCCESS, result);
    }

    @Override
    public ResultActionWithMessage getRuleList(long tenantId, long projectId, String accountId, String sort, boolean asc, boolean showAll, int offset, int limit) {
        Map<String, String> headers = getHttpHeaders(tenantId, projectId, accountId);
        String url = apiServerConfig.getAlertRuleUrl() + baseAlertRuleUrl;
        Map<String, String> params = new HashMap<>();
        params.put("showAll", String.valueOf(showAll));
        params.put("asc", String.valueOf(asc));
        params.put("offset", String.valueOf(offset));
        params.put("limit", String.valueOf(limit));
        if (sort != null) {
            params.put("sort", sort);
        }

        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(url, params, null, headers, Const.GET_METHOD);
        ResultActionWithMessage ram = AccessUtil.convertResponse(httpClientResponse);
        if (ram.getStatusCode() != HttpStatus.SC_OK || ram.getResult() == null) {
            return ram;
        }
        List<AlertRuleDto> ruleDtoList = null;
        JSONObject jsonObject = JSON.parseObject(String.valueOf(ram.getResult()));
        JSONObject r1 = jsonObject.getJSONObject("Result");
        Map<String, Object> result = new HashMap<>();
        if (r1 != null) {
            ruleDtoList = JSON.parseArray(r1.getString("result"), AlertRuleDto.class);
            if (!CollectionUtils.isEmpty(ruleDtoList)) {
                for (AlertRuleDto alertRuleDto : ruleDtoList) {
                    parseAnnotations(alertRuleDto);
                    AlertRuleDtoExpr expr = parseExpr(alertRuleDto.getExprStr());
                    convertTarget(expr, true);
                    alertRuleDto.setExpression(expr);
                }
            }
            result.put("Result", ruleDtoList);
            result.put("TotalCount", r1.get("total"));
        }

        return new ResultActionWithMessage(CommonErrorCode.Success.statusCode, CommonErrorCode.SUCCESS, CommonErrorCode.SUCCESS, result);
    }

    /**
     * 组装告警信息
     *
     * @param expr
     * @param tenantId
     * @param projectId
     * @return
     */
    private String genMessage(AlertRuleDtoExpr expr, long tenantId, long projectId) {
        DimensionType dimensionType = DimensionType.getByTarget(expr.getLevel());
        PromUtils.Params params = PromUtils.params()
                .p("dimension_desc", dimensionType.getDescription())
                .p("dimension_name", dimensionType.getDimensionType())
                .p("metric_desc", MetricTypeEnum.getDescription(expr.getType()))
                .p("op", expr.getOperator())
                .p("value", expr.getValue())
                .p("unit", MetricTypeEnum.getUnit(expr.getType()))
                .p("tenantId", tenantId)
                .p("projectId", projectId)
                .p("domain", apiServerConfig.getApiServerUrl())
                .p("suffix", dimensionType.getSuffix());
        return PromUtils.interpolate(PromUtils.ALERT_MSG_TEMPLATE, params);
    }


    /**
     * 提取监控指标信息
     *
     * @param expr
     * @return
     */
    private PromUtils.PromExpTemplate extractTypeInfo(AlertRuleDtoExpr expr) {
        final PromUtils.PromExpTemplate typeInfo = new PromUtils.PromExpTemplate();
        typeInfo.baseParams = PromUtils.params()
                .p("level", DimensionType.getDimensionType(expr.getLevel()))
                .p("by", DimensionType.getLevelToby(expr.getLevel()));
        typeInfo.baseFilter = new ArrayList<>();
        final String dtoType = expr.getType();
        MetricTypeEnum metricTypeEnum = MetricTypeEnum.get(dtoType);
        if (metricTypeEnum != null) {
            String type = metricTypeEnum.getPromType();
            typeInfo.baseParams.put("type", type);
            //QPS / 60
            String algorithm = StringUtils.EMPTY;
            if (MetricTypeEnum.QPS.equals(metricTypeEnum)) {
                algorithm = "/ 60";
            } else if (MetricTypeEnum.FAILED_RATE.equals(metricTypeEnum)) {
                algorithm = "* 100";
            }
            typeInfo.baseParams.p("algorithm", algorithm);
            typeInfo.template = PromUtils.PROM_ALERT_TEMPLATE;
        }
        return typeInfo;
    }

    /**
     * 组装Prometheus表达式
     *
     * @param expr
     * @return
     */
    private String makeExpr(AlertRuleDtoExpr expr) {
        final PromUtils.PromExpTemplate typeInfo = extractTypeInfo(expr);
        return "(" + PromUtils.makeExpr(typeInfo, expr.getTargets()) + expr.getOperator() + String.valueOf(expr.getValue()) + ")";
    }

    /**
     * 组装告警等级及信息
     *
     * @param dto
     * @param tenantId
     * @param projectId
     */
    private void makeAnnotations(AlertRuleDto dto, long tenantId, long projectId) {
        Map<String, Object> annotations = dto.getAnnotations();
        Map<String, Object> makeAnno = new HashMap<>();
        makeAnno.put("message", genMessage(dto.getExpression(), tenantId, projectId));
        makeAnno.put("level", annotations.get("Level"));
        dto.setAnnotations(makeAnno);
    }

    /**
     * 解析告警等级及信息
     *
     * @param dto
     */
    private void parseAnnotations(AlertRuleDto dto) {
        Map<String, Object> annotations = dto.getAnnotations();
        Map<String, Object> makeAnno = new HashMap<>();
        makeAnno.put("Message", annotations.get("message"));
        makeAnno.put("Level", annotations.get("level"));
        dto.setAnnotations(makeAnno);
    }

    /**
     * 组装target
     *
     * @param expr
     * @return
     */
    private ErrorCode makeTarget(AlertRuleDtoExpr expr) {
        List<Map<String, String>> targets = expr.getTargets();
        if (CollectionUtils.isEmpty(targets)) {
            return CommonErrorCode.Success;
        }
        List<Map<String, String>> t2 = new ArrayList<>();
        for (Map<String, String> target : targets) {
            if (CollectionUtils.isEmpty(target)) {
                continue;
            }
            Map<String, String> m2 = new HashMap<>();
            //处理网关信息
            String gwId = target.get(DimensionType.GATEWAY.getTarget());
            long gwIdForLong = NumberUtils.toLong(gwId);
            GatewayInfo gatewayInfo = gatewayInfoService.get(gwIdForLong);
            if (gatewayInfo == null) {
                return CommonErrorCode.InvalidParameterGwId(String.valueOf(gwId));
            }
            m2.put(DimensionType.GATEWAY.getDimensionType(), gatewayInfo.getGwUniId());
            //处理服务信息
            String serviceId = target.get(DimensionType.SERVICE.getTarget());
            long serviceIdForLong = NumberUtils.toLong(serviceId);
            String serviceName = null;
            if (serviceIdForLong == NumberUtils.LONG_ZERO) {
                serviceName = ALL;
            } else {
                ServiceInfo serviceByServiceId = serviceInfoService.getServiceByServiceId(serviceIdForLong);
                if (serviceByServiceId == null) {
                    return CommonErrorCode.InvalidParameterServiceId(serviceId);
                }
                serviceName = serviceByServiceId.getServiceName();
            }
            m2.put(DimensionType.SERVICE.getDimensionType(), serviceName);
            //处理API信息
            String apiId = target.get(DimensionType.API.getTarget());
            long apiIdForLong = NumberUtils.toLong(apiId);
            String apiTag = null;
            if (apiIdForLong == NumberUtils.LONG_ZERO) {
                apiTag = ALL;
            } else {
                ApiInfo apiById = apiInfoService.getApiById(apiIdForLong);
                if (apiById == null) {
                    return CommonErrorCode.InvalidParameterApiId(apiId);
                }
                apiTag = apiById.getApiPath() + "_" + apiById.getApiMethod();
            }
            m2.put(DimensionType.API.getDimensionType(), apiTag);
            t2.add(m2);
        }
        expr.setTargets(t2);
        return CommonErrorCode.Success;
    }

    /**
     * 转换target
     *
     * @param expr
     * @param isNameNeed 返回的Target对应value 是否是Name(如果不是，value则为Id)
     */
    private void convertTarget(AlertRuleDtoExpr expr, boolean isNameNeed) {
        List<Map<String, String>> targets = expr.getTargets();
        if (CollectionUtils.isEmpty(targets)) {
            return;
        }
        List<Map<String, String>> t2 = new ArrayList<>();
        for (Map<String, String> target : targets) {
            if (CollectionUtils.isEmpty(target)) {
                continue;
            }
            Map<String, String> m2 = new HashMap<>();
            //处理网关信息
            String gwUniId = target.get(DimensionType.GATEWAY.getDimensionType());
            GatewayInfo gatewayInfo = gatewayInfoService.getGwByUniId(gwUniId);
            if (gatewayInfo == null) {
                logger.info("从Prometheus表达式解析网关信息失败，gwUniId ={}", gwUniId);
                continue;
            }
            m2.put(DimensionType.GATEWAY.getTarget(), isNameNeed ? gatewayInfo.getGwName() : String.valueOf(gatewayInfo.getId()));
            //处理服务信息
            String serviceName = target.get(DimensionType.SERVICE.getDimensionType());
            if (ALL.equals(serviceName)) {
                t2.add(m2);
                continue;
            }
            ServiceInfo serviceByServiceId = serviceInfoService.getServiceByServiceName(serviceName);
            long serviceId = 0;
            if (serviceByServiceId != null) {
                serviceId = serviceByServiceId.getId();
                m2.put(DimensionType.SERVICE.getTarget(), isNameNeed ? serviceByServiceId.getDisplayName() : String.valueOf(serviceId));
            }
            //处理API信息
            String apiTag = target.get(DimensionType.API.getDimensionType());
            if (ALL.equals(apiTag)) {
                t2.add(m2);
                continue;
            }
            String[] apiSp = apiTag.split("_");
            if (apiSp.length != 2) {
                logger.info("从Prometheus表达式解析API信息失败，apiExpr ={}", apiTag);
                t2.add(m2);
                continue;
            }
            ApiInfo apiById = apiInfoService.getApiInfoByApiPathAndService(apiSp[0], apiSp[1], serviceId);
            if (apiById == null) {
                logger.info("从Prometheus表达式解析API信息失败，apiExpr ={}", apiTag);
                t2.add(m2);
                continue;
            }
            m2.put(DimensionType.API.getTarget(), isNameNeed ? apiById.getApiName() : String.valueOf(apiById.getId()));
            t2.add(m2);
        }
        expr.setTargets(t2);
    }

    /**
     * 解析表达式
     *
     * @param exprStr eg. (gateway:service:count:minute{GatewayId=~\"1\",ApiId=~\"0\",ServiceId=~\"0\",project=\"177\",})>22.0
     */
    public AlertRuleDtoExpr parseExpr(String exprStr) {
        exprStr = StringUtils.trim(exprStr);
        final AlertRuleDtoExpr result = new AlertRuleDtoExpr();
        final String OPERATOR = "[=<>!]+";
        final String VALUE = "\\-?[\\d.]+$";
        final String OPERATOR_AND_VALUE = OPERATOR + "\\s*\\-?" + VALUE;
        String operatorAndValue = PromUtils.firstMatch(exprStr, OPERATOR_AND_VALUE);
        result.setOperator(PromUtils.firstMatch(operatorAndValue, OPERATOR));
        result.setValue(NumberUtils.toDouble(PromUtils.firstMatch(operatorAndValue, VALUE)));
        final String TAIL = "\\)\\s*" + OPERATOR_AND_VALUE;
        exprStr = exprStr.substring(1).replaceAll(TAIL, "");
        final String[] clauses = exprStr.split(OR);
        List<Map<String, String>> targets = Arrays.stream(clauses)
                .map(target -> PromUtils.parseTarget(target, DimensionType.getDimensionTypeSet()))
                .collect(Collectors.toList());
        parseTypeAndLevel(result, clauses[0]);
        result.setTargets(targets);
        return result;
    }

    /**
     * 解析监控维度、指标信息
     *
     * @param dto
     * @param clause
     */
    private void parseTypeAndLevel(AlertRuleDtoExpr dto, String clause) {
        //参照countTemplate
        final Pattern PATTERN = Pattern.compile("gateway:(" + DimensionType.getDimensionTypePattern() + "):(" + MetricTypeEnum.getPromTypePattern() + "):minute\\{.*?}");
        final Matcher mth = PATTERN.matcher(clause);
        if (!mth.find()) {
            return;
        }
        dto.setLevel(DimensionType.getTarget(mth.group(1)));
        String promType = mth.group(2);
        dto.setType(MetricTypeEnum.getMetricType(promType));
    }


    private Map<String, String> getHttpHeaders(long tenantId, long projectId, String accountId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-auth-accountId", accountId);
        headers.put("x-auth-projectId", String.valueOf(projectId));
        headers.put("x-auth-tenantId", String.valueOf(tenantId));
        return headers;
    }


}
