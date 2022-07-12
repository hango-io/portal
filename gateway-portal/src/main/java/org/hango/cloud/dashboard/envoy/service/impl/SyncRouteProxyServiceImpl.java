package org.hango.cloud.dashboard.envoy.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.service.ISyncRouteProxyService;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.SyncRouteRuleGwDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 路由元信息同步impl,主要用于元信息同步。
 *
 * @author hanjiahao
 */
@Service
public class SyncRouteProxyServiceImpl implements ISyncRouteProxyService {
    private static final Logger logger = LoggerFactory.getLogger(CopyServiceProxyImpl.class);

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Override
    public long syncRouteProxy(long gwId, long routeId) {
        RouteRuleInfo routeRuleInfoDb = routeRuleInfoService.getRouteRuleInfoById(routeId);
        if (null == routeRuleInfoDb) {
            logger.error("同步路由规则，存在不存在的路由规则，存在脏数据，routeRuleId:{}", routeId);
            return Const.ERROR_RESULT;
        }
        RouteRuleProxyInfo routeRuleProxyInDb = routeRuleProxyService.getRouteRuleProxy(gwId, routeId);
        if (routeRuleProxyInDb == null) {
            logger.error("同步路由规则，路由未发布到同步网关,gwId:{}", gwId);
            return Const.ERROR_RESULT;
        }

        //uri
        routeRuleProxyInDb.setUri(routeRuleInfoDb.getUri());
        routeRuleProxyInDb.setUriMatchInfo(routeRuleInfoDb.getUriMatchInfo());
        //host
        routeRuleProxyInDb.setHost(routeRuleInfoDb.getHost());
        routeRuleProxyInDb.setHostMatchInfo(routeRuleInfoDb.getHostMatchInfo());
        //method
        routeRuleProxyInDb.setMethod(routeRuleInfoDb.getMethod());
        routeRuleProxyInDb.setMethodMatchInfo(routeRuleInfoDb.getMethodMatchInfo());
        //header
        routeRuleProxyInDb.setHeader(routeRuleInfoDb.getHeader());
        routeRuleProxyInDb.setHeaderList(routeRuleInfoDb.getHeaderList());
        //param
        routeRuleProxyInDb.setQueryParam(routeRuleInfoDb.getQueryParam());
        routeRuleProxyInDb.setQueryParamList(routeRuleInfoDb.getQueryParamList());
        routeRuleProxyInDb.setPriority(routeRuleInfoDb.getPriority());
        routeRuleProxyInDb.setOrders(routeRuleInfoDb.getOrders());

        return routeRuleProxyService.updateEnvoyRouteRuleProxy(routeRuleProxyInDb);
    }

    @Override
    public ErrorCode checkSyncRouteProxy(RouteRuleProxyDto routeRuleProxyDto) {
        List<Long> gwIds = routeRuleProxyDto.getGwIds();
        if (CollectionUtils.isEmpty(gwIds)) {
            logger.info("同步路由，网关信息为控");
            return CommonErrorCode.MissingParameter("GwIds");
        }
        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(routeRuleProxyDto.getRouteRuleId());
        if (null == routeRuleInfo) {
            logger.info("同步更新路由指定的路由规则不存在! routeRuleId:{}", routeRuleProxyDto.getRouteRuleId());
            return CommonErrorCode.NoSuchRouteRule;
        }
        List<Long> collectIds = gwIds.stream().filter(item -> routeRuleProxyService.getRouteRuleProxy(item, routeRuleInfo.getId()) == null).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collectIds)) {
            return CommonErrorCode.Success;
        }
        return CommonErrorCode.RouteRuleNotPublished;
    }


    @Override
    public List<String> syncRouteRuleBatch(List<Long> gwIds, RouteRuleProxyDto routeRuleProxyDto) {
        if (CollectionUtils.isEmpty(gwIds)) return new ArrayList<>();
        return gwIds.stream().filter(item -> {
            routeRuleProxyDto.setGwId(item);
            return Const.ERROR_RESULT == syncRouteProxy(item, routeRuleProxyDto.getRouteRuleId());
        }).map(item -> gatewayInfoService.get(item).getGwName()).collect(Collectors.toList());
    }

    @Override
    public List<SyncRouteRuleGwDto> describeGatewayForSyncRule(long routeId) {
        List<RouteRuleProxyInfo> routeProxyListInDb = routeRuleProxyService.getRouteRuleProxyByRouteRuleId(routeId);
        if (CollectionUtils.isEmpty(routeProxyListInDb)) return Lists.newArrayList();
        RouteRuleInfo routeRuleInfoInDb = routeRuleInfoService.getRouteRuleInfoById(routeId);
        return routeProxyListInDb.stream().map(item -> new SyncRouteRuleGwDto(GatewayDto.fromMeta(gatewayInfoService.get(item.getGwId())), routeRuleInfoInDb.isSame(item))).collect(Collectors.toList());
    }

    @Override
    public RouteRuleProxyInfo toSyncMeta(RouteRuleProxyDto routeRuleProxyDto) {
        RouteRuleProxyInfo proxyInfo = routeRuleProxyService.getRouteRuleProxy(routeRuleProxyDto.getGwId(), routeRuleProxyDto.getRouteRuleId());
        //构造routeMeta
        routeRuleProxyDto.toRouteMeta(proxyInfo);
        return proxyInfo;
    }
}
