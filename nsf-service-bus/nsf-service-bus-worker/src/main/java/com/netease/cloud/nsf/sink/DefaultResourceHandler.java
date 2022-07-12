package com.netease.cloud.nsf.sink;

import com.netease.cloud.nsf.resource.ResourceManager;
import nsb.route.Service;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/20
 **/
public class DefaultResourceHandler implements ResourceHandler {

    private ResourceManager resourceManager;

    public DefaultResourceHandler(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public String handler(Service.Resources message) throws Exception {
        try {
            String nonce = message.getNonce();
            resourceManager.setResources(message);
            return nonce;
        } catch (RuntimeException e) {
            throw new Exception(e);
        }
    }
}
