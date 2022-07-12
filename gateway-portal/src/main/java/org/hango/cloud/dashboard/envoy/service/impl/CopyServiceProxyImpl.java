package org.hango.cloud.dashboard.envoy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.ICopyRouteRuleProxy;
import org.hango.cloud.dashboard.envoy.service.ICopyServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CopyServiceProxyImpl implements ICopyServiceProxy {

    private static final Logger logger = LoggerFactory.getLogger(CopyServiceProxyImpl.class);

    @Autowired
    private ICopyRouteRuleProxy copyRouteRuleProxy;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;
    @Autowired
    private IServiceProxyService serviceProxyService;


    @Override
    public List<Long> copyServiceProxy(long serviceId, long originGwId, long desGwId) {
        ServiceProxyInfo serviceProxyInfoInDb = serviceProxyService.getServiceProxyByServiceIdAndGwId(originGwId, serviceId);
        if (serviceProxyInfoInDb == null) {
            return new ArrayList<>();
        }
        if (serviceProxyService.getServiceProxyByServiceIdAndGwId(desGwId, serviceId) == null) {
            serviceProxyInfoInDb.setGwId(desGwId);
            if (Const.ERROR_RESULT == serviceProxyService.publishServiceToGw(serviceProxyService.fromMeta(serviceProxyInfoInDb))) {
                logger.info("复制发布服务，发布至目标网关异常");
                return null;
            }
        }
        List<RouteRuleProxyInfo> routeRuleProxyList = routeRuleProxyService.getRouteRuleProxyListByServiceId(originGwId, serviceId);
        if (CollectionUtils.isEmpty(routeRuleProxyList)) return new ArrayList<>();

        return routeRuleProxyList.stream().filter(item ->
                        !copyRouteRuleProxy.copyRouteRuleProxy(item.getRouteRuleId(), originGwId, desGwId)).
                map(RouteRuleProxyInfo::getRouteRuleId).collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkCopyServiceProxy(List<Long> serviceIds, long originGwId, long desGwId) {
        if (CollectionUtils.isEmpty(serviceIds)) return CommonErrorCode.Success;
        for (Long serviceId : serviceIds) {
            return checkCopyServiceProxy(serviceId, originGwId, desGwId);
        }
        return CommonErrorCode.Success;
    }

    public ErrorCode checkCopyServiceProxy(long serviceId, long originGwId, long desGwId) {
        ServiceProxyInfo sourceServiceProxy = serviceProxyService.getServiceProxyByServiceIdAndGwId(originGwId, serviceId);
        if (sourceServiceProxy == null) {
            logger.info("复制已发布服务，原服务没有发布，禁止复制发布,服务id:{},网关id:{}", serviceId, originGwId);
            return CommonErrorCode.ServiceNotPublished;
        }
        ServiceProxyInfo desServiceProxy = serviceProxyService.getServiceProxyByServiceIdAndGwId(desGwId, serviceId);
        if (desServiceProxy == null) return CommonErrorCode.Success;
        if (!desServiceProxy.getBackendService().equals(sourceServiceProxy.getBackendService())) {
            logger.info("复制已发布服务，原服务发布地址和目标服务发布地址不同，禁止复制,服务id:{},原网关id:{},目标网关id：{}", new Object[]{serviceId, originGwId, desGwId});
            return CommonErrorCode.BackendServiceDifferent;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public Map<Long, List<Long>> copyServiceProxy(List<Long> serviceId, long originGwId, long desGwId) {
        if (CollectionUtils.isEmpty(serviceId)) return new HashMap<>(Const.DEFAULT_MAP_SIZE);
        return serviceId.stream().collect(Collectors.toMap(item -> item, item -> copyServiceProxy(item, originGwId, desGwId)));
    }
}
