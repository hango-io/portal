package org.hango.cloud.dashboard.envoy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IRouteRuleProxyDao;
import org.hango.cloud.dashboard.envoy.meta.BindingPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.ICopyRouteRuleProxy;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CopyRouteRuleProxyImpl implements ICopyRouteRuleProxy {

    private static final Logger logger = LoggerFactory.getLogger(CopyRouteRuleProxyImpl.class);

    @Autowired
    private IRouteRuleProxyDao routeRuleProxyDao;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @Autowired
    private IRouteRuleProxyService envoyRouteRuleProxyService;

    @Override
    public ErrorCode checkCopyRouteRuleProxy(long routeRuleId, long originGwId, long desGwId) {
        if (envoyRouteRuleProxyService.getRouteRuleProxyCount(originGwId, routeRuleId) == 0) {
            logger.info("复制已发布路由，路由未发布到源网关");
            return CommonErrorCode.RouteRuleNotPublished;
        }

        RouteRuleProxyInfo routeRuleProxy = envoyRouteRuleProxyService.getRouteRuleProxy(originGwId, routeRuleId);
        if (routeRuleProxy != null) {
            ServiceProxyInfo serviceProxyInfoInDb = serviceProxyService.getServiceProxyByServiceIdAndGwId(originGwId, routeRuleProxy.getServiceId());
            ServiceProxyInfo serviceProxyInfoDesInDb = serviceProxyService.getServiceProxyByServiceIdAndGwId(desGwId, routeRuleProxy.getServiceId());
            if (serviceProxyInfoDesInDb != null && !serviceProxyInfoDesInDb.getBackendService().equals(serviceProxyInfoInDb.getBackendService())) {
                logger.info("路由所属服务发布至源网关和目标网关后端地址不同，禁止复制。服务id:{},源网关id;{}，目标网关id:{}",
                        new Object[]{routeRuleProxy.getServiceId(), originGwId, desGwId});
                return CommonErrorCode.BackendServiceDifferent;
            }
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean copyRouteRuleProxy(long routeRuleId, long originGwId, long desGwId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("routeRuleId", routeRuleId);
        params.put("gwId", originGwId);
        RouteRuleProxyInfo routeRuleProxyInfoInDb = routeRuleProxyDao.getRecordsByField(params).get(0);
        if (routeRuleProxyInfoInDb == null) {
            logger.info("路由未发布至源网关,不复制，路由id:{},源网关id:{}", routeRuleId, originGwId);
            return true;
        }

        //路由所依赖服务未发布到目的网关，一键复制服务至目的网关
        if (serviceProxyService.getServiceProxyCount(desGwId, routeRuleProxyInfoInDb.getServiceId()) == 0) {
            logger.info("复制发布已发布路由，路由所属服务未发布至目标网关，需要发布服务至目标网关");
            ServiceProxyInfo serviceProxyInfoInDb = serviceProxyService.getServiceProxyByServiceIdAndGwId(originGwId, routeRuleProxyInfoInDb.getServiceId());
            if (serviceProxyInfoInDb == null) {
                logger.info("路由所属服务未发布到源网关，返回false，需要重新发布");
                return false;
            }
            serviceProxyInfoInDb.setGwId(desGwId);
            if (Const.ERROR_RESULT == serviceProxyService.publishServiceToGw(serviceProxyService.fromMeta(serviceProxyInfoInDb))) {
                logger.info("发布路由所属服务至目标网关，发布失败");
                return false;
            }
        }

        List<EnvoyPluginBindingInfo> alreadyBindingPluginsOri = envoyPluginInfoService.getEnablePluginBindingList(originGwId, String.valueOf(routeRuleId),
                EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        routeRuleProxyInfoInDb.setGwId(desGwId);
        routeRuleProxyInfoInDb.setId(0);

        //如果路由已发布至目标网关，则进行配置更新即可
        RouteRuleProxyInfo routeRuleProxyDesInDb = envoyRouteRuleProxyService.getRouteRuleProxy(desGwId, routeRuleId);
        if (routeRuleProxyDesInDb != null) {
            logger.info("复制已发布路由，路由新发布到目标网关，修改id为已发布路由id，仅进行配置更新");
            routeRuleProxyInfoInDb.setId(routeRuleProxyDesInDb.getId());
            //当前路由为禁用状态，已发布网关路由为有效状态，需要删除vs资源
            if (Const.ROUTE_RULE_DISABLE_STATE.equals(routeRuleProxyInfoInDb.getEnableState())
                    && Const.ROUTE_RULE_ENABLE_STATE.equals(routeRuleProxyDesInDb.getEnableState())) {
                if (Const.ERROR_RESULT == envoyRouteRuleProxyService.updateEnableState(desGwId, routeRuleId, Const.ROUTE_RULE_DISABLE_STATE)) {
                    logger.error("复制已发布路由，禁用目标路由失败");
                    return false;
                }
            }
        }
        List<String> newPluginConfigurations = alreadyBindingPluginsOri.stream().map(EnvoyPluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
        //复制发布路由至目标网关
        if (Const.ERROR_RESULT == envoyRouteRuleProxyService.publishRouteRule(routeRuleProxyInfoInDb, newPluginConfigurations, true)) {
            logger.error("复制已发布路由，发布失败");
            return false;
        }
        // 路由使能状态才需要在api-plane侧发布插件GatewayPlugin资源
        if (Const.ROUTE_RULE_ENABLE_STATE.equals(routeRuleProxyInfoInDb.getEnableState())) {
            BindingPluginInfo bindingPluginInfo = new BindingPluginInfo(originGwId,
                    BindingPluginInfo.PLUGIN_TYPE_ROUTE,
                    routeRuleId,
                    "",
                    "");
            bindingPluginInfo.setDestGatewayId(String.valueOf(desGwId));
            if (!envoyPluginInfoService.publishGatewayPlugin(bindingPluginInfo)) {
                return false;
            }
        }
        return opsForCopyRoutePluginInDb(routeRuleId, originGwId, desGwId);
    }

    public boolean opsForCopyRoutePluginInDb(long routeRuleId, long originGwId, long desGwId) {
        try {
            List<EnvoyPluginBindingInfo> alreadyBindingPlugins = envoyPluginInfoService.getPluginBindingList(originGwId, String.valueOf(routeRuleId),
                    EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
            //删除目标网关路由插件数据库存储
            List<EnvoyPluginBindingInfo> alreadyBindingPluginDes = envoyPluginInfoService.getPluginBindingList(desGwId, String.valueOf(routeRuleId),
                    EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
            alreadyBindingPluginDes.forEach(item -> envoyPluginInfoService.deletePluginFromDb(item));
            //将插件配置同步至目标网关
            if (CollectionUtils.isNotEmpty(alreadyBindingPlugins)) {
                alreadyBindingPlugins.forEach(envoyPluginBindingInfo -> {
                    envoyPluginBindingInfo.setGwId(desGwId);
                    envoyPluginBindingInfo.setCreateTime(System.currentTimeMillis());
                    envoyPluginBindingInfo.setUpdateTime(System.currentTimeMillis());
                    envoyPluginInfoService.bindingPluginToDb(envoyPluginBindingInfo);
                });
            }
        } catch (Exception e) {
            logger.error("同步路由插件配置至目标网关数据库出现异常，e:{}", e);
            return false;
        }
        return true;
    }
}
