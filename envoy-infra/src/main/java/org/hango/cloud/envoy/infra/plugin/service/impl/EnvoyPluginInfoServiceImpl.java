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
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.dto.PluginDto;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.Operation;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.meta.PluginConstant;
import org.hango.cloud.envoy.infra.base.service.VersionManagerService;
import org.hango.cloud.envoy.infra.plugin.dao.ICustomPluginInfoDao;
import org.hango.cloud.envoy.infra.plugin.dto.GatewayPluginDto;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfoQuery;
import org.hango.cloud.envoy.infra.plugin.meta.PluginStatusStatus;
import org.hango.cloud.envoy.infra.plugin.metas.PluginType;
import org.hango.cloud.envoy.infra.plugin.service.CustomPluginInfoService;
import org.hango.cloud.envoy.infra.plugin.service.IEnvoyPluginInfoService;
import org.hango.cloud.envoy.infra.plugin.util.Trans;
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
                .filter(pluginDto-> PluginStatusStatus.ONLINE.equals(pluginDto.getPluginStatus()))
                .map(Trans::fromCustomPluginMeta).collect(Collectors.toList());
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


    private List<PluginInfo> getSystemPluginInfos(VirtualGatewayDto virtualGateway) {
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


    /**
     * 发布网关插件
     *
     * @param bindingPluginDto 网关插件聚合信息对象
     * @return 是否操作成功
     */
    @Override
    public boolean publishGatewayPlugin(BindingPluginDto bindingPluginDto) {
        logger.info("{} start to publishGatewayPlugin, virtualGwId: [{}], bindingObjectId: [{}]", PluginConstant.PLUGIN_LOG_NOTE, bindingPluginDto.getVirtualGwId(), bindingPluginDto.getBindingObjectId());
        // 新路由插件操作不需要关注pluginId
        return opsForGatewayPlugin(bindingPluginDto, Operation.CREATE, Collections.singletonList(0L));
    }

    /**
     * 更新指定ID的网关插件
     *
     * @param bindingPluginDto 网关插件聚合信息对象
     * @param pluginId         需要被更新的插件ID
     * @return 是否操作成功
     */
    @Override
    public boolean updateGatewayPlugin(BindingPluginDto bindingPluginDto, long pluginId) {
        return opsForGatewayPlugin(bindingPluginDto, Operation.UPDATE, Collections.singletonList(pluginId));
    }

    /**
     * 删除指定ID的网关插件
     *
     * @param bindingPluginDto 网关插件聚合信息对象
     * @param pluginId         需要被删除的插件ID
     * @return 是否操作成功
     */
    @Override
    public boolean deleteGatewayPlugin(BindingPluginDto bindingPluginDto, long pluginId) {
        return deleteGatewayPlugin(bindingPluginDto, Collections.singletonList(pluginId));
    }

    @Override
    public boolean deleteGatewayPlugin(BindingPluginDto bindingPluginDto, List<Long> pluginIdList) {
        return opsForGatewayPlugin(bindingPluginDto, Operation.DELETE, pluginIdList);
    }


    /**
     * 插件流程的公共请求操作方法，Gportal请求Api-plane的端点函数
     *
     * @param bindingPluginDto 插件聚合对象
     * @param operation        本次插件的操作方法（详见本文件中的Operation枚举类）
     * @param pluginIdList     插件ID集合（主要用于update和delete）
     * @return 是否请求操作成功
     */
    private boolean opsForGatewayPlugin(BindingPluginDto bindingPluginDto, Operation operation, List<Long> pluginIdList) {
        logger.info("{} gateway plugin bindingPluginDto:{}, operation: {}, pluginIdList:{}",
                PluginConstant.PLUGIN_LOG_NOTE, bindingPluginDto, operation.getAction(), pluginIdList);
        List<String> toBePublishedPluginList = createToBePublishedPluginList(bindingPluginDto, operation, pluginIdList);
        toBePublishedPluginList.forEach(plugin -> logger.info("{} gateway plugin info: {}", PluginConstant.PLUGIN_LOG_NOTE, plugin));
        // 非拷贝路由场景不执行该方法
        prepareForCopyingRoute(bindingPluginDto);
        return createPluginAndMakeRequest(bindingPluginDto, operation, toBePublishedPluginList);
    }

    private void prepareForCopyingRoute(BindingPluginDto bindingPluginDto) {
        // 拷贝路由场景需要将网关ID赋值为目标网关ID
        if (bindingPluginDto.isCopyRoute()) {
            logger.info("{} gateway plugin for copy route, origin gatewayId: {}, dest gatewayId: {}",
                    PluginConstant.PLUGIN_LOG_NOTE,
                    bindingPluginDto.getVirtualGwId(),
                    bindingPluginDto.getDestGatewayId());
            bindingPluginDto.setVirtualGwId(Long.parseLong(bindingPluginDto.getDestGatewayId()));
        }
    }

    /**
     * 根据操作类型获取全局或路由插件列表
     *
     * @param bindingPluginInfo
     * @param operation
     * @param pluginIdList
     * @return
     */
    @Override
    public List<String> createToBePublishedPluginList(BindingPluginDto bindingPluginInfo,
                                                      Operation operation,
                                                      List<Long> pluginIdList) {
        List<String> toBePublishedPluginList;
        if (bindingPluginInfo.isRoutePlugin()) {
            logger.info("{} this is route plugin.", PluginConstant.PLUGIN_LOG_NOTE);
            List<PluginBindingInfo> enabledPluginList = pluginInfoService.getEnablePluginBindingList(bindingPluginInfo.getVirtualGwId(),
                    String.valueOf(bindingPluginInfo.getBindingObjectId()),
                    bindingPluginInfo.getBindingObjectType());
            toBePublishedPluginList = createToBePublishedRoutePluginList(enabledPluginList,
                    operation,
                    bindingPluginInfo.getPluginConfiguration(),
                    pluginIdList);
        } else if (bindingPluginInfo.isGlobalPlugin() || bindingPluginInfo.isHostPlugin()) {
            logger.info("{} this is {} plugin.", bindingPluginInfo.getPluginType(), PluginConstant.PLUGIN_LOG_NOTE);
            toBePublishedPluginList =
                    createToBePublishedGlobalPluginList(operation, bindingPluginInfo.getPluginConfiguration());
        } else {
            toBePublishedPluginList = new ArrayList<>();
            logger.error("{} illegal plugin type! not route or global.", PluginConstant.PLUGIN_LOG_NOTE);
        }
        return toBePublishedPluginList;
    }


    // 根据操作类型创建对应路由的待发布插件列表
    private List<String> createToBePublishedRoutePluginList(List<PluginBindingInfo> enabledPluginList,
                                                            Operation operation,
                                                            String pluginConfig,
                                                            List<Long> pluginIdList) {
        List<String> toBePublishedPluginList;
        switch (operation) {
            case CREATE: {
                // 为当前的插件集合添加一个新的插件配置
                toBePublishedPluginList = enabledPluginList.stream()
                        .map(PluginBindingInfo::getPluginConfiguration)
                        .collect(Collectors.toList());
                if (!StringUtils.isEmpty(pluginConfig)) {
                    toBePublishedPluginList.add(pluginConfig);
                }
                break;
            }
            case UPDATE: {
                // 修改对应pluginId的插件的配置
                final Long toBeUpdatePluginId = pluginIdList.get(0);
                toBePublishedPluginList = enabledPluginList.stream()
                        .map(bindingInfoItem -> {
                            if (toBeUpdatePluginId != bindingInfoItem.getId()) {
                                return bindingInfoItem.getPluginConfiguration();
                            }
                            return pluginConfig;
                        })
                        .collect(Collectors.toList());
                break;
            }
            case DELETE: {
                // 过滤去除对应pluginId的插件的配置
                enabledPluginList.forEach(plugin -> {
                    for (Long pluginId : pluginIdList) {
                        if (pluginId.equals(plugin.getId())) {
                            plugin.setId(-1L);
                        }
                    }
                });
                toBePublishedPluginList = enabledPluginList.stream()
                        .filter(plugin -> plugin.getId() != -1L)
                        .map(PluginBindingInfo::getPluginConfiguration)
                        .collect(Collectors.toList());
                break;
            }
            default:
                toBePublishedPluginList = new ArrayList<>();
                logger.error("{} illegal operation of route plugin!", PluginConstant.PLUGIN_LOG_NOTE);
                break;
        }

        return toBePublishedPluginList;
    }


    // 根据操作类型创建对应项目的待发布插件列表
    private List<String> createToBePublishedGlobalPluginList(Operation operation, String pluginConfig) {
        List<String> toBePublishedPluginList = new ArrayList<>();
        switch (operation) {
            case CREATE:
            case UPDATE:
                toBePublishedPluginList.add(pluginConfig);
                break;
            case DELETE:
                break;
            default:
                logger.error("{} illegal operation of global plugin!", PluginConstant.PLUGIN_LOG_NOTE);
        }
        return toBePublishedPluginList;
    }

    @Override
    public boolean createPluginAndMakeRequest(BindingPluginDto bindingPluginDto, Operation operation, List<String> toBePublishedPluginList) {
        RouteDto routeRuleProxyInfo;
        // 路由使能状态，路由不使能则不发布（该标志不影响全局插件）
        String enableState = BaseConst.ENABLE_STATE;
        GatewayPluginDto gatewayPlugin;

        // 获取插件实体gatewayPlugin信息
        if (bindingPluginDto.isRoutePlugin()) {
            routeRuleProxyInfo = routeRuleProxyService
                    .getRoute(bindingPluginDto.getVirtualGwId(), bindingPluginDto.getBindingObjectId());
            gatewayPlugin = createRoutePlugin(bindingPluginDto, routeRuleProxyInfo, toBePublishedPluginList);
            enableState = routeRuleProxyInfo.getEnableState();
        }else if (bindingPluginDto.isHostPlugin()){
            gatewayPlugin = createHostPlugin(bindingPluginDto, toBePublishedPluginList);
        } else {
             gatewayPlugin = createGlobalPlugin(bindingPluginDto, toBePublishedPluginList);
        }

        // 使能状态下进行路由插件发布，发送相关配置至api-plane，否则在控制台进行虚拟发布；路由启用情况，虽然路由本身是disable状态，但仍然要发布插件
        if (bindingPluginDto.canPublishPlugin(enableState) || bindingPluginDto.isEnableRouteOperation()) {
            logger.info("{} bindingID:{}, bindingID is route ID for route plugins or project ID for global" +
                            "plugins; GatewayPluginDto plugins start to change",
                    PluginConstant.PLUGIN_LOG_NOTE, bindingPluginDto.getBindingObjectId());
            return changeGatewayPluginFromApiPlane(operation, gatewayPlugin);
        }
        return true;
    }

    private boolean changeGatewayPluginFromApiPlane(Operation operation, GatewayPluginDto gatewayPlugin) {
        boolean responseStatus;
        if (operation.equals(Operation.DELETE)) {
            responseStatus = deleteGatewayPluginFromApiPlane(gatewayPlugin);
        } else {
            responseStatus = publishGatewayPluginToApiPlane(gatewayPlugin);
        }
        if (!responseStatus) {
            logger.info("{} request to Api-plane failed", PluginConstant.PLUGIN_LOG_NOTE);
            return false;
        }
        return true;
    }

    /**
     * 向API-Plane请求删除路由插件
     *
     * @param plugin 待删除插件信息
     * @return 请求是否成功
     */
    public boolean deleteGatewayPluginFromApiPlane(GatewayPluginDto plugin) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put(ACTION, "DeletePlugin");
        return requestForGatewayPlugin(plugin, params);
    }

    /**
     * 向API-Plane请求发布路由插件
     *
     * @param plugin 待发布插件信息
     * @return 请求是否成功
     */
    public boolean publishGatewayPluginToApiPlane(GatewayPluginDto plugin) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put(ACTION, "PublishPlugin");
        return requestForGatewayPlugin(plugin, params);
    }

    private boolean requestForGatewayPlugin(GatewayPluginDto plugin, Map<String, Object> params) {
        params.put(VERSION, PLANE_VERSION);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String action = (String) params.get(ACTION);
//        ResourceDTO resourceDTO = versionManagerService.getResourceDTO(virtualGateway.getId(), plugin.getBindingObjectId(), plugin.getBindingObjectType(), plugin.getPluginType(), action);
        return versionManagerService.publishPluginWithVersionManager(plugin.getAddr() + PLANE_PORTAL_PATH, params, headers, plugin, null);
    }


    /**
     * 路由插件创建实体
     *
     * @param bindingPluginInfo  插件绑定信息
     * @param routeRuleProxyInfo 路由实体信息
     * @param pluginList         待发布插件配置集合
     * @return 网关插件实体（在Api-plane转换为GatewayPlugin CRD）
     */
    private GatewayPluginDto createRoutePlugin(BindingPluginDto bindingPluginInfo,
                                               RouteDto routeRuleProxyInfo,
                                               List<String> pluginList) {
        // 虚拟主机信息不传
        return createPlugin(bindingPluginInfo, routeRuleProxyInfo, null, null, pluginList);
    }

    /**
     * 全局（项目级）插件创建实体
     *
     * @param bindingPluginInfo 插件绑定信息
     * @param pluginList        待发布插件配置集合
     * @return 网关插件实体（在Api-plane转换为GatewayPlugin CRD）
     */
    private GatewayPluginDto createGlobalPlugin(BindingPluginDto bindingPluginInfo, List<String> pluginList) {
        // 路由信息不传
        List<String> hosts = domainInfoService.getHosts(bindingPluginInfo.getBindingObjectId(), bindingPluginInfo.getVirtualGwId());
        PermissionScopeDto project = virtualGatewayProjectService.getProjectScope(bindingPluginInfo.getBindingObjectId());
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(bindingPluginInfo.getVirtualGwId());
        String code = project.getPermissionScopeEnName() + "-" + bindingPluginInfo.getBindingObjectId() + "-" + virtualGatewayDto.getCode();
        return createPlugin(bindingPluginInfo, null, code, hosts, pluginList);
    }

    /**
     * 域名插件创建实体
     *
     * @param bindingPluginInfo 插件绑定信息
     * @param pluginList        待发布插件配置集合
     * @return 网关插件实体（在Api-plane转换为GatewayPlugin CRD）
     */
    private GatewayPluginDto createHostPlugin(BindingPluginDto bindingPluginInfo, List<String> pluginList) {
        // 路由信息不传
        DomainInfo domainInfoPO = domainInfoMapper.selectById(bindingPluginInfo.getBindingObjectId());
        List<String> hosts = Collections.singletonList(domainInfoPO.getHost());
        String code = bindingPluginInfo.getBindingObjectType() + "-" + bindingPluginInfo.getBindingObjectId();
        return createPlugin(bindingPluginInfo, null, code, hosts, pluginList);
    }

    private GatewayPluginDto createPlugin(BindingPluginDto bindingPluginInfo,
                                          RouteDto routeDto,
                                          String code,
                                          List<String> hosts,
                                          List<String> pluginList) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(bindingPluginInfo.getVirtualGwId());

        GatewayPluginDto gatewayPlugin = new GatewayPluginDto();
        gatewayPlugin.setAddr(virtualGatewayDto.getConfAddr());
        gatewayPlugin.setPort(virtualGatewayDto.getPort());
        gatewayPlugin.setPluginType(bindingPluginInfo.getPluginType());
        gatewayPlugin.setGateway(virtualGatewayDto.getGwClusterName().toLowerCase() + "-" + virtualGatewayDto.getCode());
        gatewayPlugin.setPlugins(pluginList);
        gatewayPlugin.setBindingObjectId(bindingPluginInfo.getBindingObjectId());
        gatewayPlugin.setBindingObjectType(bindingPluginInfo.getBindingObjectType());
        if (routeDto != null) {
            gatewayPlugin.setRouteId(buildVirtualServiceName(
                    routeDto.getName(), String.valueOf(routeDto.getProjectId()), CommonUtil.genGatewayStrForRoute(virtualGatewayDto)));
        }
        if (!CollectionUtils.isEmpty(hosts)){
            gatewayPlugin.setHosts(hosts);
        }
        gatewayPlugin.setCode(code + "-" + bindingPluginInfo.getPluginType());
        return gatewayPlugin;
    }

    String buildVirtualServiceName(String apiName, String projectId, String gw) {
        return String.format("%s-%s-%s", apiName, projectId, gw);
    }

}
