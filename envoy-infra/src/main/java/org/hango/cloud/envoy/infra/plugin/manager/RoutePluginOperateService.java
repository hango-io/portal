package org.hango.cloud.envoy.infra.plugin.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.plugin.dto.GatewayPluginDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
            log.error("pluginIdList is empty");
            throw new IllegalArgumentException("插件id为空");
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
        return build(pluginDto, pluginConfigs);
    }

    //查询当前路由下所有插件
    private List<String> getPluginConfigList(BindingPluginDto pluginDto) {
        List<PluginBindingInfo> pluginBindingInfos = pluginInfoService.getEnablePluginBindingList(pluginDto.getVirtualGwId(),
                pluginDto.getBindingObjectId(), pluginDto.getBindingObjectType());
        return pluginBindingInfos.stream().map(PluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
    }

    private GatewayPluginDto build(BindingPluginDto pluginDto, List<String> pluginConfigs){
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(pluginDto.getVirtualGwId());
        GatewayPluginDto gatewayPlugin = buildPlugin(pluginDto, null, pluginConfigs);
        RouteDto route = routeService.getRoute(pluginDto.getVirtualGwId(), pluginDto.getBindingObjectId());;
        gatewayPlugin.setCode(buildVirtualServiceName(route.getName(), String.valueOf(route.getProjectId()), CommonUtil.genGatewayStrForRoute(virtualGatewayDto)));
        return gatewayPlugin;
    }

    private String buildVirtualServiceName(String apiName, String projectId, String gw) {
        return String.format("%s-%s-%s", apiName, projectId, gw);
    }
}
