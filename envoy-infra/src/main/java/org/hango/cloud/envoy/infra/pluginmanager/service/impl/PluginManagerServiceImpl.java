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
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginManagerDto;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderDto;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.MODULE_API_PLANE;

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
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            return Collections.emptyList();
        }
        PluginOrderDto pluginManagers = getPluginManagers(virtualGatewayDto);
        if (pluginManagers == null) {
            return Collections.emptyList();
        }
        List<PluginOrderItemDto> plugins = pluginManagers.getPlugins();
        if (CollectionUtils.isEmpty(plugins)) {
            return Collections.emptyList();
        }
        return plugins.stream().map(this::toPluginManagerDto).collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkPluginManager(long virtualGwId, String name, boolean enable) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public boolean updatePluginManager(long virtualGwId, String name, boolean enable) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            logger.info("网关信息为空");
            return false;
        }
        PluginOrderDto pluginManagers = getPluginManagers(virtualGatewayDto);
        List<PluginOrderItemDto> plugins = pluginManagers.getPlugins();
        if (pluginManagers == null || CollectionUtils.isEmpty(plugins)) {
            return false;
        }
        for (PluginOrderItemDto plugin : plugins) {
            if (plugin.getName().equals(name)) {
                plugin.setEnable(enable);
                break;
            }
        }
        return publishPluginManager(pluginManagers, virtualGatewayDto);
    }

    @Override
    public boolean publishPluginManager(VirtualGatewayDto virtualGatewayDto) {
        PluginOrderDto pluginTemplate = getPluginTemplate(virtualGatewayDto);
        if (pluginTemplate == null) {
            logger.warn("不存在对应的插件开关配置，virtualGateway = {} ", virtualGatewayDto);
            return false;
        }
        return publishPluginManager(pluginTemplate, virtualGatewayDto);
    }

    @Override
    public boolean offlinePluginManager(VirtualGatewayDto virtualGatewayDto) {
        PluginOrderDto pluginManagers = rebuildTemplate(null, virtualGatewayDto);
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "DeletePluginOrder");
        params.put("Version", "2019-07-25");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + "/api", JSON.toJSONString(pluginManagers), params, headers, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane下线插件配置接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

    /**
     * 从数据面获取插件配置
     *
     * @param virtualGatewayDto
     * @return
     */
    @Override
    public PluginOrderDto getPluginManagers(VirtualGatewayDto virtualGatewayDto) {
        PluginOrderDto pluginOrderDto = rebuildTemplate(null, virtualGatewayDto);
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "GetPluginOrder");
        params.put("Version", "2019-07-25");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + "/api", JSON.toJSONString(pluginOrderDto), params, headers, MODULE_API_PLANE);

        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("获取网关插件配置失败，返回http status code非2xx，httpStatusCode:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
        String result = jsonObject.getString(BaseConst.RESULT);
        if (StringUtils.isBlank(result)) {
            logger.info("未能找到对应网关的插件配置");
            return null;
        }
        PluginOrderDto pluginOrder = JSON.parseObject(result, PluginOrderDto.class);
        return pluginOrder;
    }

    /**
     * 从数据面获取插件配置模板
     *
     * @param virtualGatewayDto
     * @return
     */
    private PluginOrderDto getPluginTemplate(VirtualGatewayDto virtualGatewayDto) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "GetPluginOrderTemplate");
        params.put("Version", "2019-07-25");
        params.put("GatewayKind", virtualGatewayDto.getType());
        //由于是先创建PluginManger，再创建虚拟网关数据(to db) ,所以当此时， 虚拟网关数据并不存在ConfAddr,需要通过网关获取
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (gatewayDto == null) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpClientResponse response = HttpClientUtil.getRequest(gatewayDto.getConfAddr() + "/api", params, headers, EnvoyConst.MODULE_API_PLANE);
            if (response == null) {
                return null;
            }
            if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
                logger.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return null;
            }
            JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
            if (jsonObject == null) {
                logger.info("未查询到有效数据， Type = {}", virtualGatewayDto.getType());
                return null;
            }
            PluginOrderDto pluginTemplate = JSON.parseObject(jsonObject.getString(BaseConst.RESULT), PluginOrderDto.class);
            if (pluginTemplate == null) {
                return null;
            }
            return pluginTemplate;
        } catch (Exception e) {
            logger.error("调用api-plane发布接口异常，e:", e);
            return null;
        }
    }

    /**
     * 重整插件开关数据
     *
     * @param template
     * @param virtualGatewayDto
     */
    private PluginOrderDto rebuildTemplate(PluginOrderDto template, VirtualGatewayDto virtualGatewayDto) {
        if (template == null) {
            template = new PluginOrderDto();
        }
        if (StringUtils.isBlank(virtualGatewayDto.getGwClusterName())){
            GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
            virtualGatewayDto.setGwClusterName(gatewayDto.getGwClusterName());
        }
        template.setGatewayKind(virtualGatewayDto.getType());
        template.setName(StringUtils.joinWith(BaseConst.SYMBOL_HYPHEN, "gw_cluster", virtualGatewayDto.getGwClusterName(), virtualGatewayDto.getCode()));
        List<PluginOrderItemDto> plugins = template.getPlugins();
        if (CollectionUtils.isEmpty(plugins)) {
            return template;
        }
        plugins.stream().forEach(p -> p.setPort(virtualGatewayDto.getPort()));
        return template;
    }

    /**
     * 数据转换
     *
     * @param item
     * @return
     */
    public PluginManagerDto toPluginManagerDto(PluginOrderItemDto item) {
        logger.warn(JSON.toJSONString(item));
        PluginManagerDto pluginManagerDto = new PluginManagerDto();
        pluginManagerDto.setEnable(item.getEnable());
        pluginManagerDto.setName(item.getName());
        pluginManagerDto.setDisplayName(item.getName());
        return pluginManagerDto;
    }


    private boolean publishPluginManager(PluginOrderDto pluginOrder, VirtualGatewayDto virtualGateway) {
        rebuildTemplate(pluginOrder, virtualGateway);
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "PublishPluginOrder");
        params.put("Version", "2019-07-25");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //由于是先创建PluginManger，再创建虚拟网关数据(to db) ,所以当此时， 虚拟网关数据并不存在ConfAddr,需要通过网关获取
        GatewayDto gatewayDto = gatewayService.get(virtualGateway.getGwId());
        if (gatewayDto == null) {
            return false;
        }
        HttpClientResponse response = HttpClientUtil.postRequest(gatewayDto.getConfAddr() + "/api", JSON.toJSONString(pluginOrder), params, headers, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane发布插件配置接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

}
