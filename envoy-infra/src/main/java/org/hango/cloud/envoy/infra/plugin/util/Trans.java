package org.hango.cloud.envoy.infra.plugin.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.plugin.dto.PluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginUpdateDto;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.base.util.EnvoyCommonUtil;
import org.hango.cloud.envoy.infra.plugin.dto.CustomPluginInfoDto;
import org.hango.cloud.envoy.infra.plugin.dto.DescribeCustomPluginDto;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;
import org.hango.cloud.envoy.infra.plugin.metas.PluginSource;
import org.hango.cloud.envoy.infra.plugin.metas.PluginType;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;

import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.FILE;
import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_INGRESS;

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
                .sourceUrl(customPluginInfoDto.getSourceUrl())
                .secretName(customPluginInfoDto.getSecretName())
                .author(customPluginInfoDto.getAuthor())
                .pluginSchema(customPluginInfoDto.getSchemaContent())
                .pluginContent(EnvoyCommonUtil.file2Str(customPluginInfoDto.getSourceContent()))
                .build();
    }


    public static void merge(CustomPluginInfo source, PluginUpdateDto update) {
        source.setPluginName(update.getPluginName());
        source.setDescription(update.getDescription());
        source.setPluginScope(update.getPluginScope());
        source.setAuthor(update.getAuthor());
        source.setSourceType(update.getSourceType());
        source.setPluginSchema(update.getSchemaContent());
        source.setLanguage(update.getLanguage());

        source.setSourceUrl(update.getSourceUrl());
        source.setSecretName(update.getSecretName());

        String content = EnvoyCommonUtil.file2Str(update.getSourceContent());
        source.setPluginContent(content);
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
        describeCustomPluginDto.setSourceUrl(customPluginInfo.getSourceUrl());
        describeCustomPluginDto.setSecretName(customPluginInfo.getSecretName());
        describeCustomPluginDto.setPluginCategory(customPluginInfo.getPluginCategory());
        describeCustomPluginDto.setPluginStatus(customPluginInfo.getPluginStatus());
        describeCustomPluginDto.setPluginScope(customPluginInfo.getPluginScope());
        describeCustomPluginDto.setAuthor(customPluginInfo.getAuthor());
        describeCustomPluginDto.setCreateTime(customPluginInfo.getCreateTime());
        describeCustomPluginDto.setUpdateTime(customPluginInfo.getUpdateTime());
        describeCustomPluginDto.setSchemaContent(customPluginInfo.getPluginSchema());
        describeCustomPluginDto.setSourceContent(customPluginInfo.getPluginContent());
        return describeCustomPluginDto;
    }

    public static PluginOrderItemDto builderCustomPluginItem(CustomPluginInfo customPluginInfo){
        PluginOrderItemDto pluginOrderItemDto = new PluginOrderItemDto();
        String pluginType = customPluginInfo.getPluginType();
        pluginOrderItemDto.setEnable(false);
        pluginOrderItemDto.setName(pluginType);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pluginName", pluginType);
        String url;
        if (FILE.equals(customPluginInfo.getSourceType())){
            url = "file://usr/local/lib/rider/plugins/" + getPluginNmae(pluginType, customPluginInfo.getLanguage());
        }else {
            url = "oci://" + customPluginInfo.getSourceUrl();
            jsonObject.put("imagePullSecretName", customPluginInfo.getSecretName());
        }
        jsonObject.put("url", url);
        if ("lua".equals(customPluginInfo.getLanguage())){
            pluginOrderItemDto.setRider(jsonObject);
        }else {
            pluginOrderItemDto.setWasm(jsonObject);
        }
        return pluginOrderItemDto;
    }

    public static String getPluginNmae(String pluginType, String language){
        return pluginType + "." + language;
    }

    public static String getPluginManagerName(VirtualGatewayDto virtualGatewayDto) {
        //多个ingress共用plm
        String name;
        if (KUBERNETES_INGRESS.equals(virtualGatewayDto.getType())){
            name =  StringUtils.joinWith(BaseConst.SYMBOL_HYPHEN, "ingress", virtualGatewayDto.getGwClusterName());
        }else {
            name =  StringUtils.joinWith(BaseConst.SYMBOL_HYPHEN, "gw-cluster", virtualGatewayDto.getGwClusterName(), virtualGatewayDto.getCode());
        }
        return name.replace("_", "-");
    }
}
