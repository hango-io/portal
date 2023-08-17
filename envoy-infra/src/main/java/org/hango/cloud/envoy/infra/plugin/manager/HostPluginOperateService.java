package org.hango.cloud.envoy.infra.plugin.manager;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.envoy.infra.plugin.dto.GatewayPluginDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/7/29
 */
@Component
public class HostPluginOperateService extends AbstractPluginOperateService{

    @Autowired
    IDomainInfoService domainInfoService;


    @Override
    public ErrorCode create(BindingPluginDto plugin) {
        return publishPlugin(build(plugin, plugin.getPluginConfiguration()));
    }

    @Override
    public ErrorCode update(BindingPluginDto plugin) {
        return publishPlugin(build(plugin, plugin.getPluginConfiguration()));
    }

    @Override
    public ErrorCode delete(BindingPluginDto plugin) {
        return publishPlugin(build(plugin, null));
    }

    @Override
    public BindingObjectTypeEnum getBindingObjectType() {
        return BindingObjectTypeEnum.HOST;
    }

    protected GatewayPluginDto build(BindingPluginDto pluginDto, String pluginConfig){
        //插件数据构建
        String code = pluginDto.getBindingObjectType() + "-" + pluginDto.getBindingObjectId() + "-" + pluginDto.getPluginType();
        List<String> pluginConfigList = new ArrayList<>();
        if (StringUtils.hasText(pluginConfig)) {
            pluginConfigList.add(pluginConfig);
        }
        GatewayPluginDto gatewayPluginDto = buildPlugin(pluginDto, code, pluginConfigList);
        //设置域名
        DomainInfoDTO domainInfoDTO = domainInfoService.get(pluginDto.getBindingObjectId());
        List<String> hosts = Collections.singletonList(domainInfoDTO.getHost());
        if (!CollectionUtils.isEmpty(hosts)) {
            gatewayPluginDto.setHosts(hosts);
        }
        return gatewayPluginDto;
    }
}
