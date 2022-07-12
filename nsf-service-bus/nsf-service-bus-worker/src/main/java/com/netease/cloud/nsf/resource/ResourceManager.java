package com.netease.cloud.nsf.resource;

import nsb.route.Service;

/**
 * Resource资源管理
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/19
 **/
public interface ResourceManager {
    void setResources(Service.Resources resources);

    void clearResources();
}
