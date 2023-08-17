package org.hango.cloud.envoy.infra.plugin.manager;

import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.hango.cloud.envoy.infra.plugin.dto.GatewayPluginDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/7/29
 */
@Component
public class ProjectPluginOperateService extends HostPluginOperateService {

    @Autowired
    private IVirtualGatewayProjectService virtualGatewayProjectService;

    @Override
    public BindingObjectTypeEnum getBindingObjectType() {
        return BindingObjectTypeEnum.GLOBAL;
    }

    @Override
    protected GatewayPluginDto build(BindingPluginDto pluginDto, String pluginConfig){
        PermissionScopeDto project = virtualGatewayProjectService.getProjectScope(pluginDto.getBindingObjectId());
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(pluginDto.getVirtualGwId());
        String code = project.getPermissionScopeEnName() + "-" + pluginDto.getBindingObjectId() + "-" + virtualGatewayDto.getCode() + "-" + pluginDto.getPluginType();
        List<String> pluginConfigList = new ArrayList<>();
        if (StringUtils.hasText(pluginConfig)) {
            pluginConfigList.add(pluginConfig);
        }
        GatewayPluginDto gatewayPluginDto = buildPlugin(pluginDto, code, pluginConfigList);
        List<String> hosts = domainInfoService.getHosts(pluginDto.getBindingObjectId(), pluginDto.getVirtualGwId());
        if (!CollectionUtils.isEmpty(hosts)) {
            gatewayPluginDto.setHosts(hosts);
        }
        return gatewayPluginDto;
    }
}
