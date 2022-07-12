package org.hango.cloud.dashboard.apiserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayEnum;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCodeEnum;
import org.hango.cloud.dashboard.apiserver.service.IDubboService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.core.constant.PluginConstant;
import org.hango.cloud.dashboard.envoy.dao.IRouteRuleProxyDao;
import org.hango.cloud.dashboard.envoy.meta.BindingPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyDestinationInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IAuthPermissionService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGatewayService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyWebServiceService;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyDestinationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleHeaderOperationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.VirtualClusterDto;
import org.hango.cloud.dashboard.scg.service.IGetFromScgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 路由规则管理Service层实现类
 *
 * @author hzchenzhongyang 2019-09-18
 */
@Service
public class RouteRuleProxyServiceImpl implements IRouteRuleProxyService {
    private static final Logger logger = LoggerFactory.getLogger(RouteRuleProxyServiceImpl.class);

    @Autowired
    private IRouteRuleProxyDao routeRuleProxyDao;

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @Autowired
    private IEnvoyGatewayService envoyGatewayService;

    @Autowired
    private IAuthPermissionService authPermissionService;

    @Autowired
    private IGetFromApiPlaneService getFromApiPlaneService;

    @Autowired
    private IDubboService dubboService;

    @Autowired
    private IEnvoyWebServiceService webServiceService;

    @Autowired
    private IGetFromScgService getFromScgService;

