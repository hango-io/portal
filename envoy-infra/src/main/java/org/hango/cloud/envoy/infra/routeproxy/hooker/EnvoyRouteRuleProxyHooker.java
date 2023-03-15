package org.hango.cloud.envoy.infra.routeproxy.hooker;

import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.invoker.MethodAroundHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.hooker.AbstractRouteRuleProxyHooker;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyInfo;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboBindingService;
import org.hango.cloud.envoy.infra.routeproxy.service.IEnvoyRouteRuleProxyService;
import org.hango.cloud.envoy.infra.trafficmark.meta.TrafficMarkInfo;
import org.hango.cloud.envoy.infra.trafficmark.service.ITrafficMarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author xin li
 * @date 2022/9/8 15:26
 */
@Component
@SuppressWarnings("unused")
public class EnvoyRouteRuleProxyHooker extends AbstractRouteRuleProxyHooker<RouteRuleProxyInfo, RouteRuleProxyDto> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRouteRuleProxyHooker.class);

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;
    @Autowired
    private IEnvoyRouteRuleProxyService envoyRouteRuleProxyService;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;

    @Autowired
    private IPluginInfoService pluginInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private IDubboBindingService dubboBindingService;

    @Autowired
    private ITrafficMarkService trafficMarkService;
    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    protected void preCreateHook(RouteRuleProxyDto routeRuleProxyDto) {
        //禁用状态不需要发布服务
        if (BaseConst.ROUTE_RULE_ENABLE_STATE.equals(routeRuleProxyDto.getEnableState())
                && !envoyRouteRuleProxyService.publishRouteProxy(routeRuleProxyDto, Collections.emptyList())) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

    }


    @Override
    protected ErrorCode checkDeleteParam(ErrorCode returnCode) {
        RouteRuleProxyDto routeRuleProxyDto = MethodAroundHolder.getNextParam(RouteRuleProxyDto.class);
        List<TrafficMarkInfo> trafficMarkInfos = trafficMarkService.getTrafficColorRulesByRouteRuleId(routeRuleProxyDto.getRouteRuleId());
        if (!CollectionUtils.isEmpty(trafficMarkInfos)) {
            return CommonErrorCode.ROUTE_HAS_TRAFFIC_MARK_RULES;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    protected void preDeleteHook(RouteRuleProxyDto routeRuleProxyDto) {
        boolean success = envoyRouteRuleProxyService.deleteRouteProxy(routeRuleProxyDto);
        if (!success) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void preUpdateHook(RouteRuleProxyDto routeRuleProxyDto) {
        long result = envoyRouteRuleProxyService.updateRouteProxy(routeRuleProxyDto);
        if (BaseConst.ERROR_RESULT == result) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
