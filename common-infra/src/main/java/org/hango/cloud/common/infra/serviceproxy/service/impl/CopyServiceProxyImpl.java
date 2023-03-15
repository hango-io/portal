package org.hango.cloud.common.infra.serviceproxy.service.impl;

import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.service.ICopyRouteRuleProxy;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.serviceproxy.service.ICopyServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
    public List<Long> copyServiceProxy(long serviceId, long originGwId, long de) {
        ServiceProxyDto serviceProxyInfoInDb = serviceProxyService.getServiceProxyByServiceIdAndGwId(originGwId, serviceId);
        if (serviceProxyInfoInDb == null) {
            return new ArrayList<>();
        }
        if (serviceProxyService.getServiceProxyByServiceIdAndGwId(de, serviceId) == null) {
            serviceProxyInfoInDb.setVirtualGwId(de);
            if (BaseConst.ERROR_RESULT == serviceProxyService.create(serviceProxyInfoInDb)) {
                logger.info("复制发布服务，发布至目标网关异常");
                return null;
            }
        }
        RouteRuleQuery query = RouteRuleQuery.builder().virtualGwId(originGwId).serviceId(serviceId).build();
        List<RouteRuleProxyDto> routeRuleProxyList = routeRuleProxyService.getRouteRuleProxyList(query);
        if (CollectionUtils.isEmpty(routeRuleProxyList)) {
            return new ArrayList<>();
        }

        return routeRuleProxyList.stream().map(RouteRuleProxyDto::getRouteRuleId).filter(routeRuleId -> !copyRouteRuleProxy.copyRouteRuleProxy(routeRuleId, originGwId, de)).collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkCopyServiceProxy(List<Long> serviceIds, long originGwId, long de) {
        if (CollectionUtils.isEmpty(serviceIds)) {
            return CommonErrorCode.SUCCESS;
        }
        for (Long serviceId : serviceIds) {
            return checkCopyServiceProxy(serviceId, originGwId, de);
        }
        return CommonErrorCode.SUCCESS;
    }

    public ErrorCode checkCopyServiceProxy(long serviceId, long originGwId, long de) {
        ServiceProxyDto sourceServiceProxy = serviceProxyService.getServiceProxyByServiceIdAndGwId(originGwId, serviceId);
        if (sourceServiceProxy == null) {
            logger.info("复制已发布服务，原服务没有发布，禁止复制发布,服务id:{},网关id:{}", serviceId, originGwId);
            return CommonErrorCode.SERVICE_NOT_PUBLISHED;
        }
        ServiceProxyDto desServiceProxy = serviceProxyService.getServiceProxyByServiceIdAndGwId(de, serviceId);
        if (desServiceProxy == null) {
            return CommonErrorCode.SUCCESS;
        }
        if (!desServiceProxy.getBackendService().equals(sourceServiceProxy.getBackendService())) {
            logger.info("复制已发布服务，原服务发布地址和目标服务发布地址不同，禁止复制,服务id:{},原网关id:{},目标网关id：{}", new Object[]{serviceId, originGwId, de});
            return CommonErrorCode.BACKEND_SERVICE_DIFFERENT;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public Map<Long, List<Long>> copyServiceProxy(List<Long> serviceId, long originGwId, long de) {
        if (CollectionUtils.isEmpty(serviceId)) {
            return Maps.newHashMap();
        }
        return serviceId.stream().collect(Collectors.toMap(item -> item, item -> copyServiceProxy(item, originGwId, de)));
    }
}
