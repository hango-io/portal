package com.netease.cloud.nsf.server;

import nsb.route.Service;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/8/3
 **/
public interface ResourceDistributor {
    /**
     * 当调用setSnapshot时，会将最新Resource快照替换当前Resource快照。并且如果存在watch的Connection，则为每个Connection分发Resource。
     * @param resources
     */
    void setSnapshot(Service.Resources resources);

    /**
     * 清空Snapshot
     */
    void clearSnapshot();
}
