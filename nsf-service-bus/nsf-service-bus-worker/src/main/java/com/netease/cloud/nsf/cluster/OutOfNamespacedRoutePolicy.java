package com.netease.cloud.nsf.cluster;

import org.apache.camel.Route;
import org.apache.camel.support.RoutePolicySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/24
 **/
public class OutOfNamespacedRoutePolicy extends RoutePolicySupport {

    private static final Logger logger = LoggerFactory.getLogger(OutOfNamespacedRoutePolicy.class);

    @Override
    public void onInit(Route route) {
        logger.info("Route managed by {}. Setting route {} AutoStartup flag to false.", getClass(), route.getId());
        route.getRouteContext().getRoute().setAutoStartup("false");
    }
}
