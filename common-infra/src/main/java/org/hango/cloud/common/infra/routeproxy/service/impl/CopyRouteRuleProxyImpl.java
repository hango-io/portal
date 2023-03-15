package org.hango.cloud.common.infra.routeproxy.service.impl;

import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.service.ICopyRouteRuleProxy;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CopyRouteRuleProxyImpl implements ICopyRouteRuleProxy {

    private static final Logger logger = LoggerFactory.getLogger(CopyRouteRuleProxyImpl.class);

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Override
    public ErrorCode checkCopyRouteRuleProxy(long routeRuleId, long originGwId, long desGwId) {
        RouteRuleProxyDto routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(originGwId, routeRuleId);
        if (routeRuleProxy == null) {
            logger.info("复制已发布路由，路由未发布到源网关");
            return CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED;
        }

        ServiceProxyDto serviceProxyInfoInDb = serviceProxyService.getServiceProxyByServiceIdAndGwId(originGwId, routeRuleProxy.getServiceId());
        ServiceProxyDto serviceProxyInfoDesInDb = serviceProxyService.getServiceProxyByServiceIdAndGwId(desGwId, routeRuleProxy.getServiceId());
        if (serviceProxyInfoDesInDb == null) {
            logger.info("复制发布已发布路由，路由所属服务未发布至目标网关");
            return CommonErrorCode.SERVICE_NOT_PUBLISHED;
        }
        if (!serviceProxyInfoDesInDb.getBackendService().equals(serviceProxyInfoInDb.getBackendService())) {
            logger.info("路由所属服务发布至源网关和目标网关后端地址不同，禁止复制。服务id:{},源网关id;{}，目标网关id:{}",
                    routeRuleProxy.getServiceId(), originGwId, desGwId);
            return CommonErrorCode.BACKEND_SERVICE_DIFFERENT;
        }
        if (routeRuleProxy.getMirrorSwitch() == 1){
            long serviceId = routeRuleProxy.getMirrorTraffic().getServiceId();
            ServiceProxyDto serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(desGwId, serviceId);
            if (null == serviceProxyInfo) {
                logger.info("流量镜像指定服务未发布！serviceId:{}, virtualGwId:{}", serviceId, desGwId);
                return CommonErrorCode.SERVICE_NOT_PUBLISHED;
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public boolean copyRouteRuleProxy(long routeRuleId, long originGwId, long desGwId) {
        RouteRuleProxyDto originRouteProxy = routeRuleProxyService.getRouteRuleProxy(originGwId, routeRuleId);
        //如果路由已发布至目标网关，则进行配置更新即可
        RouteRuleProxyDto targetRouteProxy = routeRuleProxyService.getRouteRuleProxy(desGwId, routeRuleId);
        originRouteProxy.setVirtualGwId(desGwId);
        if (targetRouteProxy != null) {
            originRouteProxy.setId(targetRouteProxy.getId());
            routeRuleProxyService.update(originRouteProxy);
        }else {
            //发布路由
            originRouteProxy.setId(null);
            routeRuleProxyService.create(originRouteProxy);
        }
        //copy插件
        copyRoutePlugins(routeRuleId, originGwId, desGwId);
        return true;
    }

    private void copyRoutePlugins(long routeRuleId, long originGwId, long desGwId){
        List<PluginBindingDto> alreadyBindingPlugins = pluginInfoService.getPluginBindingList(originGwId, String.valueOf(routeRuleId),
                PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        //先清除目标网关对应路由的全部路由插件
        deleteDestGwRoutePlugins(routeRuleId, desGwId);
        //将插件配置同步至目标网关
        createDestGwRoutePlugins(desGwId, alreadyBindingPlugins);
    }

    private void createDestGwRoutePlugins(long desGwId, List<PluginBindingDto> alreadyBindingPlugins) {
        if (!CollectionUtils.isEmpty(alreadyBindingPlugins)) {
            alreadyBindingPlugins.forEach(pluginBindingInfo -> {
                //原插件发布网关改为目标网关
                pluginBindingInfo.setVirtualGwId(desGwId);
                pluginInfoService.create(pluginBindingInfo);
            });
        }
    }

    private void deleteDestGwRoutePlugins(long routeRuleId, long desGwId) {
        List<PluginBindingDto> alreadyBindingPluginDes = pluginInfoService.getPluginBindingList(desGwId, String.valueOf(routeRuleId),
                PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        alreadyBindingPluginDes.forEach(item -> pluginInfoService.delete(item));
    }
}
