package org.hango.cloud.envoy.infra.route.hooker;

import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.invoker.MethodAroundHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.hooker.AbstractRouteHooker;
import org.hango.cloud.common.infra.route.pojo.RoutePO;
import org.hango.cloud.envoy.infra.plugin.manager.RoutePluginOperateService;
import org.hango.cloud.envoy.infra.route.service.IEnvoyRouteService;
import org.hango.cloud.envoy.infra.trafficmark.meta.TrafficMarkInfo;
import org.hango.cloud.envoy.infra.trafficmark.service.ITrafficMarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author xin li
 * @date 2022/9/8 15:26
 */
@Component
public class EnvoyRouteHooker extends AbstractRouteHooker<RoutePO, RouteDto> {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyRouteHooker.class);

    @Autowired
    private IEnvoyRouteService envoyRouteService;

    @Autowired
    private ITrafficMarkService trafficMarkService;

    @Autowired
    private RoutePluginOperateService routePluginOperateService;
    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    protected void preCreateHook(RouteDto routeDto) {
        // 禁用状态不需要发布服务
        if (BaseConst.ENABLE_STATE.equals(routeDto.getEnableState())
                && !envoyRouteService.publishRoute(routeDto)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected Object postCreateHook(Object returnData) {
        RouteDto routeDto = MethodAroundHolder.getNextParam(RouteDto.class);
        routeDto.setId(((long) returnData));
        ErrorCode errorCode = routePluginOperateService.create(routePluginOperateService.getRouteDefaultBindInfo(routeDto));
        if (!CommonErrorCode.SUCCESS.getCode().equals(errorCode.getCode())) {
            logger.error("create route plugin failed, routeId: {}, errorCode: {}", returnData, errorCode);
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return returnData;
    }



    @Override
    protected ErrorCode checkDeleteParam(ErrorCode returnCode) {
        RouteDto routeDto = MethodAroundHolder.getNextParam(RouteDto.class);
        if (routeDto == null) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        List<TrafficMarkInfo> trafficMarkInfos = trafficMarkService.getTrafficColorRulesByRouteId(routeDto.getId());
        if (!CollectionUtils.isEmpty(trafficMarkInfos)) {
            return CommonErrorCode.ROUTE_HAS_TRAFFIC_MARK_RULES;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    protected void preDeleteHook(RouteDto routeDto) {
        ErrorCode errorCode = routePluginOperateService.delete(routePluginOperateService.getRouteDefaultBindInfo(routeDto));
        if (!CommonErrorCode.SUCCESS.getCode().equals(errorCode.getCode())) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        boolean success = envoyRouteService.deleteRoute(routeDto);
        if (!success) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void preUpdateHook(RouteDto routeDto) {
        long result = envoyRouteService.updateRoute(routeDto);
        if (BaseConst.ERROR_RESULT == result) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}