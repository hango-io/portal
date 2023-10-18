package org.hango.cloud.envoy.infra.plugin.manager;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.service.VersionManagerService;
import org.hango.cloud.envoy.infra.plugin.dto.BasePluginDTO;
import org.hango.cloud.envoy.infra.plugin.dto.GatewayPluginDto;
import org.hango.cloud.gdashboard.api.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.PLANE_PORTAL_PATH;
import static org.hango.cloud.common.infra.base.meta.BaseConst.PLANE_VERSION;
import static org.hango.cloud.common.infra.base.meta.BaseConst.VERSION;
import static org.hango.cloud.gdashboard.api.util.Const.ACTION;

/**
 * @Author zhufengwei
 * @Date 2023/7/28
 */
public abstract class AbstractPluginOperateService implements PluginOperateService {

    @Autowired
    private VersionManagerService versionManagerService;

    @Autowired
    IVirtualGatewayInfoService virtualGatewayInfoService;

    protected ErrorCode publishPlugin(GatewayPluginDto plugin) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put(ACTION, "PublishPlugin");
        boolean result = requestForGatewayPlugin(plugin.getAddr(), JSONObject.toJSONString(plugin), params);
        if (!result) {
            return CommonErrorCode.INTERNAL_SERVER_ERROR;
        }
        return CommonErrorCode.SUCCESS;
    }

    protected ErrorCode batchPublishPlugin(List<GatewayPluginDto> plugins) {
        if (CollectionUtils.isEmpty(plugins)){
            return CommonErrorCode.SUCCESS;
        }
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put(ACTION, "BatchPublishPlugin");
        Set<String> addrs = plugins.stream().map(GatewayPluginDto::getAddr).collect(Collectors.toSet());
        if (addrs.size() != NumberUtils.INTEGER_ONE) {
            return CommonErrorCode.NOT_SUPPORT_MULTI_ADDR;
        }
        boolean result = requestForGatewayPlugin(plugins.get(0).getAddr(), JSONObject.toJSONString(plugins), params);
        if (!result) {
            return CommonErrorCode.INTERNAL_SERVER_ERROR;
        }
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 发布网关级插件配置
     */
    protected ErrorCode publishBasePlugin(BasePluginDTO plugin) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put(ACTION, "PublishBasePlugin");
        boolean result = requestForGatewayPlugin(plugin.getAddr(), JSONObject.toJSONString(plugin), params);
        if (!result) {
            return CommonErrorCode.INTERNAL_SERVER_ERROR;
        }
        return CommonErrorCode.SUCCESS;
    }


    private boolean requestForGatewayPlugin(String addr, String plugin, Map<String, Object> params) {
        params.put(VERSION, PLANE_VERSION);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return versionManagerService.publishPluginWithVersionManager(addr + PLANE_PORTAL_PATH, params, headers, plugin, null);
    }

    protected GatewayPluginDto buildPlugin(BindingPluginDto bindingPluginInfo, String code, List<String> pluginList) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(bindingPluginInfo.getVirtualGwId());

        GatewayPluginDto gatewayPlugin = new GatewayPluginDto();
        gatewayPlugin.setAddr(virtualGatewayDto.getConfAddr());
        gatewayPlugin.setPort(virtualGatewayDto.getPort());
        gatewayPlugin.setGwCluster(virtualGatewayDto.getGwClusterName());
        gatewayPlugin.setGateway(virtualGatewayDto.getGwClusterName().toLowerCase() + "-" + virtualGatewayDto.getCode());
        gatewayPlugin.setPlugins(pluginList);
        gatewayPlugin.setBindingObjectId(bindingPluginInfo.getBindingObjectId());
        gatewayPlugin.setBindingObjectType(bindingPluginInfo.getBindingObjectType());
        gatewayPlugin.setCode(code);
        gatewayPlugin.setPluginScope(bindingPluginInfo.getBindingObjectType());
        return gatewayPlugin;
    }


    public abstract BindingObjectTypeEnum getBindingObjectType();
}
