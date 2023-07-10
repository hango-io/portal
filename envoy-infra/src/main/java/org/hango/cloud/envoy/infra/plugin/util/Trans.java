package org.hango.cloud.envoy.infra.plugin.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.plugin.dto.PluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.base.util.EnvoyCommonUtil;
import org.hango.cloud.envoy.infra.plugin.dto.CustomPluginDTO;
import org.hango.cloud.envoy.infra.plugin.dto.CustomPluginInfoDto;
import org.hango.cloud.envoy.infra.plugin.dto.DescribeCustomPluginDto;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;
import org.hango.cloud.envoy.infra.plugin.metas.PluginSource;
import org.hango.cloud.envoy.infra.plugin.metas.PluginType;
import org.hango.cloud.envoy.infra.pluginmanager.dto.EngineRuleDTO;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;

import static org.hango.cloud.common.infra.base.meta.BaseConst.RIDER_PLUGIN;

/**
 * @Author zhufengwei
 * @Date 2023/7/5
 */
public class Trans {
    private static final String DEFAULT_AUTHOR = "system";

    public static PluginInfo parsePlugin(String plugin) {
        if (StringUtils.isBlank(plugin)) {
            return null;
        }
        JSONObject pluginInfo = JSONObject.parseObject(plugin);
        PluginInfo envoyPluginInfo = new PluginInfo();
        envoyPluginInfo.setPluginName(pluginInfo.getString("displayName"));
        if (StringUtils.isBlank(pluginInfo.getString("displayName"))) {
            envoyPluginInfo.setPluginName(pluginInfo.getString("name"));
        }
        //对应插件唯一标志，英文标识
        envoyPluginInfo.setPluginType(pluginInfo.getString("name"));
        envoyPluginInfo.setPluginScope(pluginInfo.getString("pluginScope"));
        envoyPluginInfo.setInstructionForUse(pluginInfo.getString("instructionForUse"));
        envoyPluginInfo.setCategoryKey(pluginInfo.getString("categoryKey"));
        envoyPluginInfo.setPluginGuidance(pluginInfo.getString("pluginGuidance"));
        return envoyPluginInfo;
    }


    public static PluginDto fromMeta(PluginInfo pluginInfo) {
        if (null == pluginInfo) {
            return null;
        }
        PluginDto pluginDto = new PluginDto();
        pluginDto.setAuthor(DEFAULT_AUTHOR);
        pluginDto.setPluginSource(PluginSource.SYSTEM.getName());
        pluginDto.setPluginName(pluginInfo.getPluginName());
        pluginDto.setPluginType(pluginInfo.getPluginType());
        pluginDto.setPluginScope(pluginInfo.getPluginScope());
        pluginDto.setPluginSchema(pluginInfo.getPluginSchema());
        pluginDto.setInstructionForUse(pluginInfo.getInstructionForUse());
        pluginDto.setCategoryKey(pluginInfo.getCategoryKey());
        pluginDto.setCategoryName(PluginType.getByName(pluginInfo.getCategoryKey()));
        pluginDto.setPluginGuidance(pluginInfo.getPluginGuidance());
        return pluginDto;
    }

    public static PluginDto fromCustomPluginMeta(CustomPluginInfo customPluginInfo){
        if(customPluginInfo == null){
            return null;
        }
        PluginDto pluginDto = new PluginDto();
        pluginDto.setAuthor(customPluginInfo.getAuthor());
        pluginDto.setPluginType(customPluginInfo.getPluginType());
        String pluginName = customPluginInfo.getPluginName();
        if (StringUtils.isBlank(pluginName)){
            pluginName = customPluginInfo.getPluginType();
        }
        pluginDto.setPluginName(pluginName);
        pluginDto.setPluginScope(customPluginInfo.getPluginScope());
        pluginDto.setCategoryKey(customPluginInfo.getPluginCategory());
        pluginDto.setCategoryName(PluginType.getByName(customPluginInfo.getPluginCategory()));
        pluginDto.setInstructionForUse(customPluginInfo.getDescription());
        pluginDto.setPluginSchema(customPluginInfo.getPluginSchema());
        pluginDto.setPluginSource(PluginSource.CUSTOM.getName());
        return pluginDto;
    }

