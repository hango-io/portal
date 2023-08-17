package org.hango.cloud.envoy.infra.plugin.manager;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.plugin.dto.BasePluginDTO;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;
import org.hango.cloud.envoy.infra.plugin.service.CustomPluginInfoService;
import org.hango.cloud.envoy.infra.plugin.util.Trans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author zhufengwei
 * @Date 2023/7/29
 */
@Component
public class GatewayPluginOperateService extends AbstractPluginOperateService {

    @Autowired
    private CustomPluginInfoService customPluginInfoService;

    @Override
    public ErrorCode create(BindingPluginDto plugin) {
        return publishBasePlugin(build(plugin, plugin.getPluginConfiguration()));
    }

    @Override
    public ErrorCode update(BindingPluginDto plugin) {
        return publishBasePlugin(build(plugin, plugin.getPluginConfiguration()));
    }

    @Override
    public ErrorCode delete(BindingPluginDto plugin) {
        return publishBasePlugin(build(plugin, null));
    }

    @Override
    public BindingObjectTypeEnum getBindingObjectType() {
        return BindingObjectTypeEnum.GATEWAY;
    }


    private BasePluginDTO build(BindingPluginDto bindingPluginInfo, String plugin) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(bindingPluginInfo.getVirtualGwId());
        BasePluginDTO basePluginDTO = new BasePluginDTO();
        basePluginDTO.setPluginType(bindingPluginInfo.getPluginType());
        basePluginDTO.setName(Trans.getPluginManagerName(virtualGatewayDto));
        CustomPluginInfo customPlugin = customPluginInfoService.getCustomPlugin(bindingPluginInfo.getPluginType());
        if (customPlugin != null){
            basePluginDTO.setLanguage(customPlugin.getLanguage());
        }else {
            basePluginDTO.setLanguage("inline");
        }
        basePluginDTO.setPluginConfig(plugin);
        basePluginDTO.setAddr(virtualGatewayDto.getConfAddr());
        return basePluginDTO;
    }
}
