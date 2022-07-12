package com.netease.cloud.nsf.resource;

import nsb.route.ResourceOuterClass;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.ModelHelper;

import java.io.ByteArrayInputStream;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/26
 **/
public class DefaultResourceMonitor implements ResourceMonitor {

    private ModelCamelContext camelContext;

    public DefaultResourceMonitor(ModelCamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public void onAdd(ResourceOuterClass.Resource newRs) {
        try {
            camelContext.addRouteDefinitions(ModelHelper.loadRoutesDefinition(camelContext, new ByteArrayInputStream(newRs.getBody().getBytes())).getRoutes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(ResourceOuterClass.Resource oldRs, ResourceOuterClass.Resource newRs) {
        try {
            camelContext.addRouteDefinitions(ModelHelper.loadRoutesDefinition(camelContext, new ByteArrayInputStream(newRs.getBody().getBytes())).getRoutes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDelete(ResourceOuterClass.Resource oldRs) {
        try {
            camelContext.stopRoute(oldRs.getMetadata().getName());
            camelContext.removeRoute(oldRs.getMetadata().getName());
            camelContext.removeRouteDefinitions(ModelHelper.loadRoutesDefinition(camelContext, new ByteArrayInputStream(oldRs.getBody().getBytes())).getRoutes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
