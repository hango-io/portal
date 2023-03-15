package org.hango.cloud.common.infra.routeproxy.service.impl;

import com.google.common.collect.Lists;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.route.common.RouteRuleMapMatchDto;
import org.hango.cloud.common.infra.route.common.RouteRuleMatchDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteStringMatchDto;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.routeproxy.dto.RouteProxySyncDto;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.dto.SyncRouteRuleGwDto;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.routeproxy.service.ISyncRouteProxyService;
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
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Override
    public List<String> syncRouteProxy(RouteProxySyncDto syncDto) {
        List<String> errorNames = new ArrayList<>();
        RouteRuleDto routeRuleInfoDb = routeRuleInfoService.get(syncDto.getRouteRuleId());
        for (Long virtualGwId : syncDto.getVirtualGwIds()) {
            RouteRuleProxyDto routeRuleProxyInDb = routeRuleProxyService.getRouteRuleProxy(virtualGwId, syncDto.getRouteRuleId());
            //uri
            routeRuleProxyInDb.setUriMatchDto(routeRuleInfoDb.getUriMatchDto());
            //host
            routeRuleProxyInDb.setHostMatchDto(routeRuleInfoDb.getHostMatchDto());
            //method
            routeRuleProxyInDb.setMethodMatchDto(routeRuleInfoDb.getMethodMatchDto());
            //header
            routeRuleProxyInDb.setHeaders(routeRuleInfoDb.getHeaders());
            //param
            routeRuleProxyInDb.setQueryParams(routeRuleInfoDb.getQueryParams());
            routeRuleProxyInDb.setPriority(routeRuleInfoDb.getPriority());
            routeRuleProxyInDb.setOrders(routeRuleInfoDb.getOrders());
            long result = routeRuleProxyService.update(routeRuleProxyInDb);
            if (BaseConst.ERROR_RESULT == result){
                VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
                errorNames.add(virtualGatewayDto.getName());
            }
        }
        return errorNames;
    }

    @Override
    public ErrorCode checkSyncRouteProxy(RouteProxySyncDto routeProxySyncDto) {
        RouteRuleDto routeRuleInfo = routeRuleInfoService.get(routeProxySyncDto.getRouteRuleId());
        if (null == routeRuleInfo) {
            logger.info("同步更新路由指定的路由规则不存在! routeRuleId:{}", routeProxySyncDto.getRouteRuleId());
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }
        List<Long> collectIds = routeProxySyncDto.getVirtualGwIds().stream().filter(item -> routeRuleProxyService.getRouteRuleProxy(item, routeRuleInfo.getId()) == null).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collectIds)) {
            return CommonErrorCode.SUCCESS;
        }
        return CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED;
    }

    @Override
    public List<SyncRouteRuleGwDto> describeGatewayForSyncRule(long routeId) {
        List<RouteRuleProxyDto> routeRuleProxyDtos = routeRuleProxyService.getRouteRuleProxyByRouteRuleId(routeId);
        if (CollectionUtils.isEmpty(routeRuleProxyDtos)) {
            return Lists.newArrayList();
        }
        RouteRuleDto routeRuleInfoInDb = routeRuleInfoService.get(routeId);
        List<SyncRouteRuleGwDto> syncRouteRuleGwDtos = new ArrayList<>();
        for (RouteRuleProxyDto routeRuleProxyDto : routeRuleProxyDtos) {
            Long virtualGwId = routeRuleProxyDto.getVirtualGwId();
            VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
            SyncRouteRuleGwDto syncRouteRuleGwDto = SyncRouteRuleGwDto.builder()
                    .virtualGwId(virtualGwId)
                    .envId(virtualGatewayDto.getEnvId())
                    .name(virtualGatewayDto.getName())
                    .build();
            syncRouteRuleGwDto.setIsSameRaw(sameMatchInfo(routeRuleProxyDto, routeRuleInfoInDb));
            syncRouteRuleGwDtos.add(syncRouteRuleGwDto);
        }
        return syncRouteRuleGwDtos;
    }

    private boolean sameMatchInfo(RouteRuleMatchDto matchInfo1, RouteRuleMatchDto matchInfo2){
        return matchInfo1.getPriority() == matchInfo2.getPriority()
                && equals(matchInfo1.getUriMatchDto(), matchInfo2.getUriMatchDto())
                && equals(matchInfo1.getHostMatchDto(), matchInfo2.getHostMatchDto())
                && equals(matchInfo1.getMethodMatchDto(), matchInfo2.getMethodMatchDto())
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

    private boolean equals(RouteRuleMapMatchDto matchDto1, RouteRuleMapMatchDto matchDto2){
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

    private boolean equals(List<RouteRuleMapMatchDto> mapMatchDtos1, List<RouteRuleMapMatchDto> mapMatchDtos2){
        if (mapMatchDtos1 == null){
            mapMatchDtos1 = new ArrayList<>();
        }
        if (mapMatchDtos2 == null){
            mapMatchDtos2 = new ArrayList<>();
        }
        if (mapMatchDtos1.size() != mapMatchDtos2.size()){
            return false;
        }
        mapMatchDtos1 = mapMatchDtos1.stream().sorted(Comparator.comparing(RouteRuleMapMatchDto::getKey)).collect(Collectors.toList());
        mapMatchDtos2 = mapMatchDtos2.stream().sorted(Comparator.comparing(RouteRuleMapMatchDto::getKey)).collect(Collectors.toList());
        for (int i = 0; i < mapMatchDtos1.size(); i++) {
            if (!equals(mapMatchDtos1.get(i), mapMatchDtos2.get(i))){
                return false;
            }
        }
        return true;
    }


}
