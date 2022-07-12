package com.netease.cloud.nsf.server;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/8/3
 **/
public interface ResourceWatcher {
    /**
     * watch新建立的连接，如果当前存在可用的Resource Snapshot，则立即返回。
     * 当ResourceDistributor调用setSnapshot方法时，为所有watch的Connection分发配置。
     * @param connection
     */
    void watch(Connection connection);

    /**
     * 释放watch的连接
     * @param connection
     */
    void release(Connection connection);
}