    @Override
    public ErrorCode checkPublishParam(RouteRuleProxyDto routeRuleProxyDto) {
        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(routeRuleProxyDto.getRouteRuleId());
        if (null == routeRuleInfo) {
            logger.info("路由规则发布时指定的路由规则不存在! routeRuleId:{}", routeRuleProxyDto.getRouteRuleId());
            return CommonErrorCode.NoSuchRouteRule;
        }

        if (routeRuleProxyDto.getDestinationServices() == null || routeRuleProxyDto.getDestinationServices().size() == 0) {
            logger.info("路由规则发布时未传入目标服务信息! routeRuleId:{}", routeRuleProxyDto.getRouteRuleId());
            return CommonErrorCode.InvalidDestinationService;
        }

        routeRuleProxyDto.setServiceId(routeRuleInfo.getServiceId());
        //多网关发布参数校验
        if (CollectionUtils.isNotEmpty(routeRuleProxyDto.getGwIds())) {
            List<ServiceProxyInfo> serviceProxyInfos = routeRuleProxyDto.getGwIds().stream().map(item ->
                    serviceProxyService.getServiceProxyByServiceIdAndGwId(item, routeRuleProxyDto.getServiceId())).collect(Collectors.toList());
            if (serviceProxyInfos.stream().map(ServiceProxyInfo::getBackendService).collect(Collectors.toSet()).size() > 1) {
                logger.info("路由发布至多网关，路由所属服务后端地址不同，不允许发布");
                return CommonErrorCode.BackendServiceDifferent;
            }

            List<ErrorCode> errorCodes = routeRuleProxyDto.getGwIds().stream().map(item ->
                    checkPublishRouteRuleAndGw(item, routeRuleProxyDto)).collect(Collectors.toList());
            List<ErrorCode> errorParams = errorCodes.stream().filter(item -> !item.equals(CommonErrorCode.Success)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(errorParams)) {
                return errorParams.get(0);
            }
        } else {
            ErrorCode errorCode = checkPublishRouteRuleAndGw(routeRuleProxyDto.getGwId(), routeRuleProxyDto);
            if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
                return errorCode;
            }
        }
        return CommonErrorCode.Success;
    }

    private ErrorCode checkPublishRouteRuleAndGw(long gwId, RouteRuleProxyDto routeRuleProxyDto) {
        GatewayInfo gatewayInDb = gatewayInfoService.get(gwId);
        if (gatewayInDb == null) {
            logger.info("发布路由，指定网关不存在，网关id:{}", gwId);
            return CommonErrorCode.NoSuchGateway;
        }
        routeRuleProxyDto.setGwType(gatewayInDb.getGwType());
        long routeRuleProxyCount = getRouteRuleProxyCount(gwId, routeRuleProxyDto.getRouteRuleId());
        if (routeRuleProxyCount > 0) {
            logger.info("路由规则已经发布到该网关，不允许重复发布");
            return CommonErrorCode.RouteRuleAlreadyPublished(gatewayInDb.getGwName());
        }

        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, routeRuleProxyDto.getServiceId());
        if (null == serviceProxyInfo) {
            logger.info("路由规则发布时所属的服务未发布！serviceId:{}, gwId:{}", routeRuleProxyDto.getServiceId(), gwId);
            return CommonErrorCode.ServiceNotPublished;
        }

        //TODO SCG Check
        routeRuleProxyDto.setGwType(gatewayInDb.getGwType());
        if (GatewayEnum.SCG.getType().equals(gatewayInDb.getGwType())) {
            return CommonErrorCode.Success;
        }

        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(routeRuleProxyDto.getRouteRuleId());
        EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService.getVirtualHostByGwIdAndProjectId(gwId, routeRuleInfo.getProjectId());
        if (null == virtualHostInfo) {
            logger.info("发布路由规则时，该项目没有关联指定网关，不允许发布, gwId:{}, projectId:{}", gwId, routeRuleInfo.getProjectId());
            return CommonErrorCode.ProjectNotAssociatedGateway;
        }

        //动态发布，必须填端口
        if (Const.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyInfo.getPublishType())) {
            List<EnvoyDestinationDto> destinationServices = routeRuleProxyDto.getDestinationServices();
            if (CollectionUtils.isEmpty(destinationServices)) {
                logger.info("路由规则发布时指定的后端服务为空!");
                return CommonErrorCode.MissingParameter("ProxyServices");
            }

            int totalweight = 0;
            for (EnvoyDestinationDto destinationService : destinationServices) {
                //发布路由规则，后端服务id不正确，之后版本等也在这里校验
                if (routeRuleProxyDto.getServiceId() != destinationService.getServiceId()) {
                    logger.info("路由规则发布时，后端服务id不正确");
                    return CommonErrorCode.NoSuchService;
                }

                if (destinationService.getWeight() < 0 || destinationService.getWeight() > 100) {
                    logger.info("路由规则发布时，设置的权重不合法: {}", destinationService.getWeight());
                    return CommonErrorCode.InvalidParameter(String.valueOf(destinationService.getWeight()), "Weight");
                }
                totalweight += destinationService.getWeight();
            }

            if (totalweight != 100) {
                return CommonErrorCode.InvalidTotalWeight;
            }
        }

        //校验发布路由时，选择的版本
        return checkSubsetWhenPublishRouteRuleAndGw(serviceProxyInfo, routeRuleProxyDto);
    }

    /**
     * 校验发布路由时，选择的版本
     *
     * @param serviceProxyInfo
     * @param routeRuleProxyDto
     * @return
     */
    public ErrorCode checkSubsetWhenPublishRouteRuleAndGw(ServiceProxyInfo serviceProxyInfo, RouteRuleProxyDto routeRuleProxyDto) {
        if (routeRuleProxyDto.getDestinationServices() == null || routeRuleProxyDto.getDestinationServices().size() == 0) {
            return CommonErrorCode.Success;
        }
        List<String> subsetNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(serviceProxyInfo.getSubsets())) {
            List<EnvoySubsetDto> envoySubsetDtoObjectList = JSON.parseArray(serviceProxyInfo.getSubsets(), EnvoySubsetDto.class);
            envoySubsetDtoObjectList.stream().forEach(envoySubsetDto -> {
                String subsetName = envoySubsetDto.getName();
                subsetNameList.add(subsetName);
            });
        }

        for (EnvoyDestinationDto envoyDestinationDto : routeRuleProxyDto.getDestinationServices()) {
            if (StringUtils.isNotBlank(envoyDestinationDto.getSubsetName()) && !subsetNameList.contains(envoyDestinationDto.getSubsetName())) {
                return CommonErrorCode.InvalidSubsetName;
            }
        }
        return CommonErrorCode.Success;
    }

    private ErrorCode checkOneProxyService(long gwId, EnvoyDestinationDto destinationService) {
        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, destinationService.getServiceId());
        if (null == serviceProxyInfo) {
            logger.info("路由规则发布时指定的目的服务未发布！destinationServiceId:{}, gwId:{}", destinationService.getServiceId(), gwId);
            return CommonErrorCode.ServiceNotPublished;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkUpdateParam(RouteRuleProxyDto routeRuleProxyDto) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(routeRuleProxyDto.getGwId());
        if (null == gatewayInfo) {
            logger.info("动态更新路由时指定的网关不存在! gwId:{}", routeRuleProxyDto.getGwId());
            return CommonErrorCode.NoSuchGateway;
        }

        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(routeRuleProxyDto.getRouteRuleId());
        if (null == routeRuleInfo) {
            logger.info("动态更新路由指定的路由规则不存在! routeRuleId:{}", routeRuleProxyDto.getRouteRuleId());
            return CommonErrorCode.NoSuchRouteRule;
        }

        //TODO SCG Check
        routeRuleProxyDto.setGwType(gatewayInfo.getGwType());
        if (GatewayEnum.SCG.getType().equals(gatewayInfo.getGwType())) {
            return CommonErrorCode.Success;
        }

        List<EnvoyDestinationDto> destinationServices = routeRuleProxyDto.getDestinationServices();
        if (CollectionUtils.isEmpty(destinationServices)) {
            logger.info("动态更新路由指定的后端服务为空!");
            return CommonErrorCode.MissingParameter("ProxyServices");
        }
        for (EnvoyDestinationDto destinationService : destinationServices) {
            //发布路由规则，后端服务id不正确，之后版本等也在这里校验
            if (routeRuleInfo.getServiceId() != destinationService.getServiceId()) {
                logger.info("动态更新路由，后端服务id不正确");
                return CommonErrorCode.NoSuchService;
            }
        }

        EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService.getVirtualHostByGwIdAndProjectId(routeRuleProxyDto.getGwId(), routeRuleInfo.getProjectId());
        if (null == virtualHostInfo) {
            logger.info("更新路由规则时尚未配置virtual host，不允许发布更新！ gwId:{}, projectId:{}", routeRuleProxyDto.getGwId(), routeRuleInfo.getProjectId());
            return CommonErrorCode.ProjectNotAssociatedGateway;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean isSameRouteRuleProxyInfo(RouteRuleProxyInfo routeRuleProxyInfo) {
        EnvoyVirtualHostInfo virtualHost =
                envoyGatewayService.getVirtualHostByGwIdAndProjectId(routeRuleProxyInfo.getGwId(), routeRuleProxyInfo.getProjectId());

        // 目标项目下的已发布路由数据
        List<RouteRuleProxyInfo> targetRouteRuleProxyInfoList = new ArrayList<>();
        List<Long> projectIdList;

        if (Const.PROJECT_BINDING_TYPE.equals(virtualHost.getBindType())) {
            // 项目关联的情况需要对比当前项目和关联项目的所有路由
            projectIdList = virtualHost.getProjectList();
        } else {
            // 域名关联的情况下需要确认其他以项目形式关联到自身的项目下的所有路由
            List<EnvoyVirtualHostInfo> virtualHostListInSameGw =
                    envoyGatewayService.getVirtualHostListByGwId(routeRuleProxyInfo.getGwId());
            projectIdList = virtualHostListInSameGw.stream()
                    .filter(vh -> Const.PROJECT_BINDING_TYPE.equals(vh.getBindType())
                            && vh.getProjectList().contains(routeRuleProxyInfo.getProjectId()))
                    .map(EnvoyVirtualHostInfo::getProjectId)
                    .collect(Collectors.toList());
        }

        // 添加当前项目，查询所有目标项目下的路由
        projectIdList.add(routeRuleProxyInfo.getProjectId());
        for (Long projectId : projectIdList) {
            targetRouteRuleProxyInfoList.addAll(getRouteListByProjectIdAndGwId(projectId, virtualHost.getGwId()));
        }

        // 判断是否存在与当前将发布的新路由拥有相同规则的已发布路由数据
        for (RouteRuleProxyInfo targetRoute : targetRouteRuleProxyInfoList) {
            if (routeRuleProxyInfo.getId() == targetRoute.getId()) {
                // 对比路由规则是否相同，跳过自身
                continue;
            }
            if (routeRuleProxyInfo.isSame(targetRoute)) {
                logger.warn("exist same route rule proxy, src rule_proxy_id: {}, target route_proxy_id: {}",
                        routeRuleProxyInfo.getId(), targetRoute.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public long publishRouteRule(RouteRuleProxyInfo routeRuleProxyInfo, List<String> pluginConfigurations, boolean updateHosts) {
        RouteRuleInfo routeRuleInfoDb = routeRuleInfoService.getRouteRuleInfoById(routeRuleProxyInfo.getRouteRuleId());
        if (null == routeRuleInfoDb) {
            logger.error("发布路由规则，存在不存在的路由规则，存在脏数据，routeRuleId:{}", routeRuleProxyInfo.getRouteRuleId());
            return Const.ERROR_RESULT;
        }
        routeRuleProxyInfo.setProjectId(routeRuleInfoDb.getProjectId());
        if (updateHosts) {
            EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService.getVirtualHostByGwIdAndProjectId(routeRuleProxyInfo.getGwId(), routeRuleInfoDb.getProjectId());
            if (null == virtualHostInfo) {
                logger.info("发布路由，virtualHostInfo为空，禁止发布");
                return Const.ERROR_RESULT;
            }
            routeRuleProxyInfo.setHosts(virtualHostInfo.getHosts());
        }
        //使能状态为enable，开启使能状态，发送相关配置至api-plane，否则在控制台进行虚拟发布
        if (Const.ROUTE_RULE_ENABLE_STATE.equals(routeRuleProxyInfo.getEnableState())) {
            if (!publishToDiffTypeGw(routeRuleProxyInfo, pluginConfigurations)) {
                return Const.ERROR_RESULT;
            }
        }

        routeRuleProxyInfo.setUpdateTime(System.currentTimeMillis());
        // 如果routeRuleProxyInfo的id大于0，则仅为插件配置更新而不是新发布路由规则，不需要新增routeRuleProxyInfo，更新routeRuleProxyInfo
        if (routeRuleProxyInfo.getId() > 0) {
            routeRuleProxyDao.update(routeRuleProxyInfo);
            return routeRuleProxyInfo.getId();
        }

        //新发布路由/复制新路由，需要增加
        routeRuleProxyInfo.setCreateTime(System.currentTimeMillis());
        long publishRouteRuleId = addRouteRuleProxy(routeRuleProxyInfo);
        //发布route rule成功，修改路由规则发布状态
        if (publishRouteRuleId > NumberUtils.INTEGER_ZERO) {
            //路由规则状态未未发布，修改为已发布
            if (routeRuleInfoDb != null && routeRuleInfoDb.getPublishStatus() == NumberUtils.INTEGER_ZERO) {
                routeRuleInfoDb.setPublishStatus(NumberUtils.INTEGER_ONE);
                routeRuleInfoService.updateRouteRule(routeRuleInfoDb);
            }
        }
        return publishRouteRuleId;
    }

    @Override
    public List<String> publishRouteRuleBatch(List<Long> gwIds, RouteRuleProxyDto routeRuleProxyDto) {
        if (CollectionUtils.isEmpty(gwIds)) {
            return new ArrayList<>();
        }
        return gwIds.stream().filter(item -> {
            routeRuleProxyDto.setGwId(item);
            RouteRuleProxyInfo routeRuleProxyInfo = toMeta(routeRuleProxyDto);
            if (isSameRouteRuleProxyInfo(routeRuleProxyInfo)) {
                // 发布失败，存在相同配置的路由
                logger.error("更新路由规则，参数完全相同，不允许更新");
                return true;
            }
            return Const.ERROR_RESULT == publishRouteRule(routeRuleProxyInfo, new ArrayList<>(), true);
        }).map(item -> gatewayInfoService.get(item).getGwName()).collect(Collectors.toList());
    }

    @Override
    public long addRouteRuleProxy(RouteRuleProxyInfo routeRuleProxyInfo) {
        return routeRuleProxyDao.add(routeRuleProxyInfo);
    }


    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyList(long gwId, long serviceId, String pattern,
                                                          String sortKey, String sortValue, long offset,
                                                          long limit) {

        long projectId = ProjectTraceHolder.getProId();
        List<Long> routeIds = Collections.EMPTY_LIST;
        if (StringUtils.isNotBlank(pattern)) {
            routeIds = routeRuleInfoService.getRouteRuleInfoByPattern(pattern, -1, serviceId, projectId, "", "", 0,
                            1000).stream().map(RouteRuleInfo::getId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(routeIds)) {
                return Collections.emptyList();
            }
        }
        return routeRuleProxyDao.getRouteRuleProxyList(gwId, serviceId, routeIds, projectId, sortKey, sortValue, offset,
                limit);
    }

    @Override
    public List<RouteRuleProxyInfo> getAuthRouteProxy(final long gwId, final long serviceId, final long routeId,
                                                      final boolean auth, final long offset, final long limit) {
        if (!auth) {
            if (routeId == 0) {
                return getRouteRuleProxyList(gwId, serviceId, StringUtils.EMPTY, Const.CONST_PRIORITY, "", offset,
                        limit);
            }
            List<RouteRuleProxyInfo> routeRuleProxyList = Lists.newArrayList();
            routeRuleProxyList.add(getRouteRuleProxy(gwId, routeId));
            return routeRuleProxyList;
        }
        List<Long> routeAuthId = authPermissionService.getRouteAuthId(gwId);
        if (CollectionUtils.isEmpty(routeAuthId)) {
            return Lists.newArrayList();
        }
        return routeRuleProxyDao.getRouteRuleProxyList(gwId, serviceId, ProjectTraceHolder.getProId(), routeId,
                routeAuthId, offset, limit);
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyList(long serviceId) {
        return routeRuleProxyDao.getRouteRuleProxyList(serviceId);
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteListByProjectIdAndGwId(long projectId, long gwId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("projectId", projectId);
        params.put("gwId", gwId);
        return routeRuleProxyDao.getRecordsByField(params);
    }

    @Override
    public long getRouteRuleProxyCountByService(long gwId, long serviceId) {
        return routeRuleProxyDao.getRouteRuleProxyCount(gwId, serviceId, ProjectTraceHolder.getProId());
    }

    @Override
    public long getRouteRuleProxyCount(long gwId, long serviceId, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return getRouteRuleProxyCountByService(gwId, serviceId);
        }
        long projectId = ProjectTraceHolder.getProId();
        List<Long> routeIds = routeRuleInfoService.getRouteRuleInfoByPattern(pattern, -1, serviceId,
                projectId, "", "", 0,
                1000).stream().map(
                RouteRuleInfo::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(routeIds)) {
            return 0;
        }
        return routeRuleProxyDao.getRouteRuleProxyCount(gwId, serviceId, projectId, routeIds);
    }

    @Override
    public long getAuthRouteCount(final long gwId, final long serviceId, final long routeId, final boolean auth) {
        if (!auth) {
            if (routeId == 0) {
                return getRouteRuleProxyCountByService(gwId, serviceId);
            }
            return getRouteRuleProxyCount(gwId, routeId);
        }
        List<Long> routeAuthId = authPermissionService.getRouteAuthId(gwId);
        if (CollectionUtils.isEmpty(routeAuthId)) {
            return 0;
        }
        return routeRuleProxyDao.getRouteRuleProxyCount(gwId, serviceId, ProjectTraceHolder.getProId(), routeId,
                routeAuthId);
    }

    @Override
    public long getRouteRuleProxyCount(long gwId, long routeRuleId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        if (0 != gwId) {
            params.put("gwId", gwId);
        }
        if (0 != routeRuleId) {
            params.put("routeRuleId", routeRuleId);
        }
        params.put("projectId", ProjectTraceHolder.getProId());
        return routeRuleProxyDao.getCountByFields(params);
    }

    @Override
    public RouteRuleProxyInfo getRouteRuleProxy(long gwId, long routeRuleId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("routeRuleId", routeRuleId);
        List<RouteRuleProxyInfo> routeRuleProxyInfos = routeRuleProxyDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(routeRuleProxyInfos) ? null : routeRuleProxyInfos.get(0);
    }

    @Override
    public boolean deleteRouteRuleProxy(long gwId, long routeRuleId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("routeRuleId", routeRuleId);
        List<RouteRuleProxyInfo> routeRuleProxyInfos = routeRuleProxyDao.getRecordsByField(params);
        //幂等删除
        if (CollectionUtils.isEmpty(routeRuleProxyInfos)) {
            return true;
        }
        if (!deleteAllRoutePlugins(gwId, routeRuleId)) {
            return false;
        }
        RouteRuleProxyInfo routeRuleProxyInfo = routeRuleProxyInfos.get(0);
        if (!offlineToDiffTypeGw(routeRuleProxyInfo)) {
            return false;
        }

        if (!authPermissionService.deleteAuthPermission(gwId, 0, routeRuleId, Const.AUTH_GATEWAY_ROUTE)) {
            logger.warn("删除路由下的授权失败");
            return false;
        }
        //删除路由发布信息
        routeRuleProxyDao.delete(routeRuleProxyInfo);
        //更新路由规则发布状态
        updateRouteRulePublishStatus(routeRuleId);
        //删除dubbo转换信息
        dubboService.delete(routeRuleProxyInfo.getId(), Const.ROUTE);
        //删除webservice转换信息
        webServiceService.deleteRouteProxyWsParam(routeRuleProxyInfo.getGwId(), routeRuleProxyInfo.getServiceId(), routeRuleProxyInfo.getRouteRuleId());
        return true;
    }

    private boolean deleteAllRoutePlugins(long gwId, long routeRuleId) {
        List<EnvoyPluginBindingInfo> alreadyBindingPlugins = envoyPluginInfoService.getEnablePluginBindingList(gwId,
                String.valueOf(routeRuleId),
                EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        // 没有插件无需删除
        if (CollectionUtils.isEmpty(alreadyBindingPlugins)) {
            return true;
        }
        BindingPluginInfo bindingPluginInfo = new BindingPluginInfo(gwId,
                BindingPluginInfo.PLUGIN_TYPE_ROUTE,
                routeRuleId,
                "",
                "");
        // 删除路由插件
        List<Long> pluginIdList =
                alreadyBindingPlugins.stream().map(EnvoyPluginBindingInfo::getId).collect(Collectors.toList());
        if (!envoyPluginInfoService.deleteGatewayPlugin(bindingPluginInfo, pluginIdList)) {
            logger.error("{} delete route plugin from Api-plane failed", PluginConstant.PLUGIN_LOG_NOTE);
            return false;
        }
        //删除路由规则绑定的插件
        envoyPluginInfoService.deletePluginList(gwId,
                String.valueOf(routeRuleId),
                EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        return true;
    }

    private List<EnvoyDestinationInfo> generateDestinationWithWeight(List<Long> serviceIds) {
        if (CollectionUtils.isEmpty(serviceIds) || serviceIds.size() == 0) {
            return new ArrayList<>();
        }
        List<EnvoyDestinationInfo> envoyDestinationInfos = new ArrayList<>();
        int weight = 100 / serviceIds.size();
        for (long serviceId : serviceIds) {
            EnvoyDestinationInfo envoyDestinationInfo = new EnvoyDestinationInfo();
            envoyDestinationInfo.setServiceId(serviceId);
            envoyDestinationInfo.setWeight(weight);
            envoyDestinationInfos.add(envoyDestinationInfo);
        }
        //不能被整除，需要重新计算最后一个权重
        if ((weight * serviceIds.size()) != 100) {
            envoyDestinationInfos.remove(serviceIds.size() - 1);
            weight = 100 - (weight * (serviceIds.size() - 1));
            EnvoyDestinationInfo envoyDestinationInfo = new EnvoyDestinationInfo();
            envoyDestinationInfo.setServiceId(serviceIds.get(serviceIds.size() - 1));
            envoyDestinationInfo.setWeight(weight);
            envoyDestinationInfos.add(envoyDestinationInfo);
        }
        return envoyDestinationInfos;
    }

    private void updateRouteRulePublishStatus(long routeRuleId) {
        //修改路由规则发布状态为未发布
        Map<String, Object> paramRouteRule = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        paramRouteRule.put("routeRuleId", routeRuleId);
        List<RouteRuleProxyInfo> records = routeRuleProxyDao.getRecordsByField(paramRouteRule);
        if (!CollectionUtils.isEmpty(records)) {
            return;
        }
        RouteRuleInfo routeRuleInfoDb = routeRuleInfoService.getRouteRuleInfoById(routeRuleId);
        if (routeRuleInfoDb != null) {
            routeRuleInfoDb.setPublishStatus(NumberUtils.INTEGER_ZERO);
            routeRuleInfoService.updateRouteRule(routeRuleInfoDb);
        }
    }

    @Override
    public ErrorCode checkDeleteRouteRuleProxy(long gwId, long routeRuleId, List<Long> serviceIds) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("routeRuleId", routeRuleId);
        List<RouteRuleProxyInfo> routeRuleProxyInfos = routeRuleProxyDao.getRecordsByField(params);
        if (CollectionUtils.isEmpty(routeRuleProxyInfos)) {
            logger.info("下线路由规则，路由规则未发布");
            return CommonErrorCode.RouteRuleNotPublished;
        }
        if (CollectionUtils.isEmpty(serviceIds)) {
            return CommonErrorCode.Success;
        }
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        //TODO SCG Check
        if (GatewayEnum.SCG.getType().equals(gatewayInfo.getGwType())) {
            return CommonErrorCode.Success;
        }

        RouteRuleProxyInfo routeRuleProxyInfo = routeRuleProxyInfos.get(0);
        List<EnvoyDestinationInfo> destinationServiceList = routeRuleProxyInfo.getDestinationServiceList();
        List<Long> desServiceIdList = destinationServiceList.stream().map(EnvoyDestinationInfo::getServiceId).collect(Collectors.toList());
        for (Long serviceId : serviceIds) {
            if (!desServiceIdList.contains(serviceId)) {
                return CommonErrorCode.RouteRuleServiceNotMatch;
            }
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkUpdateEnableState(long gwId, long routeRuleId, String enableState) {
        RouteRuleProxyInfo routeRuleProxyInDb = getRouteRuleProxy(gwId, routeRuleId);
        if (routeRuleProxyInDb == null) {
            logger.info("修改路由使能状态，路由未发布，不允许修改");
            return CommonErrorCode.RouteRuleNotPublished;
        }
        if (!Const.ROUTE_RULE_ENABLE_STATE.equals(enableState) && !Const.ROUTE_RULE_DISABLE_STATE.equals(enableState)) {
            logger.info("修改路由使能状态，使能状态填写不对，仅支持enable，disable");
            return CommonErrorCode.InvalidParameter(enableState, "EnableState");
        }
        return CommonErrorCode.Success;
    }


    @Override
    public long updateEnableState(long gwId, long routeRuleId, String enableState) {
        RouteRuleProxyInfo routeRuleProxyInDb = getRouteRuleProxy(gwId, routeRuleId);
        if (routeRuleProxyInDb == null) {
            logger.error("更新使能状态，存在脏数据,routeRuleId:{}", routeRuleId);
            return Const.ERROR_RESULT;
        }
        List<EnvoyPluginBindingInfo> alreadyBindingPlugins = envoyPluginInfoService.getEnablePluginBindingList(gwId,
                String.valueOf(routeRuleId),
                EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        BindingPluginInfo bindingPluginInfo = new BindingPluginInfo(gwId,
                BindingPluginInfo.PLUGIN_TYPE_ROUTE,
                routeRuleId,
                "",
                "");
        //使能状态修改为enable
        if (Const.ROUTE_RULE_ENABLE_STATE.equals(enableState)) {
            // 发布路由
            List<String> newPluginConfigurations = alreadyBindingPlugins.stream()
                    .map(EnvoyPluginBindingInfo::getPluginConfiguration)
                    .collect(Collectors.toList());
            if (!publishToDiffTypeGw(routeRuleProxyInDb, newPluginConfigurations)) {
                return Const.ERROR_RESULT;
            }
            // 发布路由插件（此处是本路由下的插件全量发布，只需要关注路由元信息即可，插件配置不关心）
            envoyPluginInfoService.publishGatewayPlugin(bindingPluginInfo);
        }
        //使能状态修改为disable
        if (Const.ROUTE_RULE_DISABLE_STATE.equals(enableState)) {
            // 删除路由插件
            List<Long> pluginIdList =
                    alreadyBindingPlugins.stream().map(EnvoyPluginBindingInfo::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(pluginIdList)
                    && !envoyPluginInfoService.deleteGatewayPlugin(bindingPluginInfo, pluginIdList)) {
                return Const.ERROR_RESULT;
            }
            if (!offlineToDiffTypeGw(routeRuleProxyInDb)) {
                return Const.ERROR_RESULT;
            }
        }
        //是否需要更新使能状态
        if (!enableState.equals(routeRuleProxyInDb.getEnableState())) {
            routeRuleProxyInDb.setEnableState(enableState);
            routeRuleProxyInDb.setUpdateTime(System.currentTimeMillis());
            return routeRuleProxyDao.update(routeRuleProxyInDb);
        }
        return NumberUtils.INTEGER_ZERO;
    }


    @Override
    public long updateEnvoyRouteRuleProxy(RouteRuleProxyInfo proxyInfo) {
        long gwId = proxyInfo.getGwId();
        long routeRuleId = proxyInfo.getRouteRuleId();
        RouteRuleProxyInfo routeRuleProxyInDb = getRouteRuleProxy(gwId, routeRuleId);
        if (routeRuleProxyInDb == null) {
            logger.error("更新路由规则，存在脏数据,routeRuleId:{}", routeRuleId);
            return Const.ERROR_RESULT;
        }
        EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService.getVirtualHostByGwIdAndProjectId(proxyInfo.getGwId(), routeRuleProxyInDb.getProjectId());
        if (null == virtualHostInfo) {
            return Const.ERROR_RESULT;
        }
        proxyInfo.setHosts(virtualHostInfo.getHosts());
        proxyInfo.setId(routeRuleProxyInDb.getId());
        if (routeRuleProxyInDb.getMirrorTrafficValue() != null) {
            proxyInfo.setMirrorTrafficValue(routeRuleProxyInDb.getMirrorTrafficValue());
            proxyInfo.setMirrorTraffic(routeRuleProxyInDb.getMirrorTraffic());
            proxyInfo.setMirrorServiceId(routeRuleProxyInDb.getMirrorServiceId());
        }
        //使能状态为enable
        if (Const.ROUTE_RULE_ENABLE_STATE.equals(proxyInfo.getEnableState())) {
            List<EnvoyPluginBindingInfo> alreadyBindingPlugins = envoyPluginInfoService.getEnablePluginBindingList(gwId, String.valueOf(routeRuleId),
                    EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
            List<String> newPluginConfigurations = alreadyBindingPlugins.stream().map(EnvoyPluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());

            if (!publishToDiffTypeGw(proxyInfo, newPluginConfigurations)) {
                return Const.ERROR_RESULT;
            }
        }
        //使能状态修改为disable
        if (Const.ROUTE_RULE_DISABLE_STATE.equals(proxyInfo.getEnableState())) {
            if (!offlineToDiffTypeGw(proxyInfo)) {
                return Const.ERROR_RESULT;
            }
        }
        proxyInfo.setUpdateTime(System.currentTimeMillis());
        routeRuleProxyDao.update(proxyInfo);
        return proxyInfo.getId();
    }


    @Override
    public RouteRuleProxyInfo getRouteRuleProxy(long id) {
        return routeRuleProxyDao.get(id);
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyByRouteRuleId(long routeRuleId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("routeRuleId", routeRuleId);
        return routeRuleProxyDao.getRecordsByField(params);
    }

    @Override
    public RouteRuleProxyDto fromMeta(RouteRuleProxyInfo proxyInfo) {
        RouteRuleProxyDto proxyDto = new RouteRuleProxyDto();
        proxyDto.setCreateTime(proxyInfo.getCreateTime());
        proxyDto.setUpdateTime(proxyInfo.getUpdateTime());
        proxyDto.setGwId(proxyInfo.getGwId());
        proxyDto.setGwType(proxyInfo.getGwType());
        proxyDto.setId(proxyInfo.getId());
        proxyDto.setRouteRuleId(proxyInfo.getRouteRuleId());
        proxyDto.setPriority(proxyInfo.getPriority());
        proxyDto.setEnableState(proxyInfo.getEnableState());
        GatewayInfo gatewayInfo = gatewayInfoService.get(proxyInfo.getGwId());
        if (gatewayInfo != null) {
            proxyDto.setGwName(gatewayInfo.getGwName());
            proxyDto.setGwAddr(gatewayInfo.getGwAddr());
            proxyDto.setEnvId(gatewayInfo.getEnvId());
        }
        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(proxyInfo.getRouteRuleId());
        if (routeRuleInfo == null) {
            logger.info("路由元数据不存在，存在脏数据");
            return null;
        }
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(proxyInfo.getServiceId());
        ServiceProxyInfo proxyByServiceIdAndGwId = serviceProxyService.getServiceProxyByServiceIdAndGwId(proxyInfo.getGwId(), proxyInfo.getServiceId());
        proxyDto.setRouteRuleName(routeRuleInfo.getRouteRuleName());
        proxyDto.setServiceId(routeRuleInfo.getServiceId());
        proxyDto.setServiceName(serviceInfoDb.getDisplayName());
        proxyDto.setRouteRuleSource(routeRuleInfo.getRouteRuleSource());
        proxyDto.setHeaderOperation(JSON.parseObject(routeRuleInfo.getHeaderOperation(), EnvoyRouteRuleHeaderOperationDto.class));
        List<EnvoyDestinationDto> envoyDestinationDtos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(proxyInfo.getDestinationServiceList())) {
            //修改为当前for循环，减少数据库查询压力
            for (EnvoyDestinationInfo info : proxyInfo.getDestinationServiceList()) {
                EnvoyDestinationDto destinationDto = info.fromMeta();
                destinationDto.setApplicationName(proxyByServiceIdAndGwId.getBackendService());
                envoyDestinationDtos.add(destinationDto);
            }
            proxyDto.setDestinationServices(envoyDestinationDtos);
        }
        proxyDto.setServiceType(serviceInfoDb.getServiceType());
        proxyDto.setHosts(JSON.parseArray(proxyInfo.getHosts(), String.class));

        //构造match信息
        proxyDto.fromRouteMeta(proxyInfo);

        proxyDto.setTimeout(proxyInfo.getTimeout());
        proxyDto.setHttpRetryDto(proxyInfo.getHttpRetryDto());
        //路由指标监控
        if (StringUtils.isNotEmpty(proxyInfo.getVirtualCluster())) {
            proxyDto.setVirtualClusterDto(JSON.parseObject(proxyInfo.getVirtualCluster(), VirtualClusterDto.class));
        }

        //流量镜像配置
        if (StringUtils.isNotBlank(proxyInfo.getMirrorTraffic())) {
            proxyDto.setMirrorSwitch(1);
            proxyDto.setMirrorTraffic(JSON.parseObject(proxyInfo.getMirrorTraffic(), EnvoyDestinationInfo.class).fromMeta());
            ServiceProxyInfo proxyService = serviceProxyService.getServiceProxyByServiceIdAndGwId(proxyInfo.getGwId(), proxyDto.getMirrorTraffic().getServiceId());
            if (proxyService != null) {
                proxyDto.getMirrorTraffic().setApplicationName(proxyService.getBackendService());
            } else {
                proxyDto.setMirrorTraffic(null);
            }
        } else {
            proxyDto.setMirrorSwitch(0);
        }
        return proxyDto;
    }


    @Override
    public RouteRuleProxyInfo toMeta(RouteRuleProxyDto proxyDto) {
        RouteRuleProxyInfo routeRuleProxyInfo = new RouteRuleProxyInfo();
        routeRuleProxyInfo.setId(proxyDto.getId());
        routeRuleProxyInfo.setRouteRuleId(proxyDto.getRouteRuleId());
        routeRuleProxyInfo.setGwId(proxyDto.getGwId());
        routeRuleProxyInfo.setGwType(proxyDto.getGwType());

        RouteRuleInfo routeRuleInDb = routeRuleInfoService.getRouteRuleInfoById(proxyDto.getRouteRuleId());
        //构造服务id，用于按照服务id搜索
        routeRuleProxyInfo.setServiceId(routeRuleInDb.getServiceId());
        routeRuleProxyInfo.setProjectId(routeRuleInDb.getProjectId());

        //路由发布，构造routeProxy
        ServiceProxyInfo proxyByServiceIdAndGwId = serviceProxyService.getServiceProxyByServiceIdAndGwId(proxyDto.getGwId(),
                routeRuleInDb.getServiceId());
        List<EnvoyDestinationInfo> destinationInfos = proxyDto.getDestinationServices().stream()
                .map(EnvoyDestinationDto::toMeta).collect(Collectors.toList());
        if (Const.STATIC_PUBLISH_TYPE.equals(proxyByServiceIdAndGwId.getPublishType())) {
            //静态发布将端口设置为80
            destinationInfos.stream().forEach(envoyDestinationInfo -> {
                envoyDestinationInfo.setPort(80);
            });
        }

        routeRuleProxyInfo.setDestinationServiceList(destinationInfos);
        routeRuleProxyInfo.setDestinationServices(JSON.toJSONString(routeRuleProxyInfo.getDestinationServiceList()));

        routeRuleProxyInfo.setEnableState(proxyDto.getEnableState());
        routeRuleProxyInfo.setNeedRouteMetric(proxyDto.getNeedRouteMetric());
        //超时时间
        routeRuleProxyInfo.setTimeout(proxyDto.getTimeout());
        //路由重试
        if (proxyDto.getHttpRetryDto() != null) {
            routeRuleProxyInfo.setHttpRetryDto(proxyDto.getHttpRetryDto());
            routeRuleProxyInfo.setHttpRetry(JSON.toJSONString(proxyDto.getHttpRetryDto()));
        }

        if (proxyDto.getVirtualClusterDto() != null && StringUtils.isEmpty(proxyDto.getVirtualClusterDto().getVirtualClusterName())) {
            //构造VirtualCluster
            proxyDto.getVirtualClusterDto().setVirtualClusterName("vc-" + routeRuleInDb.getId());
            routeRuleProxyInfo.setVirtualCluster(JSON.toJSONString(proxyDto.getVirtualClusterDto()));
        }
        //构造uriMatch
        if (proxyDto.getUriMatchDto() == null) {
            proxyDto.fromRouteMeta(routeRuleInDb);
        }
        //构造routeMeta
        proxyDto.toRouteMeta(routeRuleProxyInfo);

        //设置流量镜像配置
        if (proxyDto.getMirrorTraffic() != null && proxyDto.getMirrorSwitch() == 1) {
            routeRuleProxyInfo.setMirrorTraffic(JSON.toJSONString(proxyDto.getMirrorTraffic()));
            routeRuleProxyInfo.setMirrorTrafficValue(proxyDto.getMirrorTraffic().toMeta());
        }
        return routeRuleProxyInfo;
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyListByServiceId(long gwId, long serviceId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("GwId", gwId);
        params.put("ServiceId", serviceId);
        return routeRuleProxyDao.getRecordsByField(params);
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyListByGwId(long gwId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("GwId", gwId);
        return routeRuleProxyDao.getRecordsByField(params);
    }

    @Override
    public ErrorCode checkPublishMirror(RouteRuleProxyDto routeRuleProxyDto) {
        RouteRuleProxyInfo routeRuleProxyInDb = getRouteRuleProxy(routeRuleProxyDto.getGwId(), routeRuleProxyDto.getRouteRuleId());
        if (routeRuleProxyInDb == null) {
            logger.info("发布流量镜像，路由未发布，不允许发布");
            return CommonErrorCode.RouteRuleNotPublished;
        }

        int mirrorSwitch = routeRuleProxyDto.getMirrorSwitch();
        if (mirrorSwitch != 0 && mirrorSwitch != 1) {
            return CommonErrorCode.InvalidParameterValue(mirrorSwitch, "mirrorSwitch");
        }

        if (mirrorSwitch == 0) {
            return CommonErrorCode.Success;
        }

        EnvoyDestinationDto mirrorTraffic = routeRuleProxyDto.getMirrorTraffic();
        if (mirrorTraffic == null) {
            logger.info("流量镜像配置不存在");
            return CommonErrorCode.MissingParameter("mirrorTraffic");
        }

        if (mirrorTraffic.getPort() <= 0 || mirrorTraffic.getPort() > 65535) {
            logger.info("端口设置错误");
            return CommonErrorCode.InvalidParameterValue(mirrorTraffic.getPort(), "流量镜像端口");
        }

        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(routeRuleProxyDto.getGwId(), mirrorTraffic.getServiceId());
        if (null == serviceProxyInfo) {
            logger.info("流量镜像指定服务未发布！serviceId:{}, gwId:{}", mirrorTraffic.getServiceId(), routeRuleProxyDto.getGwId());
            return CommonErrorCode.ServiceNotPublished;
        }

        if (StringUtils.isNotEmpty(mirrorTraffic.getSubsetName())) {
            if (StringUtils.isNotEmpty(serviceProxyInfo.getSubsets())) {
                List<EnvoySubsetDto> envoySubsetDtoObjectList = JSON.parseArray(serviceProxyInfo.getSubsets(), EnvoySubsetDto.class);
                List<EnvoySubsetDto> collect = envoySubsetDtoObjectList.stream().filter(subsetDto -> subsetDto.getName().equals(mirrorTraffic.getSubsetName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(collect)) {
                    logger.info("流量镜像指定服务版本配置错误");
                    return CommonErrorCode.InvalidSubsetName;
                }
            } else {
                logger.info("流量镜像指定服务版本配置错误");
                return CommonErrorCode.InvalidSubsetName;
            }
        }

        return CommonErrorCode.Success;
    }

    @Override
    public long publishRouteMirror(RouteRuleProxyDto routeProxyDto) {
        RouteRuleProxyInfo routeRuleProxyInDb = getRouteRuleProxy(routeProxyDto.getGwId(), routeProxyDto.getRouteRuleId());
        if (routeProxyDto.getMirrorSwitch() == 1) {
            routeRuleProxyInDb.setMirrorTraffic(JSON.toJSONString(routeProxyDto.getMirrorTraffic()));
            routeRuleProxyInDb.setMirrorTrafficValue(routeProxyDto.getMirrorTraffic().toMeta());
            routeRuleProxyInDb.setMirrorServiceId(routeProxyDto.getMirrorTraffic().getServiceId());
        } else {
            routeRuleProxyInDb.setMirrorServiceId(0);
            routeRuleProxyInDb.setMirrorTraffic("");
            routeRuleProxyInDb.setMirrorTrafficValue(null);
        }

        if (Const.ROUTE_RULE_ENABLE_STATE.equals(routeRuleProxyInDb.getEnableState())) {
            List<EnvoyPluginBindingInfo> alreadyBindingPlugins = envoyPluginInfoService.getEnablePluginBindingList(routeProxyDto.getGwId(),
                    String.valueOf(routeProxyDto.getRouteRuleId()), EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
            List<String> newPluginConfigurations = alreadyBindingPlugins.stream().map(EnvoyPluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
            if (!getFromApiPlaneService.publishRouteRuleByApiPlane(routeRuleProxyInDb, newPluginConfigurations)) {
                return Const.ERROR_RESULT;
            }
        }

        routeRuleProxyInDb.setUpdateTime(System.currentTimeMillis());
        return routeRuleProxyDao.update(routeRuleProxyInDb);
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyListByMirrorServiceIdAndGwId(long mirrorServiceId, long gwId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("mirrorServiceId", mirrorServiceId);
        params.put("gwId", gwId);
        return routeRuleProxyDao.getRecordsByField(params);
    }


    /**
     * 根据路由发布信息先获取不同类型的网关
     * 不同的类型的网关存在不同的发布逻辑
     * 因此需要区分调用
     *
     * @param routeRuleProxyInfo   路由发布信息
     * @param pluginConfigurations 插件配置
     * @return
     */
    private boolean publishToDiffTypeGw(RouteRuleProxyInfo routeRuleProxyInfo, List<String> pluginConfigurations) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(routeRuleProxyInfo.getGwId());
        if (gatewayInfo == null) {
            logger.warn("网关信息为空");
            return false;
        }
        boolean publishStatus;
        switch (GatewayEnum.getByType(gatewayInfo.getGwType())) {
            case ENVOY:
                publishStatus = getFromApiPlaneService.publishRouteRuleByApiPlane(routeRuleProxyInfo, pluginConfigurations);
                break;
            case SCG:
                publishStatus = getFromScgService.publishRouteToScgGw(routeRuleProxyInfo);
                break;
            default:
                publishStatus = false;
        }
        return publishStatus;
    }

    /**
     * 根据路由发布信息先获取不同类型的网关
     * 不同的类型的网关存在不同的发布逻辑
     * 因此需要区分调用
     *
     * @param routeRuleProxyInfo 路由发布信息
     * @return
     */
    private boolean offlineToDiffTypeGw(RouteRuleProxyInfo routeRuleProxyInfo) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(routeRuleProxyInfo.getGwId());
        if (gatewayInfo == null) {
            logger.warn("未找到对应网关,Id 为 {}", routeRuleProxyInfo.getRouteRuleId());
            return false;
        }
        boolean offlineStatus;

        switch (GatewayEnum.getByType(gatewayInfo.getGwType())) {
            case ENVOY:
                offlineStatus = getFromApiPlaneService.deleteRouteRuleByApiPlane(routeRuleProxyInfo);
                break;
            case SCG:
                offlineStatus = getFromScgService.offlineRouteToScgGw(routeRuleProxyInfo);
                break;
            default:
                offlineStatus = false;
        }
        return offlineStatus;
    }
}
