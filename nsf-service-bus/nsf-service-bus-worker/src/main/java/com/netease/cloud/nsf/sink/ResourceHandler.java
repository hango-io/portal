package com.netease.cloud.nsf.sink;

import nsb.route.Service;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/19
 **/
public interface ResourceHandler {
    /**
     * 处理Server接收到的Resources
     *
     * @param message message to handler
     * @return nonce
     * @throws Exception
     */
    String handler(Service.Resources message) throws Exception;
}
