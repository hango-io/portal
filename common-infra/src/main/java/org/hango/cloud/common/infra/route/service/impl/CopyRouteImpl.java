package org.hango.cloud.common.infra.route.service.impl;

import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.dto.CopyRouteDTO;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.dto.ServiceMetaForRouteDto;
import org.hango.cloud.common.infra.route.service.ICopyRoute;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.SCHEME_HTTP;

@Service
public class CopyRouteImpl implements ICopyRoute {

    private static final Logger logger = LoggerFactory.getLogger(CopyRouteImpl.class);

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IRouteService routeService;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayService;

    @Override
    public ErrorCode checkCopyRoute(long routeId, long originGwId, long desGwId) {
        if (originGwId == desGwId) {
            return CommonErrorCode.COPY_ROUTE_SAME_GW;
        }
        RouteDto routeDto = routeService.getRoute(originGwId, routeId);
        if (routeDto == null) {
            logger.error("复制路由，路由未发布到源网关");
            return CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED;
        }

        Long serviceId = routeDto.getServiceIds().get(0);
        ServiceProxyDto serviceProxy = serviceProxyService.get(serviceId);
        if (serviceProxy == null) {
            logger.error("复制路由，路由关联服务未发布到源网关");
            return CommonErrorCode.SERVICE_NOT_PUBLISHED;
        }

        if (!serviceProxy.getProtocol().equalsIgnoreCase(SCHEME_HTTP)) {
            logger.error("复制路由，路由关联服务非HTTP类型，关联的服务协议: {}", serviceProxy.getProtocol());
            return CommonErrorCode.COPY_ROUTE_ONLY_SUPPORT_HTTP_SERVICE;
        }

        Set<String> originHostList = serviceProxyService.getUniqueHostListFromServiceIdList(routeDto.getServiceIds());
        VirtualGatewayDto targetVirtualGw = virtualGatewayService.get(desGwId);
        List<DomainInfoDTO> domainInfos = targetVirtualGw.getDomainInfos();
        Set<String> targetVirtualGwHostList = domainInfos.stream().map(DomainInfoDTO::getHost).collect(Collectors.toSet());
        if (!targetVirtualGwHostList.containsAll(originHostList)) {
            logger.error("目标网关未配置与源网关相同的域名，无法完成路由复制");
            return CommonErrorCode.GATEWAY_NOT_BINDING_SAME_HOST;
        }
        // 流量镜像复制功能加入后开启
//        if (routeDto.getMirrorSwitch() == 1) {
//            long serviceId = routeDto.getMirrorTraffic().getServiceId();
//            ServiceProxyDto serviceProxyInfo = serviceProxyService.get(serviceId);
//            List<ServiceProxyDto> serviceProxy = serviceProxyService.getServiceProxy(ServiceProxyQuery.builder().pattern(
//                            serviceProxyInfo.getName())
//                    .virtualGwId(desGwId)
//                    .build());
//            if (CollectionUtils.isEmpty(serviceProxy)) {
//                logger.error("流量镜像指定服务未发布！serviceId:{}, virtualGwId:{}", serviceId, desGwId);
//                return CommonErrorCode.SERVICE_NOT_PUBLISHED;
//            }
//        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public CopyRouteDTO copyRoute(long routeId, long originGwId, long desGwId) {
        CopyRouteDTO result = new CopyRouteDTO();
        RouteDto originRoute = routeService.getRoute(originGwId, routeId);
        // 复制路由前，先复制关联服务
        Map<Long, Long> srcToTargetServiceIdMap = copyService(originRoute.getServiceIds(), desGwId);
        if (CollectionUtils.isEmpty(srcToTargetServiceIdMap)) {
            logger.error("[copy route] 复制路由失败，复制关联服务失败！ routeId: {}, originGwId: {}, desGwId: {}",
                    routeId, originGwId, desGwId);
            return null;
        }

        List<ServiceMetaForRouteDto> serviceMetaForRouteList = originRoute.getServiceMetaForRouteDtos();
        for (ServiceMetaForRouteDto serviceMeta : serviceMetaForRouteList) {
            Long originServiceId = serviceMeta.getServiceId();
            // 设置服务信息
            serviceMeta.setServiceId(srcToTargetServiceIdMap.get(originServiceId));

            List<DestinationDto> destinationServices = serviceMeta.getDestinationServices();
            if (!CollectionUtils.isEmpty(destinationServices)) {
                // 设置subset服务ID信息
                for (DestinationDto destinationService : destinationServices) {
                    destinationService.setServiceId(srcToTargetServiceIdMap.get(originServiceId));
                }
            }
        }
        RouteDto targetRoute = routeService.getRouteByNameInProjectGateway(originRoute.getName(), desGwId, originRoute.getProjectId());
        // 不复制流量镜像功能
        originRoute.setMirrorTraffic(null);
        originRoute.setMirrorSwitch(0);
        if (targetRoute == null) {
            logger.info("[copy route] 目标路由不存在，创建并发布路由，源路由ID： {}，源路由名称: {}",
                    originRoute.getId(), originRoute.getName());
            originRoute.setId(null);
            originRoute.setVirtualGwId(desGwId);
            long id = routeService.create(originRoute);
            result.setRouteId(id);
        } else {
            // 发布路由
            logger.info("[copy route] 目标路由已存在，修改并发布路由，源路由ID： {}，源路由名称: {}",
                    originRoute.getId(), originRoute.getName());
            originRoute.setId(targetRoute.getId());
            originRoute.setVirtualGwId(desGwId);
            routeService.update(originRoute);
            result.setRouteId(originRoute.getId());
        }
        // copy插件
        copyRoutePlugins(routeId, originGwId, desGwId);
        Set<Long> serviceIdList = result.getServiceIdList();
        serviceIdList.addAll(srcToTargetServiceIdMap.values());
        return result;
    }

