package org.hango.cloud.envoy.infra.plugin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.dto.PluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.meta.PluginConstant;
import org.hango.cloud.envoy.infra.base.service.VersionManagerService;
import org.hango.cloud.envoy.infra.plugin.dao.ICustomPluginInfoDao;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfoQuery;
import org.hango.cloud.envoy.infra.plugin.metas.PluginType;
import org.hango.cloud.envoy.infra.plugin.service.CustomPluginInfoService;
import org.hango.cloud.envoy.infra.plugin.service.IEnvoyPluginInfoService;
import org.hango.cloud.envoy.infra.plugin.util.Trans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.*;
import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.MODULE_API_PLANE;
import static org.hango.cloud.gdashboard.api.util.Const.ACTION;

/**
 * @author xin li
 * @date 2022/9/21 15:09
 */
@Service
public class EnvoyPluginInfoServiceImpl implements IEnvoyPluginInfoService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyPluginInfoServiceImpl.class);

    @Autowired
    private IPluginInfoService pluginInfoService;
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IRouteService routeRuleProxyService;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private DomainInfoMapper domainInfoMapper;

    @Autowired
    private IVirtualGatewayProjectService virtualGatewayProjectService;

    @Autowired
    private VersionManagerService versionManagerService;

    @Autowired
    private ICustomPluginInfoDao customPluginInfoDao;

    @Autowired
    private CustomPluginInfoService customPluginInfoService;

    public static final List<String> GATEWAY_PROPERTIES_PLUGINS = Arrays.asList("basic-rbac", "dynamic-downgrade");

    @Override
    public ErrorCode checkDescribePlugin(long virtualGwId) {
        if (0 < virtualGwId) {
            VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
            if (null == virtualGateway) {
                logger.error("插件流程查询指定的网关不存在! virtualGwId:{}", virtualGwId);
                return CommonErrorCode.NO_SUCH_GATEWAY;
            }
        }
        // 获取插件getPluginInfo可以不传gwId，默认值为0，此处返回成功（后面会处理所有网关的场景）
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<PluginDto> getPluginInfoList(long virtualGwId, String pluginScope) {
        List<? extends VirtualGatewayDto> virtualGateways = getVirtualGatewayDtos(virtualGwId);
        if (CollectionUtils.isEmpty(virtualGateways)) {
            logger.error("网关信息不存在! virtualGwId:{}", virtualGwId);
            return Collections.emptyList();
        }
        //查询系统插件
        List<PluginDto> plugins = virtualGateways.stream()
                .filter(virtualGateway -> EnvoyConst.ENVOY_GATEWAY_TYPE.equals(virtualGateway.getGwType()))
                .map(this::getSystemPluginInfos)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .map(Trans::fromMeta)
                .collect(Collectors.toList());
        //查询自定义插件
        List<PluginDto> customPluginInfos = customPluginInfoDao.findAll().stream()
                .filter(pluginDto-> ONLINE_STATE.equals(pluginDto.getPluginStatus()))
                .map(Trans::fromCustomPluginMeta)
                .peek(pluginDto -> pluginDto.setPluginSchema(null))
                .collect(Collectors.toList());
        plugins.addAll(customPluginInfos);
        //根据pluginScope进行过滤/排序
        return plugins.stream()
                .filter(item -> filter(virtualGwId, pluginScope, item))
                .distinct()
                .sorted(Comparator.comparing(p -> PluginType.getOrder(p.getCategoryKey())))
                .collect(Collectors.toList());
    }


    private boolean filter(long virtualGwId,String pluginScope, PluginDto item) {
        //未传虚拟网关，不允许展示网关属性插件
        if (virtualGwId <= 0 && GATEWAY_PROPERTIES_PLUGINS.contains(item.getPluginType())){
            return false;
        }
        if (StringUtils.isBlank(item.getPluginScope())) {
            return false;
        }
        if (StringUtils.isBlank(pluginScope)) {
            return true;
        }
        // 全局插件与host插件一致，此处将host直接当做global处理
        if (pluginScope.equals(PluginConstant.PLUGIN_SCOPE_HOST)) {
            pluginScope = PluginConstant.PLUGIN_SCOPE_GLOBAL;
        }
        Set<String> pluginScopeSet = Arrays.stream(item.getPluginScope().split(",")).map(String::trim).collect(Collectors.toSet());
        return pluginScopeSet.contains(pluginScope);
    }


    public List<PluginInfo> getSystemPluginInfos(VirtualGatewayDto virtualGateway) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(ACTION, "GetPluginList");
        params.put(VERSION, PLANE_VERSION);
        params.put("GatewayKind", virtualGateway.getType());
        HttpClientResponse response = HttpClientUtil.getRequest(virtualGateway.getConfAddr() + PLANE_PLUGIN_PATH, params, MODULE_API_PLANE);

        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane查询插件列表接口失败，返回http status code非2xx，httpStatusCoed:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return Lists.newArrayList();
        }
        JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
        if (jsonObject == null) {
            logger.info("未查询到有效数据");
            return new ArrayList<>();
        }
        List<String> plugins = JSONArray.parseArray(jsonObject.getString("Plugins"), String.class);

        return plugins.stream().map(Trans::parsePlugin).collect(Collectors.toList());
    }

    @Override
    public PluginDto getPluginInfo(long virtualGwId, String pluginType) {
        List<? extends VirtualGatewayDto> virtualGateways = getVirtualGatewayDtos(virtualGwId);
        if (CollectionUtils.isEmpty(virtualGateways)) {
            logger.error("网关信息不存在! virtualGwId:{}", virtualGwId);
            return null;
        }

        for (VirtualGatewayDto virtualGateway : virtualGateways) {
            PluginDto pluginInfo = getPluginInfo(virtualGateway, pluginType);
            if (null != pluginInfo) {
                return pluginInfo;
            }
        }
        return null;
    }

    private PluginDto getPluginInfo(VirtualGatewayDto virtualGateway, String pluginType){
        if (!EnvoyConst.ENVOY_GATEWAY_TYPE.equals(virtualGateway.getGwType())) {
            return null;
        }
        CustomPluginInfoQuery query = CustomPluginInfoQuery.builder().pluginType(pluginType).build();
        List<CustomPluginInfo> customPluginInfoList = customPluginInfoDao.getCustomPluginInfoList(query);
        if (!CollectionUtils.isEmpty(customPluginInfoList)){
            //查询自定义插件
            return Trans.fromCustomPluginMeta(customPluginInfoList.get(0));
        }
        //查询系统插件
        return Trans.fromMeta(getEnvoyPluginInfo(virtualGateway, pluginType));
    }



    private List<? extends VirtualGatewayDto> getVirtualGatewayDtos(long virtualGwId){
        if (virtualGwId == 0){
            return virtualGatewayInfoService.findAll();
        }
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        return virtualGateway == null ? new ArrayList<>() : Collections.singletonList(virtualGateway);
    }


    private PluginInfo getEnvoyPluginInfo(VirtualGatewayDto virtualGateway, String pluginType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(ACTION, "GetPluginDetail");
        params.put(VERSION, PLANE_VERSION);
        params.put("Name", pluginType);
        HttpClientResponse response = HttpClientUtil.getRequest(virtualGateway.getConfAddr() + PLANE_PLUGIN_PATH, params, MODULE_API_PLANE);

        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane查询插件详情接口失败，返回http status code非2xx，httpStatusCoed:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return null;
        }

        JSONObject result = JSONObject.parseObject(response.getResponseBody());
        PluginInfo plugin = Trans.parsePlugin(result.getString("Plugin"));
        if (plugin == null){
            plugin = new PluginInfo();
        }
        plugin.setPluginSchema(result.getString("Schema"));
        return plugin;
    }
}
