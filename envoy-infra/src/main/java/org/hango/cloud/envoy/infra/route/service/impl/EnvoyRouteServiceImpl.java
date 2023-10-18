package org.hango.cloud.envoy.infra.route.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.hango.cloud.common.infra.base.dto.ResourceDTO;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.meta.PluginConstant;
import org.hango.cloud.envoy.infra.base.service.VersionManagerService;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboBindingService;
import org.hango.cloud.envoy.infra.plugin.manager.IPluginOperateManagerService;
import org.hango.cloud.envoy.infra.plugin.service.IEnvoyPluginInfoService;
import org.hango.cloud.envoy.infra.route.service.IEnvoyRouteService;
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
public class EnvoyRouteServiceImpl implements IEnvoyRouteService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyRouteServiceImpl.class);

    @Autowired
    private IRouteService routeService;


    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;


    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @Autowired
    private IPluginOperateManagerService pluginOperateManagerService;

    @Autowired
    private EnvoyRouteBuilderService envoyRouteBuilderService;

    @Autowired
    private IDubboBindingService dubboBindingService;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private VersionManagerService versionManagerService;

    @Autowired
    private IServiceProxyService serviceProxyService;


    @Override
    public boolean publishRoute(RouteDto routeDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routeDto.getVirtualGwId());
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "PublishAPI");
        params.put("Version", "2019-07-25");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = envoyRouteBuilderService.buildRouteInfo(routeDto);
        // TODO route 多集群适配（此处路由ID和服务ID都不是发布的时候拿得到的，所以这里创建阶段的调用，都是空，需要考虑如何适配）
//        ResourceDTO resourceDTO = versionManagerService.getResourceDTO(routeDto.getVirtualGwId(), routeDto.getId(), ResourceEnum.Route);
        ResourceDTO resourceDTO = null;
        try {
            return versionManagerService.publishRouteWithVersionManager(virtualGatewayDto.getConfAddr() + PLANE_PORTAL_PATH, params, headers, body, resourceDTO);
        } catch (Exception e) {
            logger.error("调用API-plane发布API接口出现异常,e{:}", e);
            return false;
        }
    }

    @Override
    public boolean deleteRoute(RouteDto routeDto) {
        long virtualGwId = routeDto.getVirtualGwId();
        long routeId = routeDto.getId();
        RouteDto routeFromDb = routeService.get(routeId);
        return dubboBindingService.deleteDubboInfo(routeFromDb.getId(), ApiConst.ROUTE)
                && deleteRouteRuleByApiPlane(routeFromDb)
                && deleteAllRoutePlugins(virtualGwId, routeId);
    }


    @Override
    public boolean deleteRouteRuleByApiPlane(RouteDto routeRuleProxyInfo) {
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(routeRuleProxyInfo.getVirtualGwId());

        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DeleteAPI");
        params.put("Version", "2019-07-25");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = envoyRouteBuilderService.buildRouteInfo(routeRuleProxyInfo);
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGateway.getConfAddr() + PLANE_PORTAL_PATH, body.toJSONString(), params, headers, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane删除服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }

        return true;
    }


    @Override
    public boolean deleteAllRoutePlugins(long virtualGwId, long routeRuleId) {
        List<PluginBindingDto> alreadyBindingPlugins = pluginInfoService.getPluginBindingList(virtualGwId, routeRuleId, BindingObjectTypeEnum.ROUTE.getValue());
        // 没有插件无需删除
        if (CollectionUtils.isEmpty(alreadyBindingPlugins)) {
            return true;
        }
        List<PluginBindingInfo> enablePluginList = pluginInfoService.getEnablePluginBindingList(virtualGwId, routeRuleId, BindingObjectTypeEnum.ROUTE.getValue());
        //下线生效的插件配置
        if (!CollectionUtils.isEmpty(enablePluginList)){
            BindingPluginDto bindingPluginDto = new BindingPluginDto(virtualGwId, BindingObjectTypeEnum.ROUTE.getValue(), routeRuleId, "", "");
            List<Long> enablePluginIdList = enablePluginList.stream().map(PluginBindingInfo::getId).collect(Collectors.toList());
            bindingPluginDto.setPluginIdList(enablePluginIdList);
            ErrorCode errorCode = pluginOperateManagerService.delete(bindingPluginDto);
            if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
                logger.error("{} delete route plugin from Api-plane failed", PluginConstant.PLUGIN_LOG_NOTE);
                return false;
            }
        }
        //删除db
        for (PluginBindingDto alreadyBindingPlugin : alreadyBindingPlugins) {
            pluginInfoService.delete(alreadyBindingPlugin);
        }
        return true;
    }

    @Override
    public long updateRoute(RouteDto routeDto) {
        List<PluginBindingInfo> alreadyBindingPlugins = pluginInfoService.getEnablePluginBindingList(
                routeDto.getVirtualGwId(), routeDto.getId(), BindingObjectTypeEnum.ROUTE.getValue());

        BindingPluginDto bindingPluginDto = new BindingPluginDto(routeDto.getVirtualGwId(),
                BindingObjectTypeEnum.ROUTE.getValue(), routeDto.getId(), "", "");
        // 使能状态修改为enable
        if (isPublish(routeDto.getEnableState())) {
            return doPublishRouteWithPlugin(routeDto, bindingPluginDto);
        } else {
            return doOfflineRouteWithPlugin(routeDto, alreadyBindingPlugins, bindingPluginDto);
        }
    }


    private long doPublishRouteWithPlugin(RouteDto routeDto, BindingPluginDto bindingPluginDto) {
        // 发布路由
        if (!publishRoute(routeDto)) {
            return BaseConst.ERROR_RESULT;
        }
        bindingPluginDto.setEnableRouteOperation(true);
        // 发布路由插件（此处是本路由下的插件全量发布，只需要关注路由元信息即可，插件配置不关心）
        pluginOperateManagerService.create(bindingPluginDto);
        return 0;
    }

    private long doOfflineRouteWithPlugin(RouteDto routeRuleProxyInDb, List<PluginBindingInfo> alreadyBindingPlugins, BindingPluginDto bindingPluginDto) {
        // 删除路由插件
        List<Long> pluginIdList = alreadyBindingPlugins.stream().map(PluginBindingInfo::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(pluginIdList)) {
            bindingPluginDto.setPluginIdList(pluginIdList);
            ErrorCode errorCode = pluginOperateManagerService.delete(bindingPluginDto);
            if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
                return BaseConst.ERROR_RESULT;
            }
        }

        //删除路由
        if (!deleteRouteRuleByApiPlane(routeRuleProxyInDb)) {
            return BaseConst.ERROR_RESULT;
        }
        return 0;
    }

    private boolean isPublish(String enableState) {
        return BaseConst.ENABLE_STATE.equals(enableState);
    }
}
