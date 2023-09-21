package org.hango.cloud.envoy.infra.plugin.manager;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.dto.ServiceMetaForRouteDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceTrafficPolicyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SessionStateDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.plugin.dto.GatewayPluginDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.ENABLE_STATE;

/**
 * @Author zhufengwei
 * @Date 2023/7/28
 */
@Slf4j
@Component
public class RoutePluginOperateService extends AbstractPluginOperateService {

    @Autowired
    IPluginInfoService pluginInfoService;

    @Autowired
    IRouteService routeService;

    @Autowired
    IServiceProxyService serviceProxyService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;


    @Override
    public ErrorCode create(BindingPluginDto plugin) {
        //校验是否需要执行创建操作
        if (!filter(plugin)) {
            return CommonErrorCode.SUCCESS;
        }
        //数据处理
        GatewayPluginDto gatewayPluginDto = buildCreateInfo(plugin);
        //创建插件
        return publishPlugin(gatewayPluginDto);
    }

    @Override
    public ErrorCode update(BindingPluginDto plugin) {
        //校验是否需要执行更新操作
        if (!filter(plugin)) {
            return CommonErrorCode.SUCCESS;
        }
        //数据处理
        GatewayPluginDto gatewayPluginDto = buildUpdateInfo(plugin);
        //更新插件
        return publishPlugin(gatewayPluginDto);

    }

    public ErrorCode batchUpdate(List<BindingPluginDto> plugins) {
        if (CollectionUtils.isEmpty(plugins)) {
            return CommonErrorCode.SUCCESS;
        }
        //校验是否需要执行更新操作
        //数据处理
        List<GatewayPluginDto> gatewayPlugins = plugins.stream().filter(this::filter)
                .map(this::buildUpdateInfo)
                .collect(Collectors.toList());
        //更新插件
        return batchPublishPlugin(gatewayPlugins);
    }

    public ErrorCode batchUpdateUsingRoute(List<RouteDto> routes) {
        if (CollectionUtils.isEmpty(routes)) {
            return CommonErrorCode.SUCCESS;
        }
        List<BindingPluginDto> binds = routes.stream().map(this::getRouteDefaultBindInfo).collect(Collectors.toList());
        return batchUpdate(binds);
    }

    public BindingPluginDto getRouteDefaultBindInfo(RouteDto routeDto) {
        BindingPluginDto bindingPluginDto = new BindingPluginDto(routeDto.getVirtualGwId(),
                BindingObjectTypeEnum.ROUTE.getValue(), routeDto.getId(), "", "");
        bindingPluginDto.setPluginIdList(Lists.newArrayList());
        return bindingPluginDto;
    }

    @Override
    public ErrorCode delete(BindingPluginDto plugin) {
        //校验是否需要执行删除操作
        if (!filter(plugin)) {
            return CommonErrorCode.SUCCESS;
        }
        //数据处理
        GatewayPluginDto gatewayPluginDto = buildDeleteInfo(plugin);
        //删除插件
        return publishPlugin(gatewayPluginDto);
    }

    @Override
    public BindingObjectTypeEnum getBindingObjectType() {
        return BindingObjectTypeEnum.ROUTE;
    }

    private Boolean filter(BindingPluginDto pluginDto) {
        RouteDto route = routeService.getRoute(pluginDto.getVirtualGwId(), pluginDto.getBindingObjectId());
        return ENABLE_STATE.equals(route.getEnableState()) || pluginDto.isEnableRouteOperation();
    }

    private GatewayPluginDto buildCreateInfo(BindingPluginDto pluginDto) {
        //查询路由下所有插件
        List<String> pluginConfigs = getPluginConfigList(pluginDto);
        //添加当前插件
        if (!StringUtils.isEmpty(pluginDto.getPluginConfiguration())) {
            pluginConfigs.add(pluginDto.getPluginConfiguration());
        }
        //数据构建
        return build(pluginDto, pluginConfigs);
    }

