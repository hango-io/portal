package com.netease.cloud.nsf.cluster;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.cluster.CamelClusterService;
import org.apache.camel.impl.cluster.ClusterServiceHelper;
import org.apache.camel.impl.cluster.ClusterServiceSelectors;
import org.apache.camel.impl.cluster.ClusteredRoutePolicy;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.RoutePolicy;
import org.apache.camel.spi.RoutePolicyFactory;
import org.apache.camel.support.ServiceSupport;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/22
 **/
public class NamespacedRoutePolicyFactory extends ServiceSupport implements RoutePolicyFactory, CamelContextAware {
    private static final Logger logger = LoggerFactory.getLogger(NamespacedRoutePolicyFactory.class);

    private CamelContext context;
    private CamelClusterService clusterService;
    private CamelClusterService.Selector clusterServiceSelector = ClusterServiceSelectors.DEFAULT_SELECTOR;

    private NamespacedRouteConfiguration namespacedRouteConfiguration;

    public NamespacedRoutePolicyFactory(NamespacedRouteConfiguration namespacedRouteConfiguration) {
        this.namespacedRouteConfiguration = namespacedRouteConfiguration;
    }

    @Override
    public RoutePolicy createRoutePolicy(CamelContext camelContext, String routeId, RouteDefinition route) {
        if (ObjectHelper.isNotEmpty(route.getRoutePolicies())) {
            // Check if the route is already configured with a clustered
            // route policy, in that case exclude it.
            if (route.getRoutePolicies().stream().anyMatch(ClusteredRoutePolicy.class::isInstance)) {
                logger.debug("Route '{}' has a ClusteredRoutePolicy already set-up", routeId);
                return null;
            }
        }

        String mode, namespace, name;
        // routeId 规范 {{mode}}/{{namespace}}/{{name}}
        String[] modeNamespaceName = routeId.split("/");
        if (modeNamespaceName.length == 1) {
            mode = namespacedRouteConfiguration.getDefaultMode();
            namespace = namespacedRouteConfiguration.getDefaultNamespace();
            name = modeNamespaceName[0];
        } else if (modeNamespaceName.length == 2) {
            mode = namespacedRouteConfiguration.getDefaultMode();
            namespace = modeNamespaceName[0];
            name = modeNamespaceName[1];
        } else if (modeNamespaceName.length == 3) {
            mode = modeNamespaceName[0];
            namespace = modeNamespaceName[1];
            name = modeNamespaceName[2];
        } else {
            throw new RuntimeCamelException(String.format("RouteId '%s' does not conform to the specification '%s'", routeId, "{{mode}}-{{namespace}}-{{name}}"));
        }
        try {
            if (namespacedRouteConfiguration.getWatchedNamespace().contains(namespace)) {
                if (NamespacedConst.MASTER_SERVER_MODE.equals(mode)) {
                    NamespacedRoutePolicy policy = NamespacedRoutePolicy.forNamespace(clusterService, namespace);
                    policy.setCamelContext(getCamelContext());
                    return policy;
                }
                if (NamespacedConst.PEER_TO_PEER_MODE.equals(mode)) {
                    return null;
                }
                throw new RuntimeCamelException(String.format("Unsupported mode %s", mode));
            } else {
                return new OutOfNamespacedRoutePolicy();
            }
        } catch (Exception e) {
            throw new RuntimeCamelException(e);
        }
    }

    public void setClusterService(CamelClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public void setClusterServiceSelector(CamelClusterService.Selector clusterServiceSelector) {
        this.clusterServiceSelector = clusterServiceSelector;
    }

    @Override
    protected void doStart() throws Exception {
        if (Objects.isNull(clusterService)) {
            clusterService = ClusterServiceHelper.mandatoryLookupService(context, clusterServiceSelector);
        }

        logger.debug("Using ClusterService instance {} (id={}, type={})", clusterService, clusterService.getId(), clusterService.getClass().getName());

        if (!ServiceHelper.isStarted(clusterService)) {
            clusterService.start();
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (ServiceHelper.isStarted(clusterService)) {
            // Stop the cluster service.
            clusterService.stop();
        }
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.context = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return this.context;
    }
}