    public static CustomPluginInfo customPluginDto2MetaInfo(CustomPluginInfoDto customPluginInfoDto) {
        return CustomPluginInfo.builder()
                .pluginName(customPluginInfoDto.getPluginName())
                .pluginType(customPluginInfoDto.getPluginType())
                .description(customPluginInfoDto.getDescription())
                .language(customPluginInfoDto.getLanguage())
                .sourceType(customPluginInfoDto.getSourceType())
                .pluginCategory(customPluginInfoDto.getPluginCategory())
                .pluginScope(customPluginInfoDto.getPluginScope())
                .author(customPluginInfoDto.getAuthor())
                .pluginSchema(customPluginInfoDto.getSchemaContent())
                .pluginContent(EnvoyCommonUtil.file2Str(customPluginInfoDto.getSourceContent()))
                .build();
    }

    public static CustomPluginDTO customPluginInfo2ApiPlaneDto(CustomPluginInfo customPluginInfo) {
        return CustomPluginDTO.builder()
                .pluginName(customPluginInfo.getPluginType())
                .language(customPluginInfo.getLanguage())
                .pluginContent(customPluginInfo.getPluginContent())
                .schema(customPluginInfo.getPluginSchema())
                .build();
    }

    public static DescribeCustomPluginDto customPluginInfo2Dto(CustomPluginInfo customPluginInfo) {
        if (customPluginInfo == null) {
            return null;
        }
        DescribeCustomPluginDto describeCustomPluginDto = new DescribeCustomPluginDto();
        describeCustomPluginDto.setId(customPluginInfo.getId());
        describeCustomPluginDto.setPluginType(customPluginInfo.getPluginType());
        describeCustomPluginDto.setPluginName(customPluginInfo.getPluginName());
        describeCustomPluginDto.setDescription(customPluginInfo.getDescription());
        describeCustomPluginDto.setLanguage(customPluginInfo.getLanguage());
        describeCustomPluginDto.setSourceType(customPluginInfo.getSourceType());
        describeCustomPluginDto.setPluginCategory(customPluginInfo.getPluginCategory());
        describeCustomPluginDto.setPluginStatus(customPluginInfo.getPluginStatus());
        describeCustomPluginDto.setPluginScope(customPluginInfo.getPluginScope());
        describeCustomPluginDto.setAuthor(customPluginInfo.getAuthor());
        describeCustomPluginDto.setUpdateTime(customPluginInfo.getUpdateTime());
        describeCustomPluginDto.setCreateTime(customPluginInfo.getCreateTime());
        describeCustomPluginDto.setSchemaContent(customPluginInfo.getPluginSchema());
        describeCustomPluginDto.setSourceContent(customPluginInfo.getPluginContent());
        return describeCustomPluginDto;
    }

    public static PluginOrderItemDto buildEngineRulePlugin(EngineRuleDTO engineRuleDTO, VirtualGatewayDto virtualGatewayDto) {
        JSONObject fileName = new JSONObject();
        fileName.put("filename",engineRuleDTO.getFilename());
        JSONObject local = new JSONObject();
        local.put("local",fileName);
        JSONObject plugin = new JSONObject();
        plugin.put("code",local);
        if (engineRuleDTO.getConfig() != null) {
            plugin.put("config",engineRuleDTO.getConfig());
        }
        plugin.put("name",engineRuleDTO.getName());
        JSONObject packagePath = new JSONObject();
        packagePath.put("package_path","/usr/local/lib/rider/?/init.lua;/usr/local/lib/rider/?.lua;");
        plugin.put("vm_config",packagePath);
        JSONObject settings = new JSONObject();
        settings.put("plugin",plugin);
        JSONObject inline = new JSONObject();
        inline.put("settings",settings);
        PluginOrderItemDto pluginOrderItemDto = new PluginOrderItemDto();
        pluginOrderItemDto.setEnable(true);
        pluginOrderItemDto.setName(RIDER_PLUGIN);
        pluginOrderItemDto.setInline(inline);
        pluginOrderItemDto.setPort(virtualGatewayDto.getPort());
        pluginOrderItemDto.setOperate(engineRuleDTO.getOperate());
        pluginOrderItemDto.setSubName(engineRuleDTO.getName());
        return pluginOrderItemDto;
    }

    public static PluginOrderItemDto builderRiderItem(String pluginType, String operate, String language, Integer port){
        PluginOrderItemDto pluginOrderItemDto = new PluginOrderItemDto();
        pluginOrderItemDto.setEnable(true);
        pluginOrderItemDto.setName(pluginType);
        pluginOrderItemDto.setPort(port);
        pluginOrderItemDto.setOperate(operate);
        JSONObject rider = new JSONObject();
        rider.put("pluginName", pluginType);
        rider.put("url", "file://usr/local/lib/rider/plugins/" + pluginType + "." + language);
        pluginOrderItemDto.setRider(rider);
        return pluginOrderItemDto;
    }

}
