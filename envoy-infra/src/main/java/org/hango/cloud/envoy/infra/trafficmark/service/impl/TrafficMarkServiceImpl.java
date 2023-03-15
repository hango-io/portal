package org.hango.cloud.envoy.infra.trafficmark.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.meta.EnvoyErrorCode;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderDto;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.hango.cloud.envoy.infra.trafficmark.dao.ITrafficMarkDao;
import org.hango.cloud.envoy.infra.trafficmark.dto.TrafficMarkDto;
import org.hango.cloud.envoy.infra.trafficmark.dto.TrafficMarkParamDto;
import org.hango.cloud.envoy.infra.trafficmark.meta.TrafficMarkInfo;
import org.hango.cloud.envoy.infra.trafficmark.service.ITrafficMarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 流量染色 service层接口实现
 *
 * @author qilu01
 */
@Service
public class TrafficMarkServiceImpl implements ITrafficMarkService {
    private static Logger logger = LoggerFactory.getLogger(TrafficMarkServiceImpl.class);

    @Autowired
    private ITrafficMarkDao envoyTrafficColorDao;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IPluginManagerService pluginManagerService;

    @Override
    public List<TrafficMarkInfo> getTrafficColorByTagLimit(String colorTag, long offset, long limit) {
        return envoyTrafficColorDao.getTrafficColorByTagLimit(colorTag, ProjectTraceHolder.getProId(), offset, limit);
    }

    @Override
    public long getTrafficColorRuleCountByColorTag(String colorTag) {
        Map<String, Object> params = Maps.newHashMap();
        if (StringUtils.isBlank(colorTag)) {
            return envoyTrafficColorDao.getCount();
        }
        params.put("colorTag", colorTag);
        params.put("projectId", ProjectTraceHolder.getProId());
        return envoyTrafficColorDao.getCountByFields(params);
    }

    @Override
    public TrafficMarkInfo addTrafficColorInfo(TrafficMarkDto envoyTrafficColorDto) {
        TrafficMarkInfo envoyTrafficColorInfo = TrafficMarkDto.toMeta(envoyTrafficColorDto);
        envoyTrafficColorInfo.setEnableStatus(EnvoyConst.PLUGIN_STATE_DISABLE);
        envoyTrafficColorInfo.setCreateTime(System.currentTimeMillis());
        envoyTrafficColorInfo.setUpdateTime(System.currentTimeMillis());
        envoyTrafficColorInfo.setProjectId(ProjectTraceHolder.getProId());
        envoyTrafficColorInfo.setId(envoyTrafficColorDao.add(envoyTrafficColorInfo));
        return envoyTrafficColorInfo;
    }

    @Override
    public ErrorCode checkCreateTrafficColorParam(TrafficMarkDto envoyTrafficColorDto) {
        String trafficColorName;
        try {
            trafficColorName = envoyTrafficColorDto.getTrafficColorName();
        } catch (Exception e) {
            logger.info("流量染色规则名称为空");
            return EnvoyErrorCode.TRAFFIC_COLOR_RULE_NAME_IS_EMPTY;
        }
        if (!trafficMatchCheck(envoyTrafficColorDto.getTrafficMatch())) {
            logger.info("流量匹配不支持 当前仅支持Header、Parameter匹配");
            return EnvoyErrorCode.TRAFFIC_MATCH_NOT_SUPPORT;
        }
        //判断该流量规则名称是否已经存在
        if (isTrafficColorNameExists(trafficColorName)) {
            logger.info("创建流量染色规则，当前项目下该流量染色规则名称 trafficColorName {} 已经存在", trafficColorName);
            return EnvoyErrorCode.TRAFFIC_COLOR_RULE_NAME_ALREADY_EXIST;
        }
        //判断该服务 路由下是否已有流量染色规则 如果有则返回错误码
        if (isTrafficColorExists(envoyTrafficColorDto.getRouteRuleIds())) {
            logger.info("创建流量染色规则，当前路由列表中存在已创建流量染色规则的路由，不允许重复创建");
            return EnvoyErrorCode.TRAFFIC_COLOR_RULE_ALREADY_EXIST;
        }
        return CommonErrorCode.SUCCESS;
    }


    @Override
    public boolean isTrafficColorNameExists(String trafficColorName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("trafficColorName", trafficColorName);
        params.put("projectId", ProjectTraceHolder.getProId());
        return envoyTrafficColorDao.getCountByFields(params) > 0;
    }

    /**
     * 流量匹配校验
     *
     * @param trafficMatch 流量匹配 目前仅支持Header
     * @return Header为校验成功返回true 其他情况校验失败返回false
     */
    public boolean trafficMatchCheck(String trafficMatch) {
        if (StringUtils.isEmpty(trafficMatch)) {
            logger.info("流量匹配为空");
            return false;
        }
        switch (trafficMatch) {
            case "Header":
            case "Parameter":
                return true;
            default:
                logger.info("流量匹配 {} 不支持 当前仅支持Header、Parameter匹配", trafficMatch);
                return false;
        }
    }

    @Override
    public boolean isTrafficColorExists(String routeRuleIds) {
        if (StringUtils.isEmpty(routeRuleIds)) {
            return false;
        }
        for (String id : routeRuleIds.split(",")) {
            List<TrafficMarkInfo> trafficMarkInfoList = getTrafficColorRulesByRouteRuleId(Long.parseLong(id));
            if (!CollectionUtils.isEmpty(trafficMarkInfoList)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ErrorCode checkUpdateTrafficColorParam(TrafficMarkDto envoyTrafficColorDto) {
        //参数校验
        if (!isTrafficColorExists(envoyTrafficColorDto.getId())) {
            logger.info("更新流量染色规则，流量染色规则不存在");
            return EnvoyErrorCode.NO_SUCH_TRAFFIC_COLOR_RULE;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public boolean isTrafficColorExists(long id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        return envoyTrafficColorDao.getCountByFields(params) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateTrafficColorInfo(TrafficMarkInfo envoyTrafficColorInfo) {
        if (null == envoyTrafficColorInfo) {
            return false;
        }

        long trafficMarkRuleId = envoyTrafficColorInfo.getId();
        String paramRule = envoyTrafficColorInfo.getTrafficParam();

        // 更新染色规则DB
        envoyTrafficColorInfo.setUpdateTime(System.currentTimeMillis());
        if (1 != envoyTrafficColorDao.update(envoyTrafficColorInfo)) {
            logger.error("[traffic mark] update traffic_mark_rule failed, trafficMarkRuleId: {}", trafficMarkRuleId);
            return false;
        }

        // 未启用状态不需要流量染色更新插件
        if (envoyTrafficColorInfo.getEnableStatus() == EnvoyConst.PLUGIN_STATE_DISABLE) {
            return true;
        }

        // 染色规则启用状态下，更新插件动态生效；按照新规则(paramRule)启用流量染色规则插件
        ErrorCode enableResult = enableTrafficMarkPlugin(trafficMarkRuleId);
        if (!enableResult.equals(CommonErrorCode.SUCCESS)) {
            logger.error("[traffic mark] enable traffic_mark_plugin failed when updateTrafficColorInfo." +
                    "trafficMarkRuleId: {}, new param_rules: {}", trafficMarkRuleId, paramRule);
            return false;
        }

        return true;
    }

    @Override
    public TrafficMarkInfo getTrafficColorRuleById(long id) {
        return envoyTrafficColorDao.get(id);
    }

    @Override
    public void delete(long trafficColorRuleId) {
        envoyTrafficColorDao.delete(trafficColorRuleId);
    }

    @Override
    public boolean checkTrafficMarkStatus(Integer trafficMarkStatus) {
        return trafficMarkStatus.equals(EnvoyConst.PLUGIN_STATE_DISABLE) ||
                trafficMarkStatus.equals(EnvoyConst.PLUGIN_STATE_ENABLE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ErrorCode modifyTrafficMarkRuleStatus(Long trafficMarkRuleId, Integer trafficMarkStatus) {
        TrafficMarkInfo trafficColorRule = getTrafficColorRuleById(trafficMarkRuleId);

        // 根据trafficMarkStatus进行traffic_mark插件的更新或删除
        ErrorCode resultCode;
        if (trafficMarkStatus.equals(EnvoyConst.PLUGIN_STATE_ENABLE)) {
            resultCode = enableTrafficMarkPlugin(trafficMarkRuleId);
        } else {
            resultCode = disableTrafficMarkPlugin(trafficMarkRuleId);
        }
        if (!resultCode.equals(CommonErrorCode.SUCCESS)) {
            return resultCode;
        }

        // 更新流量染色规则DB
        trafficColorRule.setEnableStatus(trafficMarkStatus);
        if (!updateTrafficColorInfo(trafficColorRule)) {
            return CommonErrorCode.UPDATE_FAILURE;
        }
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 案例：
     * {
     * "kind": "traffic-mark",
     * "headerKey": [
     * {
     * "request": {
     * "headers": [
     * {
     * "match_type": "safe_regex_match",
     * "headerKey": "test_header_1",
     * "value": "test_value_1"
     * }
     * ],
     * "requestSwitch": true
     * },
     * "headerValue": "dev1",
     * "headerKey": "x-nsf-mark",
     * "operation": "create"
     * }
     * ]
     * }
     *
     * @param trafficColorRule 流量染色规则对象
     * @return 流量染色插件字符串，见案例
     */
    @Override
    public String assembleTrafficMarkPlugin(TrafficMarkInfo trafficColorRule) {
        if (trafficColorRule == null) {
            logger.error("[traffic mark] error trafficColorRule when assembleTrafficMarkPlugin");
            return "";
        }
        List<TrafficMarkParamDto> paramList = JSON.parseArray(trafficColorRule.getTrafficParam(), TrafficMarkParamDto.class);
        JSONObject plugin = new JSONObject();
        plugin.put("kind", EnvoyConst.PLUGIN_TYPE_TRAFFIC_MARK);
        JSONArray array = new JSONArray();
        plugin.put("headerKey", array);
        JSONObject rewriter = new JSONObject();
        array.add(rewriter);
        processRewriter(trafficColorRule, rewriter);
        switch (trafficColorRule.getTrafficMatch()) {
            case "Header":
                processHeaderMatcher(paramList, rewriter);
                break;
            case "Parameter":
                processParameterMatcher(paramList, rewriter);
                break;
            default:
                //不可达，前置已做流量染色匹配规则校验
                break;
        }
        return plugin.toJSONString();
    }

    private void processRewriter(TrafficMarkInfo trafficColorRule, JSONObject rewriter) {
        rewriter.put("headerKey", getTrafficMarkKey(trafficColorRule));
        rewriter.put("operation", "create");
        rewriter.put("headerValue", trafficColorRule.getColorTag());
    }

    private void processHeaderMatcher(List<TrafficMarkParamDto> paramList, JSONObject rewriter) {
        rewriter.put("request", generateHeaderMatcher(paramList));
    }

    private void processParameterMatcher(List<TrafficMarkParamDto> paramList, JSONObject rewriter) {
        rewriter.put("request", generateParameterMatcher(paramList));
    }

    private JSONObject generateHeaderMatcher(List<TrafficMarkParamDto> paramList) {
        JSONObject request = new JSONObject();
        JSONArray matchList = new JSONArray();
        for (TrafficMarkParamDto trafficColorMatch : paramList) {
            Map<String, String> matcher = Maps.newHashMap();
            matcher.put("headerKey", trafficColorMatch.getParaName());
            matcher.put("match_type", convertToEnvoyMatchType(trafficColorMatch.getType()));
            matcher.put("value", trafficColorMatch.getValue());
            matchList.add(matcher);
        }

        request.put("headers", matchList);
        return request;
    }

    private JSONObject generateParameterMatcher(List<TrafficMarkParamDto> paramList) {
        JSONObject request = new JSONObject();
        JSONArray matchList = new JSONArray();
        for (TrafficMarkParamDto trafficColorMatch : paramList) {
            Map<String, String> matcher = Maps.newHashMap();
            matcher.put("parameterKey", trafficColorMatch.getParaName());
            matcher.put("match_type", convertToEnvoyMatchType(trafficColorMatch.getType()));
            matcher.put("value", trafficColorMatch.getValue());
            matchList.add(matcher);
        }

        request.put("parameters", matchList);
        return request;
    }

    private String getTrafficMarkKey(TrafficMarkInfo trafficColorRule) {
        VirtualGatewayDto virtualGateway = ((VirtualGatewayDto) virtualGatewayInfoService.get(trafficColorRule.getVirtualGwId()));
        PluginOrderDto pluginManagers = pluginManagerService.getPluginManagers(virtualGateway);
        Assert.notNull(pluginManagers, "PluginManager配置信息有问题，请排查");
        List<PluginOrderItemDto> plugins = pluginManagers.getPlugins();
        Assert.notNull(plugins, "PluginManager配置信息有问题，请排查");
        Optional<PluginOrderItemDto> optional = plugins.stream().filter(e -> e.getName().equals(EnvoyConst.PLUGIN_NAME_TRAFFIC_MARK)).findFirst();
        Assert.isTrue(optional.isPresent(), "PluginManager不存在插件com.netease.traffic_mark的配置，请排查");
        logger.info("getTrafficMarkKey info :{}", JSON.toJSONString(optional.get()));
        JSONObject inline = (JSONObject) optional.get().getInline();
        Assert.notNull(inline, "PluginManager插件proxy.filters.http.traffic_mark的配置信息有问题，请排查");
        JSONObject settings = inline.getJSONObject("settings");
        Assert.notNull(settings, "PluginManager插件proxy.filters.http.traffic_mark的配置信息有问题，请排查");
        return settings.getString("header_key");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ErrorCode enableTrafficMarkPlugin(Long trafficMarkRuleId) {
        logger.info("[traffic mark] start to enable traffic_mark_plugin, trafficMarkRuleId: {}", trafficMarkRuleId);
        TrafficMarkInfo trafficColorRule = getTrafficColorRuleById(trafficMarkRuleId);
        for (String routeId : trafficColorRule.getRouteRuleIds().split(",")) {
            RouteRuleDto route = routeRuleInfoService.get(Long.parseLong(routeId));

            // 未发布的路由不允许启用流量染色插件
            if (route.getPublishStatus() != EnvoyConst.PLUGIN_STATE_ENABLE) {
                logger.warn("[traffic mark] route not publish, please publish route firstly," +
                        "route_id: {}, trafficMarkRuleId: {}", routeId, trafficMarkRuleId);
                return CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED;
            }

            // 组装插件
            String trafficMarkPlugin = assembleTrafficMarkPlugin(trafficColorRule);
            logger.info("[traffic mark] assembleTrafficMarkPlugin finished, traffic_mark_plugin: {}", trafficMarkPlugin);

            List<PluginBindingDto> pluginBindingList = pluginInfoService.getPluginBindingList(trafficColorRule.getVirtualGwId(),
                    routeId,
                    PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);

            boolean isAddTrafficMarkPlugin = true;
            for (PluginBindingDto pluginBindingDto : pluginBindingList) {
                // 已存在"traffic-mark"插件，进行插件配置修改
                if (pluginBindingDto.getPluginType().equals(EnvoyConst.PLUGIN_TYPE_TRAFFIC_MARK)) {
                    logger.info("[traffic mark] traffic_mark_plugin already exists, start to update, pluginBindingDto: {}",
                            JSON.toJSONString(pluginBindingDto));
                    isAddTrafficMarkPlugin = false;
                    boolean updateOk = pluginInfoService.update(pluginBindingDto) != BaseConst.ERROR_RESULT;
                    if (!updateOk) {
                        logger.error("[traffic mark] update traffic_mark_plugin failed, plugin_id: {}", pluginBindingDto.getId());
                        return EnvoyErrorCode.UPDATE_PLUGIN_INFO_FAILED;
                    }
                    break;
                }
            }
            // 新增"traffic-mark"插件
            if (isAddTrafficMarkPlugin) {

                PluginBindingDto pluginBindingDto = new PluginBindingDto();
                pluginBindingDto.setVirtualGwId(trafficColorRule.getVirtualGwId());
                pluginBindingDto.setBindingObjectType(BaseConst.PLUGIN_TYPE_ROUTE);
                pluginBindingDto.setBindingObjectId(String.valueOf(route.getId()));
                pluginBindingDto.setPluginType(EnvoyConst.PLUGIN_TYPE_TRAFFIC_MARK);
                pluginBindingDto.setPluginConfiguration(trafficMarkPlugin);
                pluginBindingDto.setProjectId(-1);
                pluginBindingDto.setTemplateId(-1);
                logger.info("[traffic mark] traffic_mark_plugin not exists, start to create, pluginBindingDto: {}",
                        JSON.toJSONString(pluginBindingDto));
                long result = pluginInfoService.create(pluginBindingDto);
                if (result == BaseConst.ERROR_RESULT) {
                    logger.error("[traffic mark] add new traffic_mark_plugin failed");
                    return EnvoyErrorCode.UPDATE_PLUGIN_INFO_FAILED;
                }
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ErrorCode disableTrafficMarkPlugin(Long trafficMarkRuleId) {
        logger.info("[traffic mark] start to disable traffic_mark_plugin, trafficMarkRuleId: {}", trafficMarkRuleId);
        TrafficMarkInfo trafficColorRule = getTrafficColorRuleById(trafficMarkRuleId);
        for (String routeId : trafficColorRule.getRouteRuleIds().split(",")) {
            // 获取路由ID
            RouteRuleDto route = routeRuleInfoService.get(Long.parseLong(routeId));

            List<PluginBindingDto> pluginBindingList = pluginInfoService.getPluginBindingList(trafficColorRule.getVirtualGwId(),
                    routeId,
                    PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);

            boolean isExists = false;
            for (PluginBindingDto pluginBindingDto : pluginBindingList) {
                if (pluginBindingDto.getPluginType().equals(EnvoyConst.PLUGIN_TYPE_TRAFFIC_MARK)) {
                    isExists = true;
                    pluginInfoService.delete(pluginBindingDto);
                    break;
                }
            }
            if (!isExists) {
                logger.warn("[traffic mark] traffic_mark_plugin not exists on this route, route_id: {}, route_name: {}",
                        routeId, route.getRouteRuleName());
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<TrafficMarkInfo> getTrafficColorRulesByRouteRuleId(long routeRuleId) {
        return envoyTrafficColorDao.getTrafficColorRulesByRouteRuleId(routeRuleId);
    }

    /**
     * 将前台的匹配规则转换为服务envoy格式的匹配规则
     *
     * @param matchType 前台的匹配规则
     * @return envoy可识别的匹配规则
     */
    private String convertToEnvoyMatchType(String matchType) {
        String envoyMatchType = "exact_match";
        if (StringUtils.isEmpty(matchType)) {
            return envoyMatchType;
        }
        switch (matchType) {
            case "exact":
                envoyMatchType = "exact_match";
                break;
            case "regex":
                envoyMatchType = "safe_regex_match";
                break;
            case "prefix":
                envoyMatchType = "prefix_match";
                break;
            default:
        }
        return envoyMatchType;
    }
}