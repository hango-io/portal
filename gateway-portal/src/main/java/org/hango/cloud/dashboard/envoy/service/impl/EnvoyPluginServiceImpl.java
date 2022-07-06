package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.dto.plugindto.CopyGlobalPluginDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.audit.service.IAuditConfigService;
import org.hango.cloud.dashboard.envoy.core.constant.PluginConstant;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyPluginBindingInfoDao;
import org.hango.cloud.dashboard.envoy.dao.IRouteRuleProxyDao;
import org.hango.cloud.dashboard.envoy.meta.AuthPluginTypeEnum;
import org.hango.cloud.dashboard.envoy.meta.BindingPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginTemplateInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;
import org.hango.cloud.dashboard.envoy.meta.GatewayPlugin;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGatewayService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginTemplateService;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.service.cache.PluginCacheService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginBindingDto;
import org.hango.cloud.dashboard.envoy.web.util.HttpCommonUtil;
import org.hango.cloud.dashboard.upgrade.UpgradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Envoy插件Service层实现类
 *
 * @author hzchenzhongyang 2019-10-23
 */
@Service
public class EnvoyPluginServiceImpl implements IEnvoyPluginInfoService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyPluginServiceImpl.class);

    @Autowired
    private IRouteRuleProxyDao routeRuleProxyDao;

    @Autowired
    private IGetFromApiPlaneService getFromApiPlaneService;

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IRouteRuleInfoService envoyRouteRuleInfoService;

    @Autowired
    private IEnvoyPluginBindingInfoDao envoyPluginBindingInfoDao;

    @Autowired
    private IRouteRuleProxyService envoyRouteRuleProxyService;

    @Autowired
    private IEnvoyGatewayService envoyGatewayService;

    @Autowired
    private IAuditConfigService auditConfigService;

    @Autowired
    private IEnvoyPluginTemplateService envoyPluginTemplateService;

    @Autowired
    private ApiServerConfig apiServerConfig;

    @Autowired
    private PluginCacheService pluginCacheService;

    @Override
    public ErrorCode checkDescribePlugin(long gwId) {
        if (0 < gwId) {
            GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
            if (null == gatewayInfo) {
                logger.error("插件流程查询指定的网关不存在! gwId:{}", gwId);
                return CommonErrorCode.NoSuchGateway;
            }
        }
        // 获取插件getPluginInfo可以不传gwId，默认值为0，此处返回成功（后面会处理所有网关的场景）
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkCopyGlobalPluginToGateway(CopyGlobalPluginDto copyGlobalPluginDto) {
        // 检查目标网关是否存在
        ErrorCode gwErrorCode = checkDescribePlugin(copyGlobalPluginDto.getGwId());
        if (!gwErrorCode.getCode().equals(CommonErrorCode.Success.getCode())) {
            return gwErrorCode;
        }
        // 检查插件是否存在
        EnvoyPluginBindingInfo bindingInfo = getPluginBindingInfo(copyGlobalPluginDto.getPluginId());
        if (bindingInfo == null) {
            logger.error("校验拷贝全局插件流程，插件不存在! pluginId:{}", copyGlobalPluginDto.getPluginId());
            return CommonErrorCode.NoSuchPluginBinding;
        }
        if (!bindingInfo.getBindingObjectType().equals(EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL)) {
            logger.error("校验拷贝全局插件流程，指定插件不是全局类型插件! pluginId:{}", copyGlobalPluginDto.getPluginId());
            return CommonErrorCode.IllegalPluginType;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public List<EnvoyPluginInfo> getPluginInfoListFromApiPlane(long gwId) {
        if (0 < gwId) {
            GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
            if (null == gatewayInfo) {
                logger.error("gwId对应的网关信息不存在! gwId:{}", gwId);
                return Lists.newArrayList();
            }
            return getEnvoyPluginInfos(gatewayInfo);
        }
        // 如果不传入网关id，将所有网关的插件列表合并
        List<GatewayInfo> gatewayInfos = gatewayInfoService.findAll();
        if (CollectionUtils.isEmpty(gatewayInfos)) {
            return Lists.newArrayList();
        }
        Set<EnvoyPluginInfo> envoyPluginInfoSet = Sets.newHashSet();
        for (GatewayInfo gatewayInfo : gatewayInfos) {
            if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType())) {
                envoyPluginInfoSet.addAll(getEnvoyPluginInfos(gatewayInfo));
            }
        }
        return Lists.newArrayList(envoyPluginInfoSet);
    }

    private List<EnvoyPluginInfo> getEnvoyPluginInfos(GatewayInfo gatewayInfo) {
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "GetPluginList");
        params.put("Version", "2019-07-25");

        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/plugin", params, null, null, HttpMethod.GET.name());
        if (null == response) {
            logger.error("调用api-plane查询插件列表接口响应为空!");
            return Lists.newArrayList();
        }

        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane查询插件列表接口失败，返回http status code非2xx，httpStatusCoed:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return Lists.newArrayList();
        }

        JSONObject result = JSONObject.parseObject(response.getResponseBody());
        return result.getJSONArray("Plugins").stream().map(item -> {
            JSONObject pluginInfo = JSONObject.parseObject(item.toString());
            return getEnvoyPluginInfoFromJsonObject(pluginInfo, null);
        }).collect(Collectors.toList());
    }

    @Override
    public EnvoyPluginInfo getPluginInfoFromApiPlane(long gwId, String pluginType) {
        if (0 < gwId) {
            GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
            if (null == gatewayInfo) {
                logger.error("gwId对应的网关信息不存在! gwId:{}", gwId);
                return null;
            }
            return getEnvoyPluginInfo(gatewayInfo, pluginType);
        }
        // 如果不传网关id，只要任意一个网关查询到即可
        List<GatewayInfo> gatewayInfos = gatewayInfoService.findAll();
        for (GatewayInfo gatewayInfo : gatewayInfos) {
            if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType())) {
                EnvoyPluginInfo envoyPluginInfo = getEnvoyPluginInfo(gatewayInfo, pluginType);
                if (null != envoyPluginInfo) {
                    return envoyPluginInfo;
                }
            }
        }
        return null;
    }

    private EnvoyPluginInfo getEnvoyPluginInfoFromJsonObject(JSONObject pluginInfo, String schema) {
        EnvoyPluginInfo envoyPluginInfo = new EnvoyPluginInfo();
        envoyPluginInfo.setPluginName(pluginInfo.getString("displayName"));
        if (StringUtils.isBlank(pluginInfo.getString("displayName"))) {
            envoyPluginInfo.setPluginName(pluginInfo.getString("name"));
        }
        envoyPluginInfo.setAuthor(pluginInfo.getString("author"));
        //对应插件唯一标志，英文标识
        envoyPluginInfo.setPluginType(pluginInfo.getString("name"));
        envoyPluginInfo.setPluginScope(pluginInfo.getString("pluginScope"));
        envoyPluginInfo.setCreateTime(getLongValueFromJsonWithDefault(pluginInfo, "createTime", 0));
        envoyPluginInfo.setUpdateTime(getLongValueFromJsonWithDefault(pluginInfo, "updateTime", 0));
        envoyPluginInfo.setPluginPriority(getLongValueFromJsonWithDefault(pluginInfo, "pluginPriority", 0));
        envoyPluginInfo.setInstructionForUse(pluginInfo.getString("instructionForUse"));
        envoyPluginInfo.setCategoryKey(pluginInfo.getString("categoryKey"));
        envoyPluginInfo.setCategoryName(pluginInfo.getString("categoryName"));
        envoyPluginInfo.setPluginHandler(pluginInfo.getString("processor"));
        if (schema != null) {
            envoyPluginInfo.setPluginSchema(schema);
        }
        return envoyPluginInfo;
    }

    private EnvoyPluginInfo getEnvoyPluginInfo(GatewayInfo gatewayInfo, String pluginType) {
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "GetPluginDetail");
        params.put("Version", "2019-07-25");
        params.put("Name", pluginType);

        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/plugin", params, null, null, HttpMethod.GET.name());
        if (null == response) {
            logger.error("调用api-plane查询插件详情接口响应为空!");
            return null;
        }

        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane查询插件详情接口失败，返回http status code非2xx，httpStatusCoed:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return null;
        }

        JSONObject result = JSONObject.parseObject(response.getResponseBody());
        return getEnvoyPluginInfoFromJsonObject(result.getJSONObject("Plugin"), result.getString("Schema"));
    }

    private long getLongValueFromJsonWithDefault(JSONObject jsonObject, String key, long defaultValue) {
        return jsonObject.getLong(key) == null ? defaultValue : jsonObject.getLong(key);
    }

    @Override
    public ErrorCode checkBindingPlugin(BindingPluginInfo bindingPluginInfo, long projectId, long templateId) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(bindingPluginInfo.getGwId());
        if (null == gatewayInfo) {
            logger.info("绑定插件时指定的网关id不存在！ gwId：{}", bindingPluginInfo.getGwId());
            return CommonErrorCode.NoSuchGateway;
        }
        if (EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE.equals(bindingPluginInfo.getBindingObjectType())) {
            RouteRuleProxyInfo routeRuleProxyInfo =
                    envoyRouteRuleProxyService.getRouteRuleProxy(bindingPluginInfo.getGwId(), bindingPluginInfo.getBindingObjectId());
            if (null == routeRuleProxyInfo) {
                logger.info("路由规则尚未发布到指定网关，不允许绑定插件! gwId:{}, routeRuleId:{}",
                        bindingPluginInfo.getGwId(),
                        bindingPluginInfo.getBindingObjectId());
                return CommonErrorCode.RouteRuleNotPublished;
            }
        } else if (EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL.equals(bindingPluginInfo.getBindingObjectType())) {
            bindingPluginInfo.setBindingObjectId(projectId);
            // TODO 若后续改为项目级，则需要增加校验网关与项目的关联关系
            EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService.getVirtualHostByGwIdAndProjectId(
                    bindingPluginInfo.getGwId(),
                    bindingPluginInfo.getBindingObjectId());
            if (null == virtualHostInfo) {
                logger.info("绑定全局插件时指定的virtual host不存在! gwId:{}, projectId:{}",
                        bindingPluginInfo.getGwId(),
                        bindingPluginInfo.getBindingObjectId());
                return CommonErrorCode.ProjectNotAssociatedGateway;
            }
        } else {
            ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(
                    bindingPluginInfo.getGwId(),
                    bindingPluginInfo.getBindingObjectId());
            if (null == serviceProxyInfo) {
                logger.info("服务尚未发布到指定网关，不允许绑定插件！ gwId:{}, serviceId:{}",
                        bindingPluginInfo.getGwId(),
                        bindingPluginInfo.getBindingObjectId());
            }
        }

        if (StringUtils.isBlank(bindingPluginInfo.getPluginType())) {
            logger.info("绑定插件时参数 pluginType 为空!");
            return CommonErrorCode.MissingParameter("PluginType");
        }

        EnvoyPluginBindingInfo bindingInfo = getBindingInfo(bindingPluginInfo);
        if (null != bindingInfo) {
            logger.info("已绑定该插件，不允许重复绑定");
            return CommonErrorCode.CannotDuplicateBinding;
        }
        if (AuthPluginTypeEnum.isAuthPlugin(bindingPluginInfo.getPluginType())) {
            List<EnvoyPluginBindingInfo> bindingInfoList = getBindingInfoList(bindingPluginInfo);
            if (CollectionUtils.isNotEmpty(bindingInfoList)) {
                List<EnvoyPluginBindingInfo> authPluginList = bindingInfoList.stream().filter(
                        item -> AuthPluginTypeEnum.get(item.getPluginType()) != null).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(authPluginList)) {
                    logger.info("认证类型插件只能绑定一种，不能重复绑定");
                    return CommonErrorCode.CannotDuplicateBindingAuthPlugin;
                }
            }
        }

        if (0 < templateId) {
            EnvoyPluginTemplateInfo templateInfo = envoyPluginTemplateService.getTemplateById(templateId);
            if (null == templateInfo) {
                logger.info("指定插件模板不存在! templateId:{}", templateId);
                return CommonErrorCode.NoSuchPluginTemplate;
            }
            if (!bindingPluginInfo.getPluginType().equals(templateInfo.getPluginType())) {
                logger.info("插件模板与插件类型不匹配! pluginType:{}, templatePluginType:{}",
                        bindingPluginInfo.getPluginType(),
                        templateInfo.getPluginType());
                return CommonErrorCode.NoSuchPluginTemplate;
            }
        }
        return CommonErrorCode.Success;
    }

    private EnvoyPluginBindingInfo getBindingInfo(BindingPluginInfo bindingPluginInfo) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", bindingPluginInfo.getGwId());
        params.put("bindingObjectId", bindingPluginInfo.getBindingObjectId());
        params.put("bindingObjectType", bindingPluginInfo.getBindingObjectType());
        params.put("pluginType", bindingPluginInfo.getPluginType());
        List<EnvoyPluginBindingInfo> pluginBindingInfoList = envoyPluginBindingInfoDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(pluginBindingInfoList) ? null : pluginBindingInfoList.get(0);
    }

    private List<EnvoyPluginBindingInfo> getBindingInfoList(BindingPluginInfo bindingPluginInfo) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", bindingPluginInfo.getGwId());
        params.put("bindingObjectId", bindingPluginInfo.getBindingObjectId());
        params.put("bindingObjectType", bindingPluginInfo.getBindingObjectType());
        return envoyPluginBindingInfoDao.getRecordsByField(params);
    }

    /**
     * 插件流程的公共请求操作方法，Gportal请求Api-plane的端点函数
     *
     * @param bindingPluginInfo 插件聚合对象
     * @param operation         本次插件的操作方法（详见本文件中的Operation枚举类）
     * @param pluginIdList      插件ID集合（主要用于update和delete）
     * @return 是否请求操作成功
     */
    private boolean opsForGatewayPlugin(BindingPluginInfo bindingPluginInfo,
                                        Operation operation,
                                        List<Long> pluginIdList) {
        logger.info("{} gateway plugin operation: {}", PluginConstant.PLUGIN_LOG_NOTE, operation.getAction());

        List<String> toBePublishedPluginList = createToBePublishedPluginList(bindingPluginInfo, operation, pluginIdList);

        logger.info("bindingPluginInfo: {}", bindingPluginInfo);
        toBePublishedPluginList.forEach(plugin ->
                logger.info("{} gateway plugin info: {}", PluginConstant.PLUGIN_LOG_NOTE, plugin));

        // 非拷贝路由场景不执行该方法
        prepareForCopyingRoute(bindingPluginInfo);

        // 升级接口兼容方法调用，根据ApiServerConfig.bakApiPlaneAddr配置决定是否开启（仅在升级场景开启）
        if (!UpgradeUtil.opsRoutePluginForOldApiPlane(bindingPluginInfo, operation, pluginIdList)) {
            logger.error("[upgrade] opsRoutePluginForOldApiPlane failed!");
            return false;
        }

        // 创建核心数据对象并向Api-plane发请求
        return createPluginAndMakeRequest(bindingPluginInfo, operation, toBePublishedPluginList);
    }

    private void prepareForCopyingRoute(BindingPluginInfo bindingPluginInfo) {
        // 拷贝路由场景需要将网关ID赋值为目标网关ID
        if (bindingPluginInfo.isCopyRoute()) {
            logger.info("{} gateway plugin for copy route, origin gatewayId: {}, dest gatewayId: {}",
                    PluginConstant.PLUGIN_LOG_NOTE,
                    bindingPluginInfo.getGwId(),
                    bindingPluginInfo.getDestGatewayId());
            bindingPluginInfo.setGwId(Long.parseLong(bindingPluginInfo.getDestGatewayId()));
        }
    }

    public boolean createPluginAndMakeRequest(BindingPluginInfo bindingPluginInfo, Operation operation, List<String> toBePublishedPluginList) {
        RouteRuleProxyInfo routeRuleProxyInfo = new RouteRuleProxyInfo();
        // 路由使能状态，路由不使能则不发布（该标志不影响全局插件）
        String enableState = Const.ROUTE_RULE_ENABLE_STATE;
        GatewayPlugin gatewayPlugin;

        // 获取插件实体gatewayPlugin信息
        if (bindingPluginInfo.isRoutePlugin()) {
            routeRuleProxyInfo = envoyRouteRuleProxyService
                    .getRouteRuleProxy(bindingPluginInfo.getGwId(), bindingPluginInfo.getBindingObjectId());
            gatewayPlugin = createRoutePlugin(bindingPluginInfo, routeRuleProxyInfo, toBePublishedPluginList);

            enableState = routeRuleProxyInfo.getEnableState();
            // 路由插件发布，更新路由信息更新时间
            routeRuleProxyInfo.setUpdateTime(System.currentTimeMillis());
            routeRuleProxyDao.update(routeRuleProxyInfo);
        } else {
            EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService
                    .getVirtualHostByGwIdAndProjectId(bindingPluginInfo.getGwId(), bindingPluginInfo.getBindingObjectId());
            gatewayPlugin = createGlobalPlugin(bindingPluginInfo, virtualHostInfo, toBePublishedPluginList);
        }

        // 使能状态下进行路由插件发布，发送相关配置至api-plane，否则在控制台进行虚拟发布
        if (bindingPluginInfo.canPublishPlugin(enableState)) {
            logger.info("{} bindingID:{}, bindingID is route ID for route plugins or project ID for global" +
                            "plugins; GatewayPlugin plugins start to change",
                    PluginConstant.PLUGIN_LOG_NOTE, bindingPluginInfo.getBindingObjectId());
            return changeGatewayPluginFromApiPlane(operation, gatewayPlugin);
        }
        return true;
    }

    private boolean changeGatewayPluginFromApiPlane(Operation operation, GatewayPlugin gatewayPlugin) {
        boolean responseStatus;
        if (operation.equals(Operation.DELETE)) {
            responseStatus = deleteGatewayPluginFromApiPlane(gatewayPlugin);
        } else {
            responseStatus = publishGatewayPluginToApiPlane(gatewayPlugin);
        }
        if (!responseStatus) {
            logger.info("{} request to Api-plane failed", PluginConstant.PLUGIN_LOG_NOTE);
            return false;
        }
        return true;
    }

    /**
     * 发布网关插件
     *
     * @param bindingPluginInfo 网关插件聚合信息对象
     * @return 是否操作成功
     */
    @Override
    public boolean publishGatewayPlugin(BindingPluginInfo bindingPluginInfo) {
        logger.info("{} start to publishGatewayPlugin, gwId: [{}], bindingObjectId: [{}]",
                PluginConstant.PLUGIN_LOG_NOTE,
                bindingPluginInfo.getGwId(),
                bindingPluginInfo.getBindingObjectId());
        // 新路由插件操作不需要关注pluginId
        return opsForGatewayPlugin(bindingPluginInfo, Operation.CREATE, createPluginIdList(0L));
    }

    /**
     * 更新指定ID的网关插件
     *
     * @param bindingPluginInfo 网关插件聚合信息对象
     * @param pluginId          需要被更新的插件ID
     * @return 是否操作成功
     */
    @Override
    public boolean updateGatewayPlugin(BindingPluginInfo bindingPluginInfo, long pluginId) {
        logger.info("{} start to updateGatewayPlugin, gwId: [{}], pluginId: [{}] bindingObjectId: [{}]",
                PluginConstant.PLUGIN_LOG_NOTE,
                bindingPluginInfo.getGwId(),
                pluginId,
                bindingPluginInfo.getBindingObjectId());
        // 更新路由插件需要明确pluginId和pluginConfig
        return opsForGatewayPlugin(bindingPluginInfo, Operation.UPDATE, createPluginIdList(pluginId));
    }

    /**
     * 删除指定ID的网关插件
     *
     * @param bindingPluginInfo 网关插件聚合信息对象
     * @param pluginId          需要被删除的插件ID
     * @return 是否操作成功
     */
    @Override
    public boolean deleteGatewayPlugin(BindingPluginInfo bindingPluginInfo, long pluginId) {
        return deleteGatewayPlugin(bindingPluginInfo, createPluginIdList(pluginId));
    }

    /**
     * 删除指定ID列表的网关插件
     *
     * @param bindingPluginInfo 网关插件聚合信息对象
     * @param pluginIdList      需要被删除的插件ID集合
     * @return 是否操作成功
     */
    @Override
    public boolean deleteGatewayPlugin(BindingPluginInfo bindingPluginInfo, List<Long> pluginIdList) {
        logger.info("{} start to deleteGatewayPlugin, gwId: [{}], pluginId: [{}] bindingObjectId: [{}]",
                PluginConstant.PLUGIN_LOG_NOTE,
                bindingPluginInfo.getGwId(),
                createStringPluginIdSequence(pluginIdList),
                bindingPluginInfo.getBindingObjectId());
        // 删除路由插件操作需要明确pluginId但不需要关注pluginConfig
        return opsForGatewayPlugin(bindingPluginInfo, Operation.DELETE, pluginIdList);
    }

    @Override
    public boolean copyGlobalPluginToGatewayByGwId(CopyGlobalPluginDto copyGlobalPlugin) {
        // 拷贝全局插件的目标网关ID
        Long gwId = copyGlobalPlugin.getGwId();
        // 拷贝全局插件的源插件ID
        Long pluginId = copyGlobalPlugin.getPluginId();
        EnvoyPluginBindingInfo pluginBindingInfo = envoyPluginBindingInfoDao.get(pluginId);
        pluginBindingInfo.setGwId(gwId);
        pluginBindingInfo.setCreateTime(System.currentTimeMillis());
        pluginBindingInfo.setUpdateTime(System.currentTimeMillis());
        GatewayInfo gateway = gatewayInfoService.get(gwId);

        // 查询目标网关下相同类型的全局插件（项目级）
        BindingPluginInfo bindingPlugin = BindingPluginInfo.createBindingPluginFromEnvoyPluginBindingInfo(pluginBindingInfo);
        bindingPlugin.setBindingObjectType(BindingPluginInfo.PLUGIN_TYPE_GLOBAL);
        bindingPlugin.setGwId(gwId);
        List<EnvoyPluginBindingInfo> sameTypePlugins =
                getPluginBindingListByGwIdAndTypeAndProjectId(bindingPlugin, copyGlobalPlugin.getProjectId());

        if (CollectionUtils.isEmpty(sameTypePlugins)) {
            // 网关没有相同类型的全局插件（项目级）
            logger.info("[copyGlobalPlugin] no same plugin[pluginType:{}, scope:{}] exists under gw[gwId:{}, gwName:{}]",
                    bindingPlugin.getPluginType(), bindingPlugin.getBindingObjectType(), gwId, gateway.getGwName());
            long newGlobalPluginId = envoyPluginBindingInfoDao.add(pluginBindingInfo);
            pluginBindingInfo.setId(newGlobalPluginId);
            logger.info("[copyGlobalPlugin] new plugin[pluginId:{}, pluginType:{}, scope:{}] copy to gw[gwId:{}, gwName:{}] ok",
                    newGlobalPluginId, bindingPlugin.getPluginType(), bindingPlugin.getBindingObjectType(), gwId, gateway.getGwName());

            // 调用api-plane对GP资源做变更
            return changeForGatewayPluginCRD(pluginBindingInfo);
        } else {
            // 网关下有相同类型的全局插件（项目级）
            logger.info("[copyGlobalPlugin] plugin[pluginType:{}, scope:{}] already exists under gw[gwId:{}, gwName:{}]",
                    bindingPlugin.getPluginType(), bindingPlugin.getBindingObjectType(), gwId, gateway.getGwName());
            // 全局插件同一种类型只能有一个存在，因此取第一个元素
            EnvoyPluginBindingInfo oldPlugin = sameTypePlugins.get(0);
            pluginBindingInfo.setId(oldPlugin.getId());
            // 根据插件状态的策略设置新插件的启用状态
            prepareForBindingStatus(copyGlobalPlugin, pluginBindingInfo);
            envoyPluginBindingInfoDao.update(pluginBindingInfo);
            logger.info("[copyGlobalPlugin] plugin[pluginId:{}, pluginType:{}, scope:{}] under gw[gwId:{}, gwName:{}] update ok",
                    oldPlugin.getId(), bindingPlugin.getPluginType(), bindingPlugin.getBindingObjectType(), gwId, gateway.getGwName());

            // 根据新老插件的启用情况，调用api-plane对GP资源做变更
            return changeForGatewayPluginCRD(pluginBindingInfo, oldPlugin);
        }
    }

    /**
     * 配置插件的启用决策，根据前台策IsEnable决定
     *
     * @param copyGlobalPlugin  前台输入的拷贝插件流程对象（前台期望状态）
     * @param pluginBindingInfo 新插件配置对象（插件启用状态）
     */
    private void prepareForBindingStatus(CopyGlobalPluginDto copyGlobalPlugin,
                                         EnvoyPluginBindingInfo pluginBindingInfo) {
        // enable为空代表前端没有传值，标识按照默认策略（源插件状态）；否则设置成前台期望状态
        if (copyGlobalPlugin.getIsEnable() != null) {
            if (copyGlobalPlugin.getIsEnable()) {
                pluginBindingInfo.setBindingStatus(EnvoyPluginBindingInfo.BINDING_STATUS_ENABLE);
            } else {
                pluginBindingInfo.setBindingStatus(EnvoyPluginBindingInfo.BINDING_STATUS_DISABLE);
            }
        }
    }

    /**
     * 拷贝全局插件场景下新插件启用对GP的变更
     *
     * @param pluginBindingInfo 新插件对象
     * @return 操作是否成功
     */
    private boolean changeForGatewayPluginCRD(EnvoyPluginBindingInfo pluginBindingInfo) {
        if (pluginBindingInfo.getBindingStatus().equals(EnvoyPluginBindingInfo.BINDING_STATUS_ENABLE)) {
            // 调用api-plane创建GP
            return publishGatewayPlugin(BindingPluginInfo.createBindingPluginFromEnvoyPluginBindingInfo(pluginBindingInfo));
        }
        return true;
    }

    /**
     * 以下列出拷贝全局插件场景下存在老插件的四种情况对GP资源的操作方法
     * √代表enable(启用)；x代表disable(禁用)
     * <p>
     * 老插件  新插件
     * √     x    ---   下线GP
     * √     √    ---   更新GP
     * x     x    ---   不操作
     * x     √    ---   上下GP
     *
     * @param pluginBindingInfo 新插件对象
     * @param oldPlugin         旧插件对象
     * @return 操作是否成功
     */
    private boolean changeForGatewayPluginCRD(EnvoyPluginBindingInfo pluginBindingInfo, EnvoyPluginBindingInfo oldPlugin) {
        BindingPluginInfo bindingPluginInfo = BindingPluginInfo.createBindingPluginFromEnvoyPluginBindingInfo(pluginBindingInfo);
        boolean opsOk = true;
        if (oldPlugin.getBindingStatus().equals(pluginBindingInfo.getBindingStatus())) {
            if (pluginBindingInfo.getBindingStatus().equals(EnvoyPluginBindingInfo.BINDING_STATUS_ENABLE)) {
                // 都是启用场景，需调用api-plane更新GP配置
                opsOk = updateGatewayPlugin(bindingPluginInfo, pluginBindingInfo.getId());
            }
        } else {
            if (pluginBindingInfo.getBindingStatus().equals(EnvoyPluginBindingInfo.BINDING_STATUS_ENABLE)) {
                // 新插件启用，旧插件禁用场景，需调用api-plane创建GP
                opsOk = publishGatewayPlugin(bindingPluginInfo);
            } else {
                // 新插件禁用，旧插件启用场景，需调用api-plane下线GP
                opsOk = deleteGatewayPlugin(bindingPluginInfo, pluginBindingInfo.getId());
            }
        }
        return opsOk;
    }

    @Override
    public List<EnvoyPluginBindingInfo> getPluginBindingListByGwIdAndTypeAndProjectId(BindingPluginInfo bindingPlugin, Long projectId) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("gwId", bindingPlugin.getGwId());
        params.put("pluginType", bindingPlugin.getPluginType());
        params.put("bindingObjectType", bindingPlugin.getBindingObjectType());
        params.put("projectId", projectId);
        return envoyPluginBindingInfoDao.getRecordsByField(params);
    }

    // 根据操作类型获取全局或路由插件列表
    public List<String> createToBePublishedPluginList(BindingPluginInfo bindingPluginInfo,
                                                       Operation operation,
                                                       List<Long> pluginIdList) {
        List<String> toBePublishedPluginList;
        if (bindingPluginInfo.isRoutePlugin()) {
            logger.info("{} this is route plugin.", PluginConstant.PLUGIN_LOG_NOTE);
            List<EnvoyPluginBindingInfo> enabledPluginList = getEnablePluginBindingList(bindingPluginInfo.getGwId(),
                    String.valueOf(bindingPluginInfo.getBindingObjectId()),
                    bindingPluginInfo.getBindingObjectType());
            toBePublishedPluginList = createToBePublishedRoutePluginList(enabledPluginList,
                    operation,
                    bindingPluginInfo.getPluginConfiguration(),
                    pluginIdList);
        } else if (bindingPluginInfo.isGlobalPlugin()) {
            logger.info("{} this is global plugin.", PluginConstant.PLUGIN_LOG_NOTE);
            toBePublishedPluginList =
                    createToBePublishedGlobalPluginList(operation, bindingPluginInfo.getPluginConfiguration());
        } else {
            toBePublishedPluginList = new ArrayList<>();
            logger.error("{} illegal plugin type! not route or global.", PluginConstant.PLUGIN_LOG_NOTE);
        }
        return toBePublishedPluginList;
    }

    // 根据操作类型创建对应项目的待发布插件列表
    private List<String> createToBePublishedGlobalPluginList(Operation operation, String pluginConfig) {
        List<String> toBePublishedPluginList = new ArrayList<>();
        switch (operation) {
            case CREATE:
            case UPDATE:
                toBePublishedPluginList.add(pluginConfig);
                break;
            case DELETE:
                break;
            default:
                logger.error("{} illegal operation of global plugin!", PluginConstant.PLUGIN_LOG_NOTE);
        }
        return toBePublishedPluginList;
    }

    // 根据操作类型创建对应路由的待发布插件列表
    private List<String> createToBePublishedRoutePluginList(List<EnvoyPluginBindingInfo> enabledPluginList,
                                                            Operation operation,
                                                            String pluginConfig,
                                                            List<Long> pluginIdList) {
        List<String> toBePublishedPluginList;
        switch (operation) {
            case CREATE: {
                // 为当前的插件集合添加一个新的插件配置
                toBePublishedPluginList = enabledPluginList.stream()
                        .map(EnvoyPluginBindingInfo::getPluginConfiguration)
                        .collect(Collectors.toList());
                if (!StringUtils.isEmpty(pluginConfig)) {
                    toBePublishedPluginList.add(pluginConfig);
                }
                break;
            }
            case UPDATE: {
                // 修改对应pluginId的插件的配置
                final Long toBeUpdatePluginId = pluginIdList.get(0);
                toBePublishedPluginList = enabledPluginList.stream()
                        .map(bindingInfoItem -> {
                            if (toBeUpdatePluginId != bindingInfoItem.getId()) {
                                return bindingInfoItem.getPluginConfiguration();
                            }
                            return pluginConfig;
                        })
                        .collect(Collectors.toList());
                break;
            }
            case DELETE: {
                // 过滤去除对应pluginId的插件的配置
                enabledPluginList.forEach(plugin -> {
                    for (Long pluginId : pluginIdList) {
                        if (pluginId.equals(plugin.getId())) {
                            plugin.setId(-1L);
                        }
                    }
                });
                toBePublishedPluginList = enabledPluginList.stream()
                        .filter(plugin -> plugin.getId() != -1L)
                        .map(EnvoyPluginBindingInfo::getPluginConfiguration)
                        .collect(Collectors.toList());
                break;
            }
            default:
                toBePublishedPluginList = new ArrayList<>();
                logger.error("{} illegal operation of route plugin!", PluginConstant.PLUGIN_LOG_NOTE);
        }

        return toBePublishedPluginList;
    }

    /**
     * 路由插件创建实体
     *
     * @param bindingPluginInfo  插件绑定信息
     * @param routeRuleProxyInfo 路由实体信息
     * @param pluginList         待发布插件配置集合
     * @return 网关插件实体（在Api-plane转换为GatewayPlugin CRD）
     */
    private GatewayPlugin createRoutePlugin(BindingPluginInfo bindingPluginInfo,
                                            RouteRuleProxyInfo routeRuleProxyInfo,
                                            List<String> pluginList) {
        // 虚拟主机信息不传
        return createPlugin(bindingPluginInfo, routeRuleProxyInfo, null, pluginList);
    }

    /**
     * 全局（项目级）插件创建实体
     *
     * @param bindingPluginInfo 插件绑定信息
     * @param virtualHostInfo   虚拟主机实体信息
     * @param pluginList        待发布插件配置集合
     * @return 网关插件实体（在Api-plane转换为GatewayPlugin CRD）
     */
    private GatewayPlugin createGlobalPlugin(BindingPluginInfo bindingPluginInfo,
                                             EnvoyVirtualHostInfo virtualHostInfo,
                                             List<String> pluginList) {
        // 路由信息不传
        return createPlugin(bindingPluginInfo, null, virtualHostInfo, pluginList);
    }

    private GatewayPlugin createPlugin(BindingPluginInfo bindingPluginInfo,
                                       RouteRuleProxyInfo routeRuleProxyInfo,
                                       EnvoyVirtualHostInfo virtualHostInfo,
                                       List<String> pluginList) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(bindingPluginInfo.getGwId());

        GatewayPlugin gatewayPlugin = new GatewayPlugin();
        gatewayPlugin.setGatewayInfo(gatewayInfo);
        gatewayPlugin.setPluginType(bindingPluginInfo.getPluginType());
        gatewayPlugin.setGateway(gatewayInfo.getGwClusterName().toLowerCase());
        gatewayPlugin.setPlugins(pluginList);
        if (routeRuleProxyInfo != null) {
            gatewayPlugin.setRouteId(routeRuleProxyInfo.getRouteRuleId());
            gatewayPlugin.setHosts(JSON.parseArray(routeRuleProxyInfo.getHosts(), String.class));
        }

        if (virtualHostInfo != null) {
            gatewayPlugin.setCode(virtualHostInfo.getVirtualHostCode() + "-" + bindingPluginInfo.getPluginType());
            gatewayPlugin.setHosts(virtualHostInfo.getHostList());
        }

        return gatewayPlugin;
    }

    private boolean requestForGatewayPlugin(GatewayPlugin plugin, Map<String, String> params) {
        params.put("Version", "2019-07-25");

        Map<String, String> headers = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        headers.put("Content-type", Const.DEFAULT_CONTENT_TYPE);

        GatewayInfo gatewayInfo = plugin.getGatewayInfo();
        if (gatewayInfo == null || StringUtils.isEmpty(gatewayInfo.getApiPlaneAddr())) {
            throw new IllegalArgumentException("gatewayInfo info is missing!");
        }

        HttpClientResponse response = publishGatewayPluginToApiPlane(
                gatewayInfo.getApiPlaneAddr() + "/api/portal",
                params,
                plugin.toJsonString(),
                headers,
                HttpMethod.POST.name());
        return response != null && HttpCommonUtil.isNormalCode(response.getStatusCode());
    }

    /**
     * 向API-Plane请求发布路由插件
     *
     * @param plugin 待发布插件信息
     * @return 请求是否成功
     */
    public boolean publishGatewayPluginToApiPlane(GatewayPlugin plugin) {
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "PublishPlugin");
        return requestForGatewayPlugin(plugin, params);
    }

    /**
     * 向API-Plane请求删除路由插件
     *
     * @param plugin 待删除插件信息
     * @return 请求是否成功
     */
    public boolean deleteGatewayPluginFromApiPlane(GatewayPlugin plugin) {
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DeletePlugin");
        return requestForGatewayPlugin(plugin, params);
    }

    public HttpClientResponse publishGatewayPluginToApiPlane(String apiPlaneUrl, Map<String, String> params, String body, Map<String, String> headers, String methodType) {
        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(apiPlaneUrl, params, body, headers, methodType);
        //存在bak api server，且和apiPlaneUrl不相同
        if (StringUtils.isNotBlank(apiServerConfig.getBakApiPlaneAddr()) && !apiPlaneUrl.equals(apiServerConfig.getBakApiPlaneAddr()) && null != response && HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            // 升级场景，路由插件（"PublishPlugin"和"DeletePlugin"）接口不进行旧版本api-plane调用（不兼容）
            if (UpgradeUtil.ignoreCallPluginInterfaceToOldApiPlane(params, body)) {
                return response;
            }
            // 升级双发场景下，用户配置了新插件但1.2版本api-plane没有，删除旧版本的下发
            String newBody = UpgradeUtil.dealMissingPluginInOldApiPlane(params, body);
            if (StringUtils.isNotEmpty(newBody)) {
                body = newBody;
            }
            // 1.2版本api-plane删除全局插件要求plugins列表非空，但新版本根据空列表删除插件，需要为旧版本mock数据
            String DeletePluginBody = UpgradeUtil.dealDeletePluginInOldApiPlane(params, body);
            if (StringUtils.isNotEmpty(DeletePluginBody)) {
                body = DeletePluginBody;
            }
            response = HttpCommonUtil.getFromApiPlane(apiServerConfig.getBakApiPlaneAddr() + "/api/portal", params, body, headers, methodType);
        }
        return response;
    }

    @Override
    public boolean bindingPlugin(BindingPluginInfo bindingPluginInfo, long projectId, long templateId) {
        if (EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL.equals(bindingPluginInfo.getBindingObjectType())) {
            bindingPluginInfo.setBindingObjectId(projectId);
        }
        EnvoyPluginInfo pluginInfo =
                getPluginInfoFromApiPlane(bindingPluginInfo.getGwId(), bindingPluginInfo.getPluginType());
        if (null == pluginInfo) {
            return false;
        }
        EnvoyPluginTemplateInfo templateInfo = null;
        // 若有模板信息则插件配置来源于模板数据
        if (0 < templateId) {
            templateInfo = envoyPluginTemplateService.getTemplateById(templateId);
            if (null == templateInfo) {
                return false;
            }
            bindingPluginInfo.setPluginConfiguration(templateInfo.getPluginConfiguration());
        }

        // 发布插件信息到api-plane
        boolean bindingResult = publishGatewayPlugin(bindingPluginInfo);
        if (!bindingResult) {
            return false;
        }
        EnvoyPluginBindingInfo bindingInfo = new EnvoyPluginBindingInfo();
        bindingInfo.setProjectId(ProjectTraceHolder.getProId());
        bindingInfo.setGwId(bindingPluginInfo.getGwId());
        bindingInfo.setUpdateTime(System.currentTimeMillis());
        bindingInfo.setCreateTime(System.currentTimeMillis());
        bindingInfo.setPluginConfiguration(bindingPluginInfo.getPluginConfiguration());
        if (0 < templateId) {
            bindingInfo.setTemplateId(templateId);
            bindingInfo.setTemplateVersion(templateInfo.getTemplateVersion());
        }
        bindingInfo.setBindingObjectType(bindingPluginInfo.getBindingObjectType());
        bindingInfo.setBindingObjectId(String.valueOf(bindingPluginInfo.getBindingObjectId()));
        bindingInfo.setPluginType(bindingPluginInfo.getPluginType());
        bindingInfo.setPluginPriority(pluginInfo.getPluginPriority());
        bindingInfo.setBindingStatus(EnvoyPluginBindingInfo.BINDING_STATUS_ENABLE);
        long bindingInfoId = envoyPluginBindingInfoDao.add(bindingInfo);
        return true;
    }

    @Override
    public long bindingPluginToDb(EnvoyPluginBindingInfo bindingInfo) {
        return envoyPluginBindingInfoDao.add(bindingInfo);
    }

    @Override
    public long deletePluginFromDb(EnvoyPluginBindingInfo bindingInfo) {
        return envoyPluginBindingInfoDao.delete(bindingInfo);
    }

    @Override
    public List<EnvoyPluginBindingInfo> getEnablePluginBindingList(long gwId, String bindingObjectId, String bindingObjectType) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("bindingObjectId", bindingObjectId);
        params.put("bindingObjectType", bindingObjectType);
        params.put("bindingStatus", EnvoyPluginBindingInfo.BINDING_STATUS_ENABLE);
        List<EnvoyPluginBindingInfo> pluginBindingInfoList = envoyPluginBindingInfoDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(pluginBindingInfoList) ? Lists.newArrayList() : pluginBindingInfoList;
    }

    @Override
    public List<EnvoyPluginBindingInfo> getPluginBindingList(long gwId, String bindingObjectId, String bindingObjectType) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("bindingObjectId", bindingObjectId);
        params.put("bindingObjectType", bindingObjectType);
        List<EnvoyPluginBindingInfo> pluginBindingInfoList = envoyPluginBindingInfoDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(pluginBindingInfoList) ? Lists.newArrayList() : pluginBindingInfoList;
    }

    @Override
    public boolean unbindingPlugin(long pluginBindingInfoId) {
        EnvoyPluginBindingInfo bindingInfo = getPluginBindingInfo(pluginBindingInfoId);
        if (null == bindingInfo) {
            logger.info("解绑时指定的绑定不存在，不继续解绑, pluginBindingInfoId:{}", pluginBindingInfoId);
            return true;
        }
        BindingPluginInfo bindingPluginInfo = BindingPluginInfo.createBindingPluginFromEnvoyPluginBindingInfo(bindingInfo);
        if (!deleteGatewayPlugin(bindingPluginInfo, pluginBindingInfoId)) {
            return false;
        }

        envoyPluginBindingInfoDao.delete(bindingInfo);
        return true;
    }

    @Override
    public EnvoyPluginBindingInfo getPluginBindingInfo(long pluginBindingInfoId) {
        return envoyPluginBindingInfoDao.get(pluginBindingInfoId);
    }

    @Override
    public ErrorCode checkUpdatePluginConfiguration(long pluginBindingInfoId, String pluginConfiguration, long templateId) {
        if (0 < templateId) {
            EnvoyPluginTemplateInfo templateInfo = envoyPluginTemplateService.getTemplateById(templateId);
            if (null == templateInfo) {
                logger.info("更新插件配置时，指定的模板不存在! templateId:{}", templateId);
                return CommonErrorCode.NoSuchPluginTemplate;
            }
            pluginConfiguration = templateInfo.getPluginConfiguration();
        }
        if (StringUtils.isBlank(pluginConfiguration)) {
            logger.info("更新插件配置时，参数PluginConfiguration缺失!");
            return CommonErrorCode.MissingParameter("PluginConfiguration");
        }
        EnvoyPluginBindingInfo pluginBindingInfo = getPluginBindingInfo(pluginBindingInfoId);
        if (null == pluginBindingInfo) {
            logger.info("更新插件配置时，指定的绑定关系不存在! pluginBindingInfoId:{}", pluginBindingInfoId);
            return CommonErrorCode.NoSuchPluginBinding;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean updatePluginConfiguration(long pluginBindingInfoId, String pluginConfiguration, long templateId) {
        EnvoyPluginBindingInfo bindingInfo = getPluginBindingInfo(pluginBindingInfoId);
        if (null == bindingInfo) {
            logger.error("更新插件配置时指定的绑定关系不存在! pluginBindingInfoId:{}", pluginBindingInfoId);
            return false;
        }
        EnvoyPluginTemplateInfo templateInfo = null;
        if (0 < templateId) {
            templateInfo = envoyPluginTemplateService.getTemplateById(templateId);
            pluginConfiguration = templateInfo.getPluginConfiguration();
        }
        bindingInfo.setPluginConfiguration(pluginConfiguration);
        BindingPluginInfo bindingPluginInfo = BindingPluginInfo.
                createBindingPluginFromEnvoyPluginBindingInfo(bindingInfo);

        if (EnvoyPluginBindingInfo.BINDING_STATUS_ENABLE.equals(bindingInfo.getBindingStatus())) {
            // 插件启用状态下更新api-plane的插件资源
            boolean updateRes = updateGatewayPlugin(bindingPluginInfo, pluginBindingInfoId);
            if (!updateRes) {
                logger.error("update global_plugin config to api-plane failed. plugin_id: {}", pluginBindingInfoId);
                return false;
            }
        }

        bindingInfo.setUpdateTime(System.currentTimeMillis());
        if (0 >= templateId) {
            bindingInfo.setTemplateId(0);
            bindingInfo.setTemplateVersion(0);
        } else {
            bindingInfo.setTemplateId(templateId);
            bindingInfo.setTemplateVersion(templateInfo.getTemplateVersion());
        }
        envoyPluginBindingInfoDao.update(bindingInfo);
        return true;
    }

    @Override
    public boolean updatePluginConfiguration(long pluginBindingInfoId, String pluginConfiguration, long templateId, long pluginTemplateVersion) {
        try {
            updatePluginConfiguration(pluginBindingInfoId, pluginConfiguration, templateId);
            EnvoyPluginBindingInfo bindingInfo = getPluginBindingInfo(pluginBindingInfoId);
            bindingInfo.setTemplateVersion(pluginTemplateVersion);
            bindingInfo.setUpdateTime(System.currentTimeMillis());
            envoyPluginBindingInfoDao.update(bindingInfo);
            return true;
        } catch (Exception e) {
            logger.info("同步模板配置到插件失败!", e);
            return false;
        }
    }

    @Override
    public long getBindingPluginCount(long gwId, long projectId, String bindingObjectId, List<String> bindingObjectTypeList, String pattern) {
        List<Long> gwIdList = getGwIdList(gwId, projectId, pattern);
        List<String> bindingObjectIdList = getBindingObjectIdList(projectId, bindingObjectId, Sets.newHashSet(bindingObjectTypeList), pattern);

        return envoyPluginBindingInfoDao.getBindingPluginCount(projectId, gwId, gwIdList, bindingObjectId, bindingObjectIdList, bindingObjectTypeList, pattern);
    }

    private List<Long> getGwIdList(long gwId, long projectId, String pattern) {
        List<Long> gwIdList = Lists.newArrayList();
        if (0 < gwId) {
            return gwIdList;
        } else if (StringUtils.isNotBlank(pattern)) {
            gwIdList = gatewayInfoService.getGwIdListByNameFuzzy(pattern, projectId);
        }
        return gwIdList;
    }

    private List<String> getBindingObjectIdList(long projectId, String bindingObjectId, Set<String> bindingObjectTypeList, String pattern) {
        List<String> bindingObjectIdList = Lists.newArrayList();

        // 如果传入了绑定对象id，则不进行路由名称、服务名称的模糊查询
        if (StringUtils.isNotBlank(bindingObjectId)) {
            return bindingObjectIdList;
        }

        // 如果未传入绑定对象id，且传入了pattern，则使用pattern进行路由名称、服务名称的模糊查询
        Set<String> routeRuleIdList = Sets.newHashSet();
        Set<String> serviceIdList = Sets.newHashSet();
        if (StringUtils.isNotBlank(pattern)) {
            if (CollectionUtils.isEmpty(bindingObjectTypeList) || bindingObjectTypeList.contains(EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE)) {
                routeRuleIdList = envoyRouteRuleInfoService.getRouteRuleIdListByNameFuzzy(pattern, projectId).stream().map(String::valueOf).collect(Collectors.toSet());
            }
            if (CollectionUtils.isEmpty(bindingObjectTypeList) || bindingObjectTypeList.contains(EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_SERVICE)) {
                serviceIdList = serviceInfoService.getServiceIdListByDisplayNameFuzzy(pattern, projectId).stream().map(String::valueOf).collect(Collectors.toSet());
            }
        }
        bindingObjectIdList.addAll(routeRuleIdList);
        bindingObjectIdList.addAll(serviceIdList);
        return bindingObjectIdList;
    }

    @Override
    public List<EnvoyPluginBindingInfo> getBindingPluginList(long gwId, long projectId, String bindingObjectId, List<String> bindingObjectTypeList, String pattern, long offset, long limit, String sortKey, String sortValue) {
        List<Long> gwIdList = getGwIdList(gwId, projectId, pattern);
        List<String> bindingObjectIdList = getBindingObjectIdList(projectId, bindingObjectId, Sets.newHashSet(bindingObjectTypeList), pattern);

        List<EnvoyPluginBindingInfo> pluginBindingInfoList = envoyPluginBindingInfoDao.getBindingPluginList(projectId, gwId, gwIdList, bindingObjectId, bindingObjectIdList, bindingObjectTypeList, pattern, offset, limit, sortKey, sortValue);
        return CollectionUtils.isEmpty(pluginBindingInfoList) ? Lists.newArrayList() : pluginBindingInfoList;
    }

    @Override
    public long deletePluginList(long gwId, String bindingObjectId, String bindingObjectType) {
        return envoyPluginBindingInfoDao.batchDeleteBindingInfo(getPluginBindingList(gwId, bindingObjectId, bindingObjectType));
    }

    @Override
    public ErrorCode checkUpdatePluginBindingStatus(long pluginBindingInfoId, String bindingStatus) {
        EnvoyPluginBindingInfo bindingInfo = getPluginBindingInfo(pluginBindingInfoId);
        if (null == bindingInfo) {
            logger.error("修改插件绑定关系状态时，指定插件绑定关系不存在! pluginBindinginfoId:{}", pluginBindingInfoId);
            return CommonErrorCode.NoSuchPluginBinding;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean updatePluginBindingStatus(long pluginBindingInfoId, String bindingStatus) {
        EnvoyPluginBindingInfo pluginBindingInfo = getPluginBindingInfo(pluginBindingInfoId);
        if (null == pluginBindingInfo) {
            logger.error("修改插件绑定关系状态时，指定插件绑定关系不存在! pluginBindingInfoId:{}", pluginBindingInfoId);
            return false;
        }

        BindingPluginInfo bindingPluginInfo =
                BindingPluginInfo.createBindingPluginFromEnvoyPluginBindingInfo(pluginBindingInfo);
        if (EnvoyPluginBindingInfo.BINDING_STATUS_DISABLE.equals(bindingStatus.trim().toLowerCase())) {
            if (!deleteGatewayPlugin(bindingPluginInfo, pluginBindingInfoId)) {
                return false;
            }
            pluginBindingInfo.setBindingStatus(EnvoyPluginBindingInfo.BINDING_STATUS_DISABLE);
        } else {
            if (!publishGatewayPlugin(bindingPluginInfo)) {
                return false;
            }
            pluginBindingInfo.setBindingStatus(EnvoyPluginBindingInfo.BINDING_STATUS_ENABLE);
        }
        pluginBindingInfo.setUpdateTime(System.currentTimeMillis());
        envoyPluginBindingInfoDao.update(pluginBindingInfo);
        return true;
    }

    @Override
    public void fillDtoFiled(List<EnvoyPluginBindingDto> envoyPluginBindingDtoList) {
        if (CollectionUtils.isEmpty(envoyPluginBindingDtoList)) {
            return;
        }

        Set<Long> templateIdSet = envoyPluginBindingDtoList.stream()
                .map(EnvoyPluginBindingDto::getTemplateId).collect(Collectors.toSet());
        Set<Long> gwIdSet = envoyPluginBindingDtoList.stream()
                .map(EnvoyPluginBindingDto::getGwId).collect(Collectors.toSet());
        Set<Long> serviceIdSet = envoyPluginBindingDtoList.stream()
                .filter(item -> EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_SERVICE.equals(item.getBindingObjectType()))
                .map(item -> Long.valueOf(item.getBindingObjectId())).collect(Collectors.toSet());
        Set<Long> routeRuleIdSet = envoyPluginBindingDtoList.stream()
                .filter(item -> EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE.equals(item.getBindingObjectType()))
                .map(item -> Long.valueOf(item.getBindingObjectId())).collect(Collectors.toSet());

        Map<Long, GatewayInfo> gatewayInfoMap = gatewayInfoService.getGatewayInfoList(Lists.newArrayList(gwIdSet))
                .stream().collect(Collectors.toMap(GatewayInfo::getId, item -> item));
        Map<Long, ServiceInfo> serviceInfoMap = serviceInfoService.getServiceInfoList(Lists.newArrayList(serviceIdSet))
                .stream().collect(Collectors.toMap(ServiceInfo::getId, item -> item));
        Map<Long, RouteRuleInfo> routeRuleInfoMap = envoyRouteRuleInfoService.getRouteRuleList(Lists.newArrayList(routeRuleIdSet))
                .stream().collect(Collectors.toMap(RouteRuleInfo::getId, item -> item));
        Map<Long, EnvoyPluginTemplateInfo> templateInfoMap = envoyPluginTemplateService.batchGet(Lists.newArrayList(templateIdSet))
                .stream().collect(Collectors.toMap(EnvoyPluginTemplateInfo::getId, item -> item));

        envoyPluginBindingDtoList.forEach(item -> {
            GatewayInfo gatewayInfo = gatewayInfoMap.get(item.getGwId());
            item.setGwName(null == gatewayInfo ? StringUtils.EMPTY : gatewayInfo.getGwName());
            if (EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE.equals(item.getBindingObjectType())) {
                RouteRuleInfo routeRuleInfo = routeRuleInfoMap.get(Long.valueOf(item.getBindingObjectId()));
                item.setBindingObjectName(null == routeRuleInfo ? StringUtils.EMPTY : routeRuleInfo.getRouteRuleName());
            } else if (EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL.equals(item.getBindingObjectType())) {
                item.setBindingObjectName(null == gatewayInfo ? StringUtils.EMPTY : gatewayInfo.getGwName());
            } else {
                ServiceInfo serviceInfo = serviceInfoMap.get(Long.valueOf(item.getBindingObjectId()));
                item.setBindingObjectName(null == serviceInfo ? StringUtils.EMPTY : serviceInfo.getDisplayName());
            }
            EnvoyPluginTemplateInfo templateInfo = templateInfoMap.get(item.getTemplateId());
            if (0 == item.getTemplateId() || null == templateInfo || item.getTemplateVersion() == templateInfo.getTemplateVersion()) {
                item.setTemplateStatus(EnvoyPluginTemplateInfo.STATUS_NO_NEED_SYNC);
            } else {
                item.setTemplateStatus(EnvoyPluginTemplateInfo.STATUS_NEED_SYNC);
            }
        });
    }

    @Override
    public List<EnvoyPluginBindingInfo> getBindingListByTemplateId(long templateId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("templateId", templateId);
        return envoyPluginBindingInfoDao.getRecordsByField(params);
    }

    @Override
    public List<EnvoyPluginBindingInfo> getBindingListByTemplateId(long templateId, long gwId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("templateId", templateId);
        params.put("gwId", gwId);
        return envoyPluginBindingInfoDao.getRecordsByField(params);
    }

    @Override
    public boolean batchDissociateTemplate(List<Long> bindingInfoList) {
        envoyPluginBindingInfoDao.batchDissociateTemplate(bindingInfoList);
        return true;
    }

    @Override
    public List<EnvoyPluginBindingInfo> batchGetById(List<Long> bindingInfoIdList) {
        return envoyPluginBindingInfoDao.batchGetById(bindingInfoIdList);
    }

    @Override
    public boolean isInsidePlugin(EnvoyPluginBindingInfo envoyPluginBindingInfo) {
        if ("soap-json-transcoder".equals(envoyPluginBindingInfo.getPluginType())) return true;
        // other plugin
        return false;
    }

    @Override
    public EnvoyPluginBindingDto fromMeta(EnvoyPluginBindingInfo bindingInfo) {
        EnvoyPluginBindingDto bindingDto = new EnvoyPluginBindingDto();
        bindingDto.setId(bindingInfo.getId());
        bindingDto.setGwId(bindingInfo.getGwId());
        bindingDto.setProjectId(bindingInfo.getProjectId());
        bindingDto.setPluginType(bindingInfo.getPluginType());
        bindingDto.setCreateTime(bindingInfo.getCreateTime());
        bindingDto.setUpdateTime(bindingInfo.getUpdateTime());
        /**
         * plugin_type与plugin_name并非意义对应，plugin_type进行区分，plugin_name进行前端展示
         */
        bindingDto.setPluginName(pluginCacheService.getPluginNameFromCache(bindingInfo.getPluginType()));
        bindingDto.setBindingStatus(bindingInfo.getBindingStatus());
        bindingDto.setPluginPriority(bindingInfo.getPluginPriority());
        bindingDto.setBindingObjectId(bindingInfo.getBindingObjectId());
        bindingDto.setBindingObjectType(bindingInfo.getBindingObjectType());
        bindingDto.setTemplateId(bindingInfo.getTemplateId());
        bindingDto.setPluginConfiguration(bindingInfo.getPluginConfiguration());
        bindingDto.setTemplateVersion(bindingInfo.getTemplateVersion());
        return bindingDto;
    }

    private List<Long> createPluginIdList(Long pluginId) {
        List<Long> pluginIdList = new ArrayList<>();
        pluginIdList.add(pluginId);
        return pluginIdList;
    }

    private String createStringPluginIdSequence(List<Long> pluginIdList) {
        return pluginIdList.stream().map(String::valueOf).collect(Collectors.joining("-"));
    }

    /**
     * 面向路由插件操作类型
     */
    public enum Operation {
        /**
         * 路由插件创建动作
         */
        CREATE("create route plugin"),
        /**
         * 路由插件更新动作
         */
        UPDATE("update route plugin"),
        /**
         * 路由插件删除动作
         */
        DELETE("delete route plugin");

        private final String action;

        Operation(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }
    }

}