    private GatewayPluginDto buildUpdateInfo(BindingPluginDto pluginDto) {
        List<Long> pluginIdList = pluginDto.getPluginIdList();
        if (CollectionUtils.isEmpty(pluginIdList)) {
            return build(pluginDto, Lists.newArrayList());
        }
        //查询路由下所有插件
        List<PluginBindingInfo> pluginBindingInfos = pluginInfoService.getEnablePluginBindingList(pluginDto.getVirtualGwId(),
                pluginDto.getBindingObjectId(), pluginDto.getBindingObjectType());
        //更新当前插件
        pluginBindingInfos.stream().filter(o -> o.getId().equals(pluginIdList.get(0))).findFirst().ifPresent(o -> o.setPluginConfiguration(pluginDto.getPluginConfiguration()));
        List<String> pluginConfigs = pluginBindingInfos.stream().map(PluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
        //数据构建
        return build(pluginDto, pluginConfigs);
    }

    private GatewayPluginDto buildDeleteInfo(BindingPluginDto pluginDto) {
        //查询路由下所有插件
        List<PluginBindingInfo> pluginBindingInfos = pluginInfoService.getEnablePluginBindingList(pluginDto.getVirtualGwId(),
                pluginDto.getBindingObjectId(), pluginDto.getBindingObjectType());
        //删除当前插件
        pluginBindingInfos.removeIf(o -> pluginDto.getPluginIdList().contains(o.getId()));
        List<String> pluginConfigs = pluginBindingInfos.stream().map(PluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
        //数据构建
        return build(pluginDto, pluginConfigs, true);
    }

    //查询当前路由下所有插件
    private List<String> getPluginConfigList(BindingPluginDto pluginDto) {
        List<PluginBindingInfo> pluginBindingInfos = pluginInfoService.getEnablePluginBindingList(pluginDto.getVirtualGwId(),
                pluginDto.getBindingObjectId(), pluginDto.getBindingObjectType());
        return pluginBindingInfos.stream().map(PluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
    }

    private GatewayPluginDto build(BindingPluginDto pluginDto, List<String> pluginConfigs) {
        return build(pluginDto, pluginConfigs, false);
    }

    private GatewayPluginDto build(BindingPluginDto pluginDto, List<String> pluginConfigs,boolean delete){
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(pluginDto.getVirtualGwId());
        RouteDto route = routeService.getRoute(pluginDto.getVirtualGwId(), pluginDto.getBindingObjectId());
        //添加额外插件配置
        addExtraRoutePluginConfig(route,virtualGatewayDto, pluginConfigs, delete);
        GatewayPluginDto gatewayPlugin = buildPlugin(pluginDto, null, pluginConfigs);
        gatewayPlugin.setCode(buildVirtualServiceName(route.getName(), String.valueOf(route.getProjectId()), CommonUtil.genGatewayStrForRoute(virtualGatewayDto)));
        return gatewayPlugin;
    }

    private String buildVirtualServiceName(String apiName, String projectId, String gw) {
        return String.format("%s-%s-%s", apiName, projectId, gw);
    }

    /**
     * 添加额外的路由插件配置
     * 对应插件内容不在插件数据库中存储，同时也不会返回到路由插件列表中
     * 例如：会话保持插件
     *
     * @param route
     * @param toBePublishedPluginList
     * @param delete
     */
    private void addExtraRoutePluginConfig(RouteDto route,VirtualGatewayDto virtualGatewayDto, List<String> toBePublishedPluginList, boolean delete){
        toBePublishedPluginList.addAll(processSessionState(route,virtualGatewayDto,delete));
    }

    /**
     * 设置会话保持等信息(From 服务)
     *
     * @param routeDto
     * @param delete
     */
    private List<String> processSessionState(RouteDto routeDto,VirtualGatewayDto virtualGatewayDto ,boolean delete){
        if (delete){
            return Collections.emptyList();
        }
        if (virtualGatewayDto == null){
            return Collections.emptyList();
        }
        //仅负载均衡形态支持会话保持
        if (!BaseConst.LOAD_BALANCE.equals(virtualGatewayDto.getType())){
            return Collections.emptyList();
        }
        List<ServiceMetaForRouteDto> serviceList = routeDto.getServiceMetaForRouteDtos();
        ServiceProxyDto service = serviceProxyService.get(serviceList.get(0).getServiceId());
        if (service == null) {
            throw new ErrorCodeException(CommonErrorCode.NO_SUCH_SERVICE);
        }
        ServiceTrafficPolicyDto trafficPolicy = service.getTrafficPolicy();
        if (trafficPolicy == null){
            return Collections.emptyList();
        }
        SessionStateDto sessionState = trafficPolicy.getSessionState();
        if (sessionState == null){
            return Collections.emptyList();
        }
        return Lists.newArrayList(JSON.toJSONString(sessionState));
    }
}
