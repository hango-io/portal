package org.hango.cloud.dashboard.envoy.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.ITrafficMarkDao;
import org.hango.cloud.dashboard.envoy.meta.BindingPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.TrafficMarkInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGatewayService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.service.ITrafficMarkService;
import org.hango.cloud.dashboard.envoy.web.dto.PluginOrderItemDto;
import org.hango.cloud.dashboard.envoy.web.dto.TrafficMarkDto;
import org.hango.cloud.dashboard.envoy.web.dto.TrafficMarkParamDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashMap;
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
    private static final Logger logger = LoggerFactory.getLogger(TrafficMarkServiceImpl.class);

    @Autowired
    private ITrafficMarkDao envoyTrafficColorDao;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;

    @Autowired
    private IEnvoyPluginInfoService pluginInfoService;

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private IEnvoyGatewayService envoyGatewayService;

    @Override
    public List<TrafficMarkInfo> getTrafficColorByTagLimit(String colorTag, long offset, long limit) {
        return envoyTrafficColorDao.getTrafficColorByTagLimit(colorTag, offset, limit);
    }

    @Override
    public long getTrafficColorRuleCountByColorTag(String colorTag) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        if (StringUtils.isBlank(colorTag)) {
            return envoyTrafficColorDao.getCount();
        }
        params.put("colorTag", colorTag);
        return envoyTrafficColorDao.getCountByFields(params);
    }

    @Override
    public TrafficMarkInfo addTrafficColorInfo(TrafficMarkDto envoyTrafficColorDto) {
        TrafficMarkInfo envoyTrafficColorInfo = TrafficMarkDto.toMeta(envoyTrafficColorDto);
        envoyTrafficColorInfo.setEnableStatus(Const.PLUGIN_STATE_DISABLE);
        envoyTrafficColorInfo.setCreateTime(System.currentTimeMillis());
        envoyTrafficColorInfo.setUpdateTime(System.currentTimeMillis());
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
            return CommonErrorCode.TrafficColorRuleNameIsEmpty;
        }
        if (!trafficMatchCheck(envoyTrafficColorDto.getTrafficMatch())) {
            logger.info("流量匹配不支持 当前仅支持Header、Parameter匹配");
            return CommonErrorCode.TrafficMatchNotSupport;
        }
        //判断该流量规则名称是否已经存在
        if (isTrafficColorNameExists(trafficColorName)) {
            logger.info("创建流量染色规则，该流量染色规则名称 trafficColorName {} 已经存在", trafficColorName);
            return CommonErrorCode.TrafficColorRuleNameAlreadyExist;
        }
        //判断该服务 路由下是否已有流量染色规则 如果有则返回错误码
        if (isTrafficColorExists(envoyTrafficColorDto.getServiceName(), envoyTrafficColorDto.getRouteRuleNames())) {
            logger.info("创建流量染色规则，当前服务路由下已存在流量染色规则，不允许重复创建");
            return CommonErrorCode.TrafficColorRuleAlreadyExist;
        }
        return CommonErrorCode.Success;
    }


    @Override
    public boolean isTrafficColorNameExists(String trafficColorName) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("trafficColorName", trafficColorName);
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
    public boolean isTrafficColorExists(String serviceName, String routeRuleName) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("serviceName", serviceName);
        params.put("routeRuleNames", routeRuleName);

        return envoyTrafficColorDao.getCountByFields(params) > 0;
    }

    @Override
    public ErrorCode checkUpdateTrafficColorParam(TrafficMarkDto envoyTrafficColorDto) {
        //参数校验
        if (!isTrafficColorExists(envoyTrafficColorDto.getId())) {
            logger.info("更新流量染色规则，流量染色规则不存在");
            return CommonErrorCode.NoSuchTrafficColorRule;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean isTrafficColorExists(long id) {
        Map<String, Object> params = new HashMap<>();
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
        if (envoyTrafficColorInfo.getEnableStatus() == Const.PLUGIN_STATE_DISABLE) {
            return true;
        }

        // 染色规则启用状态下，更新插件动态生效；按照新规则(paramRule)启用流量染色规则插件
        ErrorCode enableResult = enableTrafficMarkPlugin(trafficMarkRuleId);
        if (!enableResult.equals(CommonErrorCode.Success)) {
            logger.error("[traffic mark] enable traffic_mark_plugin failed when updateTrafficColorInfo." + "trafficMarkRuleId: {}, new param_rules: {}", trafficMarkRuleId, paramRule);
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
        return trafficMarkStatus.equals(Const.PLUGIN_STATE_DISABLE) || trafficMarkStatus.equals(Const.PLUGIN_STATE_ENABLE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ErrorCode modifyTrafficMarkRuleStatus(Long trafficMarkRuleId, Integer trafficMarkStatus) {
        TrafficMarkInfo trafficColorRule = getTrafficColorRuleById(trafficMarkRuleId);

        // 根据trafficMarkStatus进行traffic_mark插件的更新或删除
        ErrorCode resultCode;
        if (trafficMarkStatus.equals(Const.PLUGIN_STATE_ENABLE)) {
            resultCode = enableTrafficMarkPlugin(trafficMarkRuleId);
        } else {
            resultCode = disableTrafficMarkPlugin(trafficMarkRuleId);
        }
        if (!resultCode.equals(CommonErrorCode.Success)) {
            return resultCode;
        }

        // 更新流量染色规则DB
        trafficColorRule.setEnableStatus(trafficMarkStatus);
        if (!updateTrafficColorInfo(trafficColorRule)) {
            return CommonErrorCode.UpdateFailure;
        }
        return CommonErrorCode.Success;
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
        plugin.put("kind", Const.PLUGIN_TYPE_TRAFFIC_MARK);
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
            Map<String, String> matcher = new HashMap<>();
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
            Map<String, String> matcher = new HashMap<>();
            matcher.put("parameterKey", trafficColorMatch.getParaName());
            matcher.put("match_type", convertToEnvoyMatchType(trafficColorMatch.getType()));
            matcher.put("value", trafficColorMatch.getValue());
            matchList.add(matcher);
        }

        request.put("parameters", matchList);
        return request;
    }

    private String getTrafficMarkKey(TrafficMarkInfo trafficColorRule) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(trafficColorRule.getGwId());
        List<PluginOrderItemDto> envoyPluginManager = envoyGatewayService.getEnvoyPluginManager(gatewayInfo.getApiPlaneAddr(), gatewayInfo.getGwClusterName());
        Optional<PluginOrderItemDto> optional = envoyPluginManager.stream().filter(e -> e.getName().equals(Const.PLUGIN_NAME_TRAFFIC_MARK)).findFirst();
        Assert.isTrue(optional.isPresent(), "PluginManager不存在插件com.netease.traffic_mark的配置，请排查");
        logger.info("getTrafficMarkKey info :{}", JSON.toJSONString(optional.get()));
        JSONObject inline = (JSONObject) optional.get().getInline();
        Assert.notNull(inline,"PluginManager插件proxy.filters.http.traffic_mark的配置信息有问题，请排查");
        JSONObject settings = inline.getJSONObject("settings");
        Assert.notNull(settings,"PluginManager插件proxy.filters.http.traffic_mark的配置信息有问题，请排查");
        return settings.getString("header_key");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ErrorCode enableTrafficMarkPlugin(Long trafficMarkRuleId) {
        logger.info("[traffic mark] start to enable traffic_mark_plugin, trafficMarkRuleId: {}", trafficMarkRuleId);
        TrafficMarkInfo trafficColorRule = getTrafficColorRuleById(trafficMarkRuleId);
        for (String routeId : trafficColorRule.getRouteRuleIds().split(",")) {
            RouteRuleInfo route = routeRuleInfoService.getRouteRuleInfoById(Long.parseLong(routeId));

            // 未发布的路由不允许启用流量染色插件
            if (route.getPublishStatus() != Const.PLUGIN_STATE_ENABLE) {
                logger.warn("[traffic mark] route not publish, please publish route firstly," + "route_id: {}, trafficMarkRuleId: {}", routeId, trafficMarkRuleId);
                return CommonErrorCode.RouteRuleNotPublished;
            }

            // 组装插件
            String trafficMarkPlugin = assembleTrafficMarkPlugin(trafficColorRule);
            logger.info("[traffic mark] assembleTrafficMarkPlugin finished, traffic_mark_plugin: {}", trafficMarkPlugin);

            List<EnvoyPluginBindingInfo> pluginBindingList = pluginInfoService.getPluginBindingList(trafficColorRule.getGwId(), routeId, EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);

            boolean isAddTrafficMarkPlugin = true;
            for (EnvoyPluginBindingInfo pluginBinding : pluginBindingList) {
                // 已存在"traffic-mark"插件，进行插件配置修改
                if (pluginBinding.getPluginType().equals(Const.PLUGIN_TYPE_TRAFFIC_MARK)) {
                    logger.info("[traffic mark] traffic_mark_plugin already exists, start to update, plugin_id: {}", pluginBinding.getId());
                    isAddTrafficMarkPlugin = false;
                    boolean updateOk = pluginInfoService.updatePluginConfiguration(pluginBinding.getId(), trafficMarkPlugin, -1);
                    if (!updateOk) {
                        logger.error("[traffic mark] update traffic_mark_plugin failed, plugin_id: {}", pluginBinding.getId());
                        return CommonErrorCode.UpdatePluginInfoFailed;
                    }
                    break;
                }
            }
            // 新增"traffic-mark"插件
            if (isAddTrafficMarkPlugin) {
                BindingPluginInfo bindingPluginInfo = new BindingPluginInfo();
                bindingPluginInfo.setGwId(trafficColorRule.getGwId());
                bindingPluginInfo.setBindingObjectType(BindingPluginInfo.PLUGIN_TYPE_ROUTE);
                bindingPluginInfo.setBindingObjectId(route.getId());
                bindingPluginInfo.setPluginType(Const.PLUGIN_TYPE_TRAFFIC_MARK);
                bindingPluginInfo.setPluginConfiguration(trafficMarkPlugin);
                boolean addOk = pluginInfoService.bindingPlugin(bindingPluginInfo, -1, -1);
                if (!addOk) {
                    logger.error("[traffic mark] add new traffic_mark_plugin failed");
                    return CommonErrorCode.UpdatePluginInfoFailed;
                }
            }
        }
        return CommonErrorCode.Success;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ErrorCode disableTrafficMarkPlugin(Long trafficMarkRuleId) {
        logger.info("[traffic mark] start to disable traffic_mark_plugin, trafficMarkRuleId: {}", trafficMarkRuleId);
        TrafficMarkInfo trafficColorRule = getTrafficColorRuleById(trafficMarkRuleId);
        for (String routeId : trafficColorRule.getRouteRuleIds().split(",")) {
            // 获取路由ID
            RouteRuleInfo route = routeRuleInfoService.getRouteRuleInfoById(Long.parseLong(routeId));

            List<EnvoyPluginBindingInfo> pluginBindingList = pluginInfoService.getPluginBindingList(trafficColorRule.getGwId(), routeId, EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);

            boolean isExists = false;
            for (EnvoyPluginBindingInfo pluginBinding : pluginBindingList) {
                if (pluginBinding.getPluginType().equals(Const.PLUGIN_TYPE_TRAFFIC_MARK)) {
                    isExists = true;
                    boolean deleteOk = pluginInfoService.unbindingPlugin(pluginBinding.getId());
                    if (!deleteOk) {
                        logger.error("[traffic mark] delete traffic_mark_plugin failed, plugin_id: {}", pluginBinding.getId());
                        return CommonErrorCode.DeletePluginInfoFailed;
                    }
                    break;
                }
            }
            if (!isExists) {
                logger.warn("[traffic mark] traffic_mark_plugin not exists on this route, route_id: {}, route_name: {}", routeId, route.getRouteRuleName());
            }
        }
        return CommonErrorCode.Success;
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
            default:
        }
        return envoyMatchType;
    }
}