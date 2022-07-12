package com.netease.cloud.nsf.resource;

/**
 * Resource资源变更监听器
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/19
 **/
public interface ResourceMonitor {
    void onAdd(nsb.route.ResourceOuterClass.Resource newRs);

    void onUpdate(nsb.route.ResourceOuterClass.Resource oldRs, nsb.route.ResourceOuterClass.Resource newRs);

    void onDelete(nsb.route.ResourceOuterClass.Resource oldRs);
}
