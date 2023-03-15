package org.hango.cloud.envoy.infra.base.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.hango.cloud.common.infra.base.dto.ResourceDTO;
import org.hango.cloud.common.infra.base.mapper.RouteRuleProxyMapper;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.meta.ResourceEnum;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyPO;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.envoy.infra.base.aop.VersionManager;
import org.hango.cloud.envoy.infra.base.aop.VersionManagerAdvice;
import org.hango.cloud.envoy.infra.plugin.dto.GatewayPluginDto;
import org.hango.cloud.envoy.infra.serviceproxy.dto.DpServiceProxyDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.PLUGIN_TYPE_ROUTE;
import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.MODULE_API_PLANE;

/**
 * @Author zhufengwei
 * @Date 2023/1/16
 */
@Slf4j
@Service
public class VersionManagerService {

    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Autowired
    private RouteRuleProxyMapper routeRuleProxyMapper;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IPluginInfoService pluginInfoService;

    /**
     * 下发配置，切面实现版本号管理
     * 最后2个参数plugin和resourceDTO的位置不要改动
     * @see VersionManagerAdvice#publishProxyToApiPlane(ProceedingJoinPoint, VersionManager)
     */
    @VersionManager
    public Boolean publishServiceWithVersionManager(String url, Map<String, Object> params, HttpHeaders headers, DpServiceProxyDto service, ResourceDTO resourceDTO){
        HttpClientResponse response = HttpClientUtil.postRequest(url, JSONObject.toJSONString(service), params, headers, MODULE_API_PLANE);
        return handleResponse(response);
    }


    /**
     * 下发配置，切面实现版本号管理
     * 最后2个参数plugin和resourceDTO的位置不要改动
     * @see VersionManagerAdvice#publishProxyToApiPlane(ProceedingJoinPoint, VersionManager)
     */
    @VersionManager
    public Boolean publishRouteWithVersionManager(String url, Map<String, Object> params, HttpHeaders headers, JSONObject route, ResourceDTO resourceDTO){
        HttpClientResponse response = HttpClientUtil.postRequest(url, route.toJSONString(), params, headers, MODULE_API_PLANE);
        return handleResponse(response);
    }

    /**
     * 下发配置，切面实现版本号管理
     * 最后2个参数plugin和resourceDTO的位置不要改动
     * @see VersionManagerAdvice#publishProxyToApiPlane(ProceedingJoinPoint, VersionManager)
     */
    @VersionManager
    public Boolean publishPluginWithVersionManager(String url, Map<String, Object> params, HttpHeaders headers, GatewayPluginDto plugin, ResourceDTO resourceDTO){
        HttpClientResponse response = HttpClientUtil.postRequest(url, JSON.toJSONString(plugin), params, headers, MODULE_API_PLANE);
        return handleResponse(response);
    }

    private boolean handleResponse(HttpClientResponse response){
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            log.error("调用api-plane发布失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

    public ResourceDTO getResourceDTO(long vgId, long bindingObjectId, String bindingObjectType, String pluginType, String Action){
        boolean isRoutePlugin = PLUGIN_TYPE_ROUTE.equals(bindingObjectType);
        BindingPluginDto bindingPluginDto = new BindingPluginDto(vgId, bindingObjectType, bindingObjectId, pluginType, null);
        if ("DeletePlugin".equals(Action)){
            if (isRoutePlugin){
                List<PluginBindingInfo> bindingInfoList = pluginInfoService.getBindingInfoList(bindingPluginDto);
                List<PluginBindingInfo> enabledBindingInfoList= bindingInfoList.stream().filter(o -> PluginBindingInfo.BINDING_STATUS_ENABLE.equals(o.getBindingStatus())).collect(Collectors.toList());
                //路由只启用当前插件，会删除整个envoyplugin cr, 不需要进行版本号下发
                if (enabledBindingInfoList.size() <= 1){
                    return null;
                }
                //获取路由下所有插件的最大版本号
                Long maxVersion = bindingInfoList.stream().map(PluginBindingInfo::getVersion).max(Comparator.comparing(Long::intValue)).orElse(null);
                //选取id最小的插件需要更新版本号（需要排除当前插件），
                Long resourceId = enabledBindingInfoList.stream().filter(o -> !o.getPluginType().equals(pluginType)).map(PluginBindingInfo::getId).min(Comparator.comparing(Long::intValue)).orElse(null);
                return ResourceDTO.of(ResourceEnum.Plugin.name(), maxVersion, resourceId);
            }
            return null;
        }else if ("PublishPlugin".equals(Action)){
            if (isRoutePlugin){
                PluginBindingInfo pluginInfo = pluginInfoService.getBindingInfoList(bindingPluginDto).stream().max(Comparator.comparing(PluginBindingInfo::getVersion)).orElse(null);
                if (pluginInfo == null){
                    log.error("get route plugin resource error, plugin:{}", JSONObject.toJSONString(pluginInfo));
                    return null;
                }
                return ResourceDTO.of(ResourceEnum.Plugin.name(), pluginInfo.getVersion(), pluginInfo.getId());
            }else {
                PluginBindingInfo bindingInfo = pluginInfoService.getBindingInfo(bindingPluginDto);
                if (bindingInfo == null){
                    log.error("get global plugin resource error, plugin:{}", JSONObject.toJSONString(bindingPluginDto));
                    return null;
                }
                return ResourceDTO.of(ResourceEnum.Plugin.name(), bindingInfo.getVersion(), bindingInfo.getId());
            }
        }
        return null;
    }


    public ResourceDTO getResourceDTO(long vgId, long id, ResourceEnum resource){
        long version;
        long proxyId;
        switch (resource) {
            case Route:
                RouteRuleProxyPO query = RouteRuleProxyPO.builder().virtualGwId(vgId).routeRuleId(id).build();
                RouteRuleProxyPO proxyPO = routeRuleProxyMapper.selectOne(new QueryWrapper<>(query));
                if (proxyPO == null){
                    return null;
                }
                version = proxyPO.getVersion();
                proxyId = proxyPO.getId();
                break;
            case Service:
                ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyInfo(vgId, id);
                if (serviceProxyInfo == null){
                    return null;
                }
                version = serviceProxyInfo.getVersion();
                proxyId = serviceProxyInfo.getId();
                break;
            default:
                log.error("illegal resource kind {}", resource.name());
                return null;
        }
        return ResourceDTO.of(resource.name(), version, proxyId);
    }

}
