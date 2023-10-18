package org.hango.cloud.common.infra.plugin.convert;

import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingQueryDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;

import static org.hango.cloud.common.infra.base.meta.BaseConst.ENABLE_STATE;

/**
 * @Author zhufengwei
 * @Date 2023/7/25
 */
@Service
public class PluginInfoConvertService {

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IPluginTemplateService pluginTemplateService;

    public static PluginBindingInfoQuery trans(PluginBindingQueryDto pluginBindingQueryDto){
        PluginBindingInfoQuery pluginBindingInfoQuery = PluginBindingInfoQuery.builder()
                .virtualGwId(pluginBindingQueryDto.getVirtualGwId())
                .bindingObjectId(pluginBindingQueryDto.getBindingObjectId())
                .bindingObjectType(pluginBindingQueryDto.getBindingObjectType())
                .pattern(pluginBindingQueryDto.getPattern())
                .build();
        if (!StringUtils.hasText(pluginBindingQueryDto.getBindingObjectType())) {
            //网关级插件默认不展示
            pluginBindingInfoQuery.setBindingObjectTypes(Arrays.asList(BindingObjectTypeEnum.GLOBAL.getValue(), BindingObjectTypeEnum.HOST.getValue(), BindingObjectTypeEnum.ROUTE.getValue()));
        }
        pluginBindingInfoQuery.setLimit(pluginBindingQueryDto.getLimit());
        pluginBindingInfoQuery.setOffset(pluginBindingQueryDto.getOffset());
        pluginBindingInfoQuery.setSortByKey(pluginBindingQueryDto.getSortByKey());
        pluginBindingInfoQuery.setSortByValue(pluginBindingQueryDto.getSortByValue());
        if (BindingObjectTypeEnum.GATEWAY.getValue().equals(pluginBindingQueryDto.getBindingObjectType())) {
            // 网关级别插件不需要通过项目过滤
            pluginBindingInfoQuery.setProjectId(null);
        } else {
            pluginBindingInfoQuery.setProjectId(ProjectTraceHolder.getProId());
        }

        return pluginBindingInfoQuery;
    }

    public PluginBindingInfo trans(PluginBindingDto pluginBindingDto){
        PluginBindingInfo bindingInfo = new PluginBindingInfo();
        bindingInfo.setProjectId(ProjectTraceHolder.getProId());
        bindingInfo.setVirtualGwId(pluginBindingDto.getVirtualGwId());
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(pluginBindingDto.getVirtualGwId());
        bindingInfo.setGwType(virtualGatewayDto.getGwType());
        bindingInfo.setPluginConfiguration(pluginBindingDto.getPluginConfiguration());
        bindingInfo.setBindingObjectType(pluginBindingDto.getBindingObjectType());
        bindingInfo.setBindingObjectId(String.valueOf(pluginBindingDto.getBindingObjectId()));
        bindingInfo.setPluginType(pluginBindingDto.getPluginType());
        bindingInfo.setPluginName(pluginBindingDto.getPluginName());
        bindingInfo.setBindingStatus(ENABLE_STATE);
        bindingInfo.setTemplateId(pluginBindingDto.getTemplateId());
        bindingInfo.setTemplateVersion(pluginBindingDto.getTemplateVersion());
        return bindingInfo;
    }

    public void fillPluginInfo(PluginBindingDto pluginBindingDto){
        if (BindingObjectTypeEnum.GLOBAL.getValue().equals(pluginBindingDto.getBindingObjectType())) {
            pluginBindingDto.setBindingObjectId(String.valueOf(ProjectTraceHolder.getProId()));
        }
        if (pluginBindingDto.getTemplateId() == null || pluginBindingDto.getTemplateId() <= 0){
            pluginBindingDto.setTemplateId(0L);
            return;
        }
        PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(pluginBindingDto.getTemplateId());
        if (pluginTemplateDto != null) {
            pluginBindingDto.setTemplateVersion(pluginTemplateDto.getTemplateVersion());
            pluginBindingDto.setPluginConfiguration(pluginTemplateDto.getPluginConfiguration());
        }
    }
}
