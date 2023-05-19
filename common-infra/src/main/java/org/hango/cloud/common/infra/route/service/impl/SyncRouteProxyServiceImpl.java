package org.hango.cloud.common.infra.route.service.impl;

import com.google.common.collect.Lists;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.route.dto.RouteMapMatchDto;
import org.hango.cloud.common.infra.route.dto.RouteMatchDto;
import org.hango.cloud.common.infra.route.dto.RouteStringMatchDto;
import org.hango.cloud.common.infra.route.dto.RouteSyncDto;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.dto.SyncRouteGwDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.route.service.ISyncRouteProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 路由元信息同步impl,主要用于元信息同步。
 *
 * @author hanjiahao
 */
@Service
public class SyncRouteProxyServiceImpl implements ISyncRouteProxyService {
    private static final Logger logger = LoggerFactory.getLogger(SyncRouteProxyServiceImpl.class);

    @Autowired
    private IRouteService routeService;
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Override
    public List<String> syncRouteProxy(RouteSyncDto syncDto) {
        List<String> errorNames = new ArrayList<>();
        RouteDto routeDtoFromDb = routeService.get(syncDto.getRouteRuleId());
        for (Long virtualGwId : syncDto.getVirtualGwIds()) {
            RouteDto routeRuleProxyInDb = routeService.getRoute(virtualGwId, syncDto.getRouteRuleId());
            //uri
            routeRuleProxyInDb.setUriMatchDto(routeDtoFromDb.getUriMatchDto());
            //method
            routeRuleProxyInDb.setMethod(routeDtoFromDb.getMethod());
            //header
            routeRuleProxyInDb.setHeaders(routeDtoFromDb.getHeaders());
            //param
            routeRuleProxyInDb.setQueryParams(routeDtoFromDb.getQueryParams());
            routeRuleProxyInDb.setPriority(routeDtoFromDb.getPriority());
            routeRuleProxyInDb.setOrders(routeDtoFromDb.getOrders());
            long result = routeService.update(routeRuleProxyInDb);
            if (BaseConst.ERROR_RESULT == result){
                VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
                errorNames.add(virtualGatewayDto.getName());
            }
        }
        return errorNames;
    }

    @Override
    public ErrorCode checkSyncRouteProxy(RouteSyncDto routeSyncDto) {
        RouteDto routeDto = routeService.get(routeSyncDto.getRouteRuleId());
        if (null == routeDto) {
            logger.info("同步更新路由指定的路由规则不存在! routeRuleId:{}", routeSyncDto.getRouteRuleId());
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }
        List<Long> collectIds = routeSyncDto.getVirtualGwIds().stream().filter(item -> routeService.getRoute(item, routeDto.getId()) == null).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collectIds)) {
            return CommonErrorCode.SUCCESS;
        }
        return CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED;
    }

    @Override
    public List<SyncRouteGwDto> describeGatewayForSyncRule(long routeId) {
        List<RouteDto> routeDtos = routeService.getRouteById(routeId);
        if (CollectionUtils.isEmpty(routeDtos)) {
            return Lists.newArrayList();
        }
        RouteDto routeDtoFromDb = routeService.get(routeId);
        List<SyncRouteGwDto> syncRouteGwDtos = new ArrayList<>();
        for (RouteDto routeDto : routeDtos) {
            Long virtualGwId = routeDto.getVirtualGwId();
            VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
            SyncRouteGwDto syncRouteGwDto = SyncRouteGwDto.builder()
                    .virtualGwId(virtualGwId)
                    .envId(virtualGatewayDto.getEnvId())
                    .name(virtualGatewayDto.getName())
                    .build();
            syncRouteGwDto.setIsSameRaw(sameMatchInfo(routeDto, routeDtoFromDb));
            syncRouteGwDtos.add(syncRouteGwDto);
        }
        return syncRouteGwDtos;
    }

    private boolean sameMatchInfo(RouteMatchDto matchInfo1, RouteMatchDto matchInfo2){
        return matchInfo1.getPriority() == matchInfo2.getPriority()
                && equals(matchInfo1.getUriMatchDto(), matchInfo2.getUriMatchDto())
                && equalsList(matchInfo1.getMethod(), matchInfo2.getMethod())
                && equals(matchInfo1.getHeaders(), matchInfo2.getHeaders())
                && equals(matchInfo1.getQueryParams(), matchInfo2.getQueryParams());
    }

    private boolean equals(RouteStringMatchDto matchDto1, RouteStringMatchDto matchDto2){
        if (matchDto1 == null){
            return matchDto2 == null;
        }else {
            if (matchDto2 == null){
                return false;
            }
        }
        return matchDto1.getType().equals(matchDto2.getType())
                && CommonUtil.equalIgnoreSeq(matchDto1.getValue(), matchDto2.getValue());
    }

    private boolean equalsList(List<String> methodList1, List<String> methodList2){
        if (methodList1 == null){
            return methodList2 == null;
        } else {
            if (methodList2 == null){
                return false;
            }
        }
        methodList1.retainAll(methodList2);
        return methodList1.size() == methodList2.size();
    }

    private boolean equals(RouteMapMatchDto matchDto1, RouteMapMatchDto matchDto2){
        if (matchDto1 == null){
            return matchDto2 == null;
        }else {
            if (matchDto2 == null){
                return false;
            }
        }
        return matchDto1.getType().equals(matchDto2.getType())
                && matchDto1.getKey().equals(matchDto2.getKey())
                && CommonUtil.equalIgnoreSeq(matchDto1.getValue(), matchDto2.getValue());
    }

    private boolean equals(List<RouteMapMatchDto> mapMatchDtos1, List<RouteMapMatchDto> mapMatchDtos2){
        if (mapMatchDtos1 == null){
            mapMatchDtos1 = new ArrayList<>();
        }
        if (mapMatchDtos2 == null){
            mapMatchDtos2 = new ArrayList<>();
        }
        if (mapMatchDtos1.size() != mapMatchDtos2.size()){
            return false;
        }
        mapMatchDtos1 = mapMatchDtos1.stream().sorted(Comparator.comparing(RouteMapMatchDto::getKey)).collect(Collectors.toList());
        mapMatchDtos2 = mapMatchDtos2.stream().sorted(Comparator.comparing(RouteMapMatchDto::getKey)).collect(Collectors.toList());
        for (int i = 0; i < mapMatchDtos1.size(); i++) {
            if (!equals(mapMatchDtos1.get(i), mapMatchDtos2.get(i))){
                return false;
            }
        }
        return true;
    }


}
