package org.hango.cloud.envoy.infra.routeproxy.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.hango.cloud.common.infra.base.dto.ResourceDTO;
import org.hango.cloud.common.infra.base.mapper.RouteRuleProxyMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.meta.ResourceEnum;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.meta.PluginConstant;
import org.hango.cloud.envoy.infra.base.service.VersionManagerService;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboBindingService;
import org.hango.cloud.envoy.infra.plugin.service.IEnvoyPluginInfoService;
import org.hango.cloud.envoy.infra.routeproxy.service.IEnvoyRouteRuleProxyService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.PLANE_PORTAL_PATH;
import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.MODULE_API_PLANE;

/**
 * @author xin li
 * @date 2022/9/8 20:05
 */
@Service
public class EnvoyRouteRuleProxyServiceImpl implements IEnvoyRouteRuleProxyService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyRouteRuleProxyServiceImpl.class);

    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;


    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;


    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @Autowired
    private EnvoyRouteRuleProxyBuilderService envoyRouteRuleProxyBuilderService;

    @Autowired
    private IDubboBindingService dubboBindingService;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    RouteRuleProxyMapper routeRuleProxyMapper;

    @Autowired
    private VersionManagerService versionManagerService;


    @Override
    public boolean publishRouteProxy(RouteRuleProxyDto routeRuleProxyInfo, List<String> pluginConfigurations) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routeRuleProxyInfo.getVirtualGwId());
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "PublishAPI");
        params.put("Version", "2019-07-25");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = envoyRouteRuleProxyBuilderService.buildRouteProxyInfo(routeRuleProxyInfo, pluginConfigurations);
        ResourceDTO resourceDTO = versionManagerService.getResourceDTO(routeRuleProxyInfo.getVirtualGwId(), routeRuleProxyInfo.getRouteRuleId(), ResourceEnum.Route);
        try {
            return versionManagerService.publishRouteWithVersionManager(virtualGatewayDto.getConfAddr() + PLANE_PORTAL_PATH, params, headers, body, resourceDTO);
        } catch (Exception e) {
            logger.error("调用API-plane发布API接口出现异常,e{:}", e);
            return false;
        }
    }

    @Override
    public boolean deleteRouteProxy(RouteRuleProxyDto proxyDto) {
        long virtualGwId = proxyDto.getVirtualGwId();
        long routeRuleId = proxyDto.getRouteRuleId();
        RouteRuleProxyDto routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(virtualGwId, routeRuleId);
        return dubboBindingService.deleteDubboInfo(routeRuleProxy.getId(),BaseConst.ROUTE)
                && deleteRouteRuleByApiPlane(routeRuleProxy)
                && deleteAllRoutePlugins(virtualGwId, routeRuleId);
    }


    public boolean deleteRouteRuleByApiPlane(RouteRuleProxyDto routeRuleProxyInfo) {
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(routeRuleProxyInfo.getVirtualGwId());

        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DeleteAPI");
        params.put("Version", "2019-07-25");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = envoyRouteRuleProxyBuilderService.buildRouteProxyInfo(routeRuleProxyInfo, Lists.newArrayList());
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGateway.getConfAddr() + PLANE_PORTAL_PATH, body.toJSONString(), params, headers, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane删除服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }

        return true;
    }


    @Override
    public boolean deleteAllRoutePlugins(long virtualGwId, long routeRuleId) {
        List<PluginBindingInfo> alreadyBindingPlugins = pluginInfoService.getEnablePluginBindingList(virtualGwId, String.valueOf(routeRuleId), PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        // 没有插件无需删除
        if (CollectionUtils.isEmpty(alreadyBindingPlugins)) {
            return true;
        }
        BindingPluginDto bindingPluginDto = new BindingPluginDto(virtualGwId, BaseConst.PLUGIN_TYPE_ROUTE, routeRuleId, "", "");
        // 删除路由插件
        List<Long> pluginIdList = alreadyBindingPlugins.stream().map(PluginBindingInfo::getId).collect(Collectors.toList());
        if (!envoyPluginInfoService.deleteGatewayPlugin(bindingPluginDto, pluginIdList)) {
            logger.error("{} delete route plugin from Api-plane failed", PluginConstant.PLUGIN_LOG_NOTE);
            return false;
        }
        //删除路由规则绑定的插件
        pluginInfoService.deletePluginList(virtualGwId, String.valueOf(routeRuleId), PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        return true;
    }

    @Override
    public long updateRouteProxy(RouteRuleProxyDto routeRuleProxyDto) {
        List<PluginBindingInfo> alreadyBindingPlugins = pluginInfoService.getEnablePluginBindingList(routeRuleProxyDto.getVirtualGwId(), String.valueOf(routeRuleProxyDto.getRouteRuleId()), PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        BindingPluginDto bindingPluginDto = new BindingPluginDto(routeRuleProxyDto.getVirtualGwId(), BaseConst.PLUGIN_TYPE_ROUTE, routeRuleProxyDto.getRouteRuleId(), "", "");
        //使能状态修改为enable
        if (isPublish(routeRuleProxyDto.getEnableState())) {
            return doPublishRouteWithPlugin(routeRuleProxyDto, alreadyBindingPlugins, bindingPluginDto);
        } else{
            return doOfflineRouteWithPlugin(routeRuleProxyDto, alreadyBindingPlugins, bindingPluginDto);
        }
    }


    private long doPublishRouteWithPlugin(RouteRuleProxyDto routeRuleProxyInDb, List<PluginBindingInfo> alreadyBindingPlugins, BindingPluginDto bindingPluginDto) {
        // 发布路由
        List<String> newPluginConfigurations = alreadyBindingPlugins.stream().map(PluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
        if (!publishRouteProxy(routeRuleProxyInDb, newPluginConfigurations)) {
            return BaseConst.ERROR_RESULT;
        }
        bindingPluginDto.setEnableRouteOperation(true);
        // 发布路由插件（此处是本路由下的插件全量发布，只需要关注路由元信息即可，插件配置不关心）
        envoyPluginInfoService.publishGatewayPlugin(bindingPluginDto);
        return 0;
    }

    private long doOfflineRouteWithPlugin(RouteRuleProxyDto routeRuleProxyInDb, List<PluginBindingInfo> alreadyBindingPlugins, BindingPluginDto bindingPluginDto) {
        // 删除路由插件
        List<Long> pluginIdList = alreadyBindingPlugins.stream().map(PluginBindingInfo::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(pluginIdList) && !envoyPluginInfoService.deleteGatewayPlugin(bindingPluginDto, pluginIdList)) {
            return BaseConst.ERROR_RESULT;
        }

        //删除路由
        if (!deleteRouteRuleByApiPlane(routeRuleProxyInDb)) {
            return BaseConst.ERROR_RESULT;
        }
        return 0;
    }

    private boolean isPublish(String enableState) {
        return BaseConst.ROUTE_RULE_ENABLE_STATE.equals(enableState);
    }

}
