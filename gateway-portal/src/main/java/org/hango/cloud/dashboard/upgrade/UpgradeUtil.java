package org.hango.cloud.dashboard.upgrade;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.meta.BindingPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.GatewayPlugin;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.service.impl.EnvoyPluginServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 升级兼容方法（实现ApplicationContextAware，用于获取Spring Bean）
 *
 * @author yutao04
 * @date 2022/3/18 17:25
 */
@Component
public class UpgradeUtil implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(UpgradeUtil.class);

    private static final String API_PUBLISH_PLUGIN = "PublishPlugin";
    private static final String API_DELETE_PLUGIN = "DeletePlugin";
    private static final String API_PUBLISH_GLOBAL_PLUGIN = "PublishGlobalPlugin";
    private static final String API_DELETE_GLOBAL_PLUGIN = "DeleteGlobalPlugin";
    private static final String API_PUBLISH_API = "PublishAPI";
    private static final String VERSION_2019_07_25 = "2019-07-25";

    private static ApplicationContext applicationContext;

    /**
     * 路由插件场景屏蔽调用1.2版本"PublishPlugin"和"DeletePlugin"接口
     * （补充的兼容接口在"opsRoutePluginForOldApiPlane"方法中调用）
     *
     * @param params map类型请求参数
     * @param body   post类型接口body
     * @return 是否需忽略对1.2版本api-plane路由插件接口调用
     */
    public static boolean ignoreCallPluginInterfaceToOldApiPlane(Map<String, String> params, String body) {
        if (params == null || StringUtils.isEmpty(body)) {
            return false;
        }
        String action = params.get("Action");
        String version = params.get("Version");
        if (StringUtils.isEmpty(action) || StringUtils.isEmpty(version)) {
            return false;
        }
        try {
            // 新版本发布路由插件，旧版本不应调用"Action=PublishPlugin&Version=2019-07-25"
            // 新版删除布路由插件，旧版本不应调用"Action=DeletePlugin&Version=2019-07-25"
            if (version.equals(VERSION_2019_07_25)) {
                if (action.equals(API_PUBLISH_PLUGIN) || action.equals(API_DELETE_PLUGIN)) {
                    GatewayPlugin gatewayPlugin = JSON.parseObject(body, GatewayPlugin.class);
                    // 1.2版本api-plane "PublishPlugin"接口包括全局和路由插件，此处需要区分除路由和全局插件
                    if (gatewayPlugin.isRoutePlugin()) {
                        logger.info("[upgrade] action: {}, publish route_plugin --- not call GlobalPlugin interface for 1.2 version api-plane", action);
                        return true;
                    } else {
                        GatewayPlugin gp = JSON.parseObject(body, GatewayPlugin.class);
                        List<String> plugins = gp.getPlugins();
                        // 全局插件每次仅下发一款插件，若该插件为新版本添加的插件（1.2版本api-plane不支持），则不下发（不调用接口）
                        for (String plugin : plugins) {
                            if (isNewVersionPlugin(plugin)) {
                                logger.info("[upgrade] action: {}, new version global_plugin, no need to 1.2 api-plane", action);
                                return true;
                            }
                        }
                        // 1.12版本api-plane全局插件接口由PublishGlobalPlugin改为PublishPlugin，旧版本需要兼容
                        if (action.equals(API_PUBLISH_PLUGIN)) {
                            logger.info("[upgrade] action: {}, publish global_plugin to 1.2 api-plane -- update new action: {}", action, API_PUBLISH_GLOBAL_PLUGIN);
                            params.put("Action", API_PUBLISH_GLOBAL_PLUGIN);
                        } else {
                            logger.info("[upgrade] action: {}, delete global_plugin to 1.2 api-plane -- update new action: {}", action, API_DELETE_GLOBAL_PLUGIN);
                            params.put("Action", API_DELETE_GLOBAL_PLUGIN);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("[upgrade] action: {}, parse gatewayPlugin failed, e: {}", action, e);
            return true;
        }
        return false;
    }

    /**
     * 升级兼容接口，对旧版本api-plane进行路由插件管理
     *
     * @param bindingPluginInfo 插件绑定信息对象
     * @param operation         本次对插件进行的操作（增、删、改）
     * @param pluginIdList      （插件ID列表 // 仅在增加和删除操作存在）
     * @return 是否操作成功
     */
    public static boolean opsRoutePluginForOldApiPlane(BindingPluginInfo bindingPluginInfo,
                                                       EnvoyPluginServiceImpl.Operation operation,
                                                       List<Long> pluginIdList) {
        ApiServerConfig config = UpgradeUtil.getBean(ApiServerConfig.class);
        // 是否开启升级场景兼容性接口调用
        if (StringUtils.isEmpty(config.getBakApiPlaneAddr()) || bindingPluginInfo.isGlobalPlugin()) {
            return true;
        }

        logger.info("[upgrade] opsRoutePluginForOldApiPlane start, bindingPluginInfo: {}!, operation: {}",
                bindingPluginInfo, operation);

        String pluginConfiguration = bindingPluginInfo.getPluginConfiguration();
        boolean res = true;
        if (operation.equals(EnvoyPluginServiceImpl.Operation.CREATE)) {
            // 新增路由插件
            res = bindingRoutePluginToOldApiPlane(bindingPluginInfo);
        } else if (operation.equals(EnvoyPluginServiceImpl.Operation.UPDATE)) {
            // 修改路由插件
            if (CollectionUtils.isEmpty(pluginIdList)) {
                return false;
            } else {
                res = updateRoutePluginToOldApiPlane(pluginIdList.get(0), pluginConfiguration);
            }
        } else {
            // 删除路由插件
            if (CollectionUtils.isEmpty(pluginIdList)) {
                return false;
            } else {
                for (Long pluginId : pluginIdList) {
                    res = res && deleteRoutePluginToOldApiPlane(pluginId);
                }
            }
        }
        return res;
    }

    /**
     * 绑定旧版本api-plane路由插件
     *
     * @param bindingPluginInfo 插件绑定对象
     * @return 路由插件绑定是否成功
     */
    private static boolean bindingRoutePluginToOldApiPlane(BindingPluginInfo bindingPluginInfo) {
        Long gwId = bindingPluginInfo.getGwId();
        String bindingObjectType = bindingPluginInfo.getBindingObjectType();
        Long bindingObjectId = bindingPluginInfo.getBindingObjectId();
        String pluginConfiguration = bindingPluginInfo.getPluginConfiguration();

        IEnvoyPluginInfoService pluginInfoService = UpgradeUtil.getBean(IEnvoyPluginInfoService.class);
        IRouteRuleProxyService routeRuleProxyService = UpgradeUtil.getBean(IRouteRuleProxyService.class);

        List<EnvoyPluginBindingInfo> alreadyEnablePlugins =
                pluginInfoService.getEnablePluginBindingList(gwId, String.valueOf(bindingObjectId), bindingObjectType);
        List<String> newPluginConfigurations = alreadyEnablePlugins.stream()
                .map(EnvoyPluginBindingInfo::getPluginConfiguration)
                .collect(Collectors.toList());
        newPluginConfigurations.add(pluginConfiguration);
        if (EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE.equals(bindingObjectType)) {
            logger.info("[upgrade] bindingRoutePluginToOldApiPlane start, gwId: {}, routeId: {}, new pluginConfiguration: {}",
                    gwId, bindingObjectId, pluginConfiguration);
            RouteRuleProxyInfo routeRuleProxyInfo = routeRuleProxyService.getRouteRuleProxy(gwId, bindingObjectId);
            long publishRouteRuleId = routeRuleProxyService.publishRouteRule(routeRuleProxyInfo, newPluginConfigurations, false);
            if (publishRouteRuleId != Const.ERROR_RESULT) {
                return true;
            } else {
                logger.error("[upgrade] bindingRoutePluginToOldApiPlane failed! publishRouteRuleId: {}", publishRouteRuleId);
                return false;
            }
        } else {
            logger.warn("[upgrade] bindingRoutePluginToOldApiPlane failed, bindingObjectType:{} not route_plugin", bindingObjectType);
            // 项目级别插件不处理
            return true;
        }
    }

    /**
     * 更新旧版本api-plane路由插件
     *
     * @param pluginBindingInfoId 待更新的插件ID
     * @param pluginConfiguration 更新的插件配置
     * @return 更新路由插件是否成功
     */
    private static boolean updateRoutePluginToOldApiPlane(long pluginBindingInfoId, String pluginConfiguration) {
        IEnvoyPluginInfoService pluginInfoService = UpgradeUtil.getBean(IEnvoyPluginInfoService.class);
        IRouteRuleProxyService routeRuleProxyService = UpgradeUtil.getBean(IRouteRuleProxyService.class);

        EnvoyPluginBindingInfo bindingInfo = pluginInfoService.getPluginBindingInfo(pluginBindingInfoId);
        if (bindingInfo == null) {
            // 插件关系不存在不处理
            return true;
        }
        if (EnvoyPluginBindingInfo.BINDING_STATUS_DISABLE.equals(bindingInfo.getBindingStatus())) {
            // 禁用状态不调用api-plane
            return true;
        }
        List<EnvoyPluginBindingInfo> alreadyBindingPlugins =
                pluginInfoService.getEnablePluginBindingList(bindingInfo.getGwId(),
                        bindingInfo.getBindingObjectId(),
                        bindingInfo.getBindingObjectType());
        List<String> newPluginConfigurations = alreadyBindingPlugins.stream()
                .map(bindingInfoItem -> {
                    if (pluginBindingInfoId != bindingInfoItem.getId()) {
                        return bindingInfoItem.getPluginConfiguration();
                    }
                    return pluginConfiguration;
                })
                .collect(Collectors.toList());

        if (EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE.equals(bindingInfo.getBindingObjectType())) {
            logger.info("[upgrade] updateRoutePluginToOldApiPlane start, gwId: {}, routeId: {}, new pluginConfiguration: {}",
                    bindingInfo.getGwId(), bindingInfo.getBindingObjectId(), pluginConfiguration);
            RouteRuleProxyInfo routeRuleProxyInfo =
                    routeRuleProxyService.getRouteRuleProxy(bindingInfo.getGwId(), Integer.parseInt(bindingInfo.getBindingObjectId()));
            long publishRouteRuleId = routeRuleProxyService.publishRouteRule(routeRuleProxyInfo, newPluginConfigurations, false);
            if (publishRouteRuleId != Const.ERROR_RESULT) {
                return true;
            } else {
                logger.error("[upgrade] updateRoutePluginToOldApiPlane failed! publishRouteRuleId: {}", publishRouteRuleId);
                return false;
            }
        } else {
            logger.warn("[upgrade] updateRoutePluginToOldApiPlane failed, bindingObjectType:{} not route_plugin",
                    bindingInfo.getBindingObjectType());
            // 项目级别插件不处理
            return true;
        }
    }

    /**
     * 删除旧版本api-plane的路由插件
     *
     * @param pluginBindingInfoId 待删除的插件ID
     * @return 删除路由插件是否成功
     */
    private static boolean deleteRoutePluginToOldApiPlane(long pluginBindingInfoId) {
        IEnvoyPluginInfoService pluginInfoService = UpgradeUtil.getBean(IEnvoyPluginInfoService.class);
        IRouteRuleProxyService envoyRouteRuleProxyService = UpgradeUtil.getBean(IRouteRuleProxyService.class);
        EnvoyPluginBindingInfo bindingInfo = pluginInfoService.getPluginBindingInfo(pluginBindingInfoId);
        if (bindingInfo == null) {
            // 插件关系不存在不处理
            return true;
        }

        List<EnvoyPluginBindingInfo> alreadyEnablePlugins =
                pluginInfoService.getEnablePluginBindingList(bindingInfo.getGwId(),
                        bindingInfo.getBindingObjectId(),
                        bindingInfo.getBindingObjectType());
        List<String> newPluginConfigurations = alreadyEnablePlugins.stream()
                .filter(pluginBindingInfo -> pluginBindingInfo.getId() != bindingInfo.getId())
                .map(EnvoyPluginBindingInfo::getPluginConfiguration)
                .collect(Collectors.toList());

        if (EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE.equals(bindingInfo.getBindingObjectType())) {
            logger.info("[upgrade] deleteRoutePluginToOldApiPlane start, gwId: {}, routeId: {}, delete plugin ID: {}",
                    bindingInfo.getGwId(), bindingInfo.getBindingObjectId(), pluginBindingInfoId);
            RouteRuleProxyInfo routeRuleProxyInfo =
                    envoyRouteRuleProxyService.getRouteRuleProxy(bindingInfo.getGwId(), Integer.parseInt(bindingInfo.getBindingObjectId()));
            long publishRouteRuleId = envoyRouteRuleProxyService.publishRouteRule(routeRuleProxyInfo, newPluginConfigurations, false);
            if (publishRouteRuleId != Const.ERROR_RESULT) {
                return true;
            } else {
                logger.error("[upgrade] deleteRoutePluginToOldApiPlane failed! publishRouteRuleId: {}", publishRouteRuleId);
                return false;
            }
        } else {
            logger.warn("[upgrade] deleteRoutePluginToOldApiPlane failed, bindingObjectType:{} not route_plugin",
                    bindingInfo.getBindingObjectType());
            // 项目级别插件不处理
            return true;
        }
    }

    /**
     * 升级双发场景下，用户配置了新插件，但旧版本api-plane不支持，删除该插件的下发，前台响应成功
     * 此场景下新api-plane的插件可以设置成功，旧版本由于不支持因此不会设置
     *
     * @param params 请求参数
     * @param body   请求body
     */
    public static String dealMissingPluginInOldApiPlane(Map<String, String> params, final String body) {
        if (params == null || StringUtils.isEmpty(body)) {
            return null;
        }
        String action = params.get("Action");
        String version = params.get("Version");
        if (StringUtils.isEmpty(action) || StringUtils.isEmpty(version)) {
            return null;
        }
        // PublishApi或PublishPlugin接口进一步判断信息
        if (version.equals(VERSION_2019_07_25)) {
            try {
                if (action.equals(API_PUBLISH_API)) {
                    JSONObject jsonObject = JSON.parseObject(body);
                    if (!jsonObject.containsKey("Plugins")) {
                        return null;
                    }
                    JSONArray plugins = jsonObject.getJSONArray("Plugins");
                    // 删除新版本增加的插件
                    Iterator<Object> iterator = plugins.iterator();
                    while (iterator.hasNext()) {
                        String pluginConfig = (String) iterator.next();
                        if (isNewVersionPlugin(pluginConfig)) {
                            iterator.remove();
                        }
                    }
                    jsonObject.put("Plugins", plugins);
                    return jsonObject.toJSONString();
                } else {
                    GatewayPlugin gp = JSON.parseObject(body, GatewayPlugin.class);
                    List<String> plugins = gp.getPlugins();
                    plugins.removeIf(UpgradeUtil::isNewVersionPlugin);
                    gp.setPlugins(plugins);
                    return gp.toJsonString();
                }
            } catch (Exception e) {
                logger.warn("[upgrade] dealMissingPluginInOldApiPlane parse response body failed");
            }
        }
        return null;
    }

    /**
     * 1.2版本api-plane删除全局插件要求plugins列表非空，但实际没有对插件内容做检查；
     * 但新版本根据空列表删除插件，需要为旧版本mock数据
     *
     * @param params 请求参数
     * @param body   响应体
     * @return 适配旧版本删除全局插件的body
     */
    public static String dealDeletePluginInOldApiPlane(Map<String, String> params, final String body) {
        if (params == null || StringUtils.isEmpty(body)) {
            return null;
        }
        String action = params.get("Action");
        String version = params.get("Version");
        if (StringUtils.isEmpty(action) || StringUtils.isEmpty(version)) {
            return null;
        }
        try {
            if (version.equals(VERSION_2019_07_25) && action.equals(API_DELETE_GLOBAL_PLUGIN)) {
                logger.info("[upgrade] delete global plugin to old api-plane, start to mock plugins");
                List<String> plugins = new ArrayList<>();
                // 1.2版本api-plane “DeleteGlobalPlugin”接口的plugins有非空校验，但插件内容不使用，仅关心GatewayPlugin资源类型，此处进行插件mock
                // mock的插件，在删除场景不关心内容，但需要能被正常解析（插件存在、格式正确）
                plugins.add("{\"limit_percent\":\"100\",\"kind\":\"ianus-percent-limit\"}");
                GatewayPlugin gp = JSON.parseObject(body, GatewayPlugin.class);
                gp.setPlugins(plugins);
                return gp.toJsonString();
            }
        } catch (Exception e) {
            logger.warn("[upgrade] dealDeletePluginInOldApiPlane parse request body failed");
        }
        return null;
    }

    /**
     * 判断是否是新版本新增插件白名单
     *
     * @param pluginConfig 插件配置（取kind节点判断插件类型）
     * @return 是否是新版本新增插件白名单
     */
    public static boolean isNewVersionPlugin(String pluginConfig) {
        try {
            JSONObject jsonObject = JSON.parseObject(pluginConfig);
            String kind = (String) jsonObject.get("kind");
            switch (kind) {
                case "ua-restriction":
                case "referer-restriction":
                case "header-restriction":
                case "response-header-rewrite":
                case "local-cache":
                case "redis-cache":
                case "sign-auth":
                case "jwt-auth":
                case "oauth2-auth":
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.warn("[upgrade] isNewVersionPlugin parse pluginConfig failed, pluginConfig: {}", pluginConfig);
        }
        return false;
    }

    /**
     * 获取某个具体类型的Spring Bean
     *
     * @param tClass Spring Bean class
     * @param <T>    具体类型
     * @return Spring bean
     * @throws ClassCastException 无法正常转换为指定类型
     */
    public static <T> T getBean(Class<T> tClass) throws ClassCastException {
        return (T) UpgradeUtil.applicationContext.getBean(tClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        UpgradeUtil.applicationContext = applicationContext;
    }
}
