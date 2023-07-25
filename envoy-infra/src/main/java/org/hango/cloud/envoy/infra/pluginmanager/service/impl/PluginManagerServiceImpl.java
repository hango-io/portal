package org.hango.cloud.envoy.infra.pluginmanager.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.plugin.util.Trans;
import org.hango.cloud.envoy.infra.pluginmanager.dto.EngineRuleDTO;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginManagerDto;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderDto;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;
import org.hango.cloud.envoy.infra.pluginmanager.handler.PluginHandler;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.*;
import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.MODULE_API_PLANE;
import static org.hango.cloud.gdashboard.api.util.Const.ACTION;
import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_INGRESS;
import static org.hango.cloud.gdashboard.api.util.Const.VERSION;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/24
 */
@Service
public class PluginManagerServiceImpl implements IPluginManagerService {

    private static final Logger logger = LoggerFactory.getLogger(PluginManagerServiceImpl.class);

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IGatewayService gatewayService;

    @Override
    public List<PluginManagerDto> getPluginManager(long virtualGwId) {
        List<PluginOrderItemDto> plugins = getPluginOrder(virtualGwId).getPlugins();
        if (CollectionUtils.isEmpty(plugins)) {
            return Collections.emptyList();
        }

        return plugins.stream().filter(PluginHandler::pluginFilter).map(this::toPluginManagerDto).collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkPluginManager(long virtualGwId, String name, boolean enable) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 从数据面获取pluginManager
     */
    @Override
    public PluginOrderDto getPluginOrder(Long vgId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(vgId);
        if (virtualGatewayDto == null) {
            return new PluginOrderDto();
        }
        PluginOrderDto query = buildPluginOrder(virtualGatewayDto);
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put(ACTION, "GetPluginOrder");
        params.put(VERSION, PLANE_VERSION);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + PLANE_PLUGIN_PATH, JSON.toJSONString(query), params, headers, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("获取网关插件配置失败，返回http status code非2xx，httpStatusCode:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return new PluginOrderDto();
        }
        JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
        String result = jsonObject.getString(BaseConst.RESULT);
        if (StringUtils.isBlank(result)) {
            logger.info("调用api-plane查询插件配置失败，返回结果为空");
            return new PluginOrderDto();
        }
        return JSON.parseObject(result, PluginOrderDto.class);
    }


    @Override
    public Boolean updateCustomPluginStatus(VirtualGatewayDto virtualGatewayDto, String pluginType, String operate) {
        EngineRuleDTO engineRuleDTO = new EngineRuleDTO();
        engineRuleDTO.setOperate(operate);
        engineRuleDTO.setFilename("/usr/local/lib/rider/plugins/" + pluginType + ".lua");
        engineRuleDTO.setName(pluginType);
        PluginOrderItemDto pluginOrderItemDto = Trans.buildEngineRulePlugin(engineRuleDTO, virtualGatewayDto);
        return updatePluginManager(virtualGatewayDto.getId(), pluginOrderItemDto);
    }

    @Override
    public boolean updatePluginManager(long virtualGwId, PluginOrderItemDto itemDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        Map<String, Object> params = Maps.newHashMap();
        params.put(ACTION, "UpdatePluginOrderItem");
        params.put(VERSION, PLANE_VERSION);

        PluginOrderDto pluginOrderDto = buildPluginOrder(virtualGatewayDto);
        pluginOrderDto.setPlugins(Collections.singletonList(itemDto));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + PLANE_PLUGIN_PATH, JSON.toJSONString(pluginOrderDto), params, headers, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane下线插件配置接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }
    @Override
    public boolean offlinePluginManager(VirtualGatewayDto virtualGatewayDto) {
        PluginOrderDto pluginManagers = buildPluginOrder(virtualGatewayDto);
        Map<String, Object> params = Maps.newHashMap();
        params.put(ACTION, "DeletePluginOrder");
        params.put(VERSION, PLANE_VERSION);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + PLANE_PLUGIN_PATH, JSON.toJSONString(pluginManagers), params, headers, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane下线插件配置接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

    /**
     * 从数据面获取插件配置
     */
    @Override
    public List<PluginOrderItemDto> getPluginManagers(Long vgId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(vgId);
        if (virtualGatewayDto == null) {
            return new ArrayList<>();
        }
        PluginOrderDto query = buildPluginOrder(virtualGatewayDto);
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put(ACTION, "GetPluginOrder");
        params.put(VERSION, PLANE_VERSION);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + PLANE_PLUGIN_PATH, JSON.toJSONString(query), params, headers, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("获取网关插件配置失败，返回http status code非2xx，httpStatusCode:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return new ArrayList<>();
        }
        JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
        String result = jsonObject.getString(BaseConst.RESULT);
        if (StringUtils.isBlank(result)) {
            logger.info("调用api-plane查询插件配置失败，返回结果为空");
            return new ArrayList<>();
        }
        PluginOrderDto pluginOrderDto = JSON.parseObject(result, PluginOrderDto.class);
        return pluginOrderDto.getPlugins();
    }

    @Override
    public boolean publishPluginManager(VirtualGatewayDto virtualGateway) {
        //4层网关不下发插件配置
        if (StringUtils.equalsAnyIgnoreCase(virtualGateway.getProtocol(),BaseConst.SCHEME_TCP,BaseConst.SCHEME_UDP)) {
            return true;
        }
        PluginOrderDto pluginOrderDto = buildPluginOrder(virtualGateway);
        Map<String, Object> params = Maps.newHashMap();
        params.put(ACTION, "PublishPluginOrder");
        params.put(VERSION, PLANE_VERSION);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //由于是先创建PluginManger，再创建虚拟网关数据(to db) ,所以当此时， 虚拟网关数据并不存在ConfAddr,需要通过网关获取
        GatewayDto gatewayDto = gatewayService.get(virtualGateway.getGwId());
        if (gatewayDto == null) {
            return false;
        }
        HttpClientResponse response = HttpClientUtil.postRequest(gatewayDto.getConfAddr() + PLANE_PLUGIN_PATH, JSON.toJSONString(pluginOrderDto), params, headers, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane发布插件配置接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }
    /**
     * 构建PluginOrder参数
     *
     */
    private PluginOrderDto buildPluginOrder(VirtualGatewayDto virtualGatewayDto) {
        String gwClusterName = virtualGatewayDto.getGwClusterName();
        if (StringUtils.isBlank(gwClusterName)){
            GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
            gwClusterName = gatewayDto.getGwClusterName();
        }
        PluginOrderDto pluginOrderDto = new PluginOrderDto();
        pluginOrderDto.setGwCluster(gwClusterName);
        pluginOrderDto.setGatewayKind(virtualGatewayDto.getType());
        String name;
        //多个ingress共用plm
        if (KUBERNETES_INGRESS.equals(virtualGatewayDto.getType())){
            name = StringUtils.joinWith(BaseConst.SYMBOL_HYPHEN, "ingress", gwClusterName);
        }else {
            name = StringUtils.joinWith(BaseConst.SYMBOL_HYPHEN, "gw-cluster", gwClusterName, virtualGatewayDto.getCode());
        }
        pluginOrderDto.setName(name);
        pluginOrderDto.setPort(virtualGatewayDto.getPort());
        return pluginOrderDto;
    }

    /**
     * 数据转换
     *
     * @param item
     * @return
     */
    public PluginManagerDto toPluginManagerDto(PluginOrderItemDto item) {
        PluginManagerDto pluginManagerDto = new PluginManagerDto();
        pluginManagerDto.setEnable(item.getEnable() != null && item.getEnable());
        pluginManagerDto.setName(item.getName());
        pluginManagerDto.setDisplayName(item.getName());
        return pluginManagerDto;
    }
}