    @Override
    @SuppressWarnings({"java:S3776"})
    public Map<Long, Long> copyService(List<Long> originServiceIdList, long desGwId) {
        List<ServiceProxyDto> originServiceList = serviceProxyService.getServiceByIds(originServiceIdList);
        List<String> originServiceNameList = originServiceList.stream().map(ServiceProxyDto::getName).collect(Collectors.toList());
        List<ServiceProxyDto> sameServiceListOfDesVirtualGw = serviceProxyService.getServiceProxy(
                ServiceProxyQuery.builder()
                        .nameList(originServiceNameList)
                        .virtualGwId(desGwId)
                        .projectId(ProjectTraceHolder.getProId())
                        .build());

        VirtualGatewayDto desVirtualGatewayDto = virtualGatewayService.get(desGwId);
        Map<Long, Long> srcToTargetServiceIdMap = new HashMap<>();

        for (ServiceProxyDto originService : originServiceList) {
            boolean existSameService = false;
            for (ServiceProxyDto sameService : sameServiceListOfDesVirtualGw) {
                if (originService.getName().equals(sameService.getName())) {
                    // 目标网关已存在同名服务，更新服务
                    existSameService = true;
                    srcToTargetServiceIdMap.put(originService.getId(), sameService.getId());
                    originService.setId(sameService.getId());
                    originService.setVirtualGwId(sameService.getVirtualGwId());
                    originService.setVirtualGwCode(sameService.getVirtualGwCode());
                    originService.setGwClusterName(sameService.getGwClusterName());
                    long update = serviceProxyService.update(originService);
                    if (update < 1) {
                        logger.error("[copy route] 复制路由失败，复制关联服务失败！更新目标项目网关服务失败 srcToTargetServiceIdMap: {}, targetServiceId: {}",
                                srcToTargetServiceIdMap, sameService.getId());
                        return Collections.EMPTY_MAP;
                    }
                    break;
                }
            }
            if (!existSameService) {
                // 复制服务
                Long originServiceId = originService.getId();
                originService.setId(null);
                originService.setVirtualGwId(desVirtualGatewayDto.getId());
                originService.setVirtualGwCode(desVirtualGatewayDto.getCode());
                originService.setGwClusterName(desVirtualGatewayDto.getGwClusterName());
                try {
                    long createResult = serviceProxyService.create(originService);
                    if (createResult < 1) {
                        logger.error("[copy route] 复制路由失败，复制关联服务失败！创建数据库服务失败 serviceId: {}", originService.getId());
                        return Collections.EMPTY_MAP;
                    }
                    srcToTargetServiceIdMap.put(originServiceId, createResult);
                } catch (Exception e) {
                    logger.error("[copy route] 复制路由失败，复制关联服务失败！ serviceId: {}", originService.getId(), e);
                    return Collections.EMPTY_MAP;
                }
            }
        }
        logger.info("[copy route] 复制路由服务ID映射 {originServiceId, targetServiceId}: {}", srcToTargetServiceIdMap);
        return srcToTargetServiceIdMap;
    }

    private void copyRoutePlugins(long routeId, long originGwId, long desGwId) {
        RouteDto originRoute = routeService.get(routeId);
        if (originRoute == null) {
            logger.error("copyRoutePlugins originRoute is null");
            return;
        }
        RouteDto targetRoute = routeService.getRouteByNameInProjectGateway(originRoute.getName(), desGwId, originRoute.getProjectId());
        if (targetRoute == null) {
            logger.error("copyRoutePlugins targetRoute is null");
            return;
        }
        List<PluginBindingDto> alreadyBindingPlugins = pluginInfoService.getPluginBindingList(originGwId, String.valueOf(originRoute.getId()),
                PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);

        //先清除目标网关对应路由的全部路由插件
        deleteDestGwRoutePlugins(targetRoute.getId(), desGwId);
        //将插件配置同步至目标网关
        List<PluginBindingDto> targetPluginList = alreadyBindingPlugins.stream()
                .peek(plugin -> plugin.setBindingObjectId(String.valueOf(targetRoute.getId())))
                .collect(Collectors.toList());
        createDestGwRoutePlugins(desGwId, targetPluginList);
    }

    private void createDestGwRoutePlugins(long desGwId, List<PluginBindingDto> alreadyBindingPlugins) {
        if (!CollectionUtils.isEmpty(alreadyBindingPlugins)) {
            alreadyBindingPlugins.forEach(pluginBindingInfo -> {
                //原插件发布网关改为目标网关
                pluginBindingInfo.setVirtualGwId(desGwId);
                pluginInfoService.create(pluginBindingInfo);
            });
        }
    }

    private void deleteDestGwRoutePlugins(long routeRuleId, long desGwId) {
        List<PluginBindingDto> alreadyBindingPluginDes = pluginInfoService.getPluginBindingList(desGwId, String.valueOf(routeRuleId),
                PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        alreadyBindingPluginDes.forEach(item -> pluginInfoService.delete(item));
    }
}
