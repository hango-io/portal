package com.netease.cloud.nsf.cluster;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/24
 **/
public class NamespacedRouteConfiguration {
    private final String defaultNamespace;
    /**
     * 有两种模式：1. p2p 2. ms
     * 1. p2p 平级架构，该namespace下的所有实例都会运行该路由，常见需要外部触发的路由用该模式
     * 2. ms  主从架构，该namespace下只有一个实例会运行该路由，常见内部触发的路由用该模式
     */
    private final String defaultMode;
    private final List<String> watchedNamespace;

    public NamespacedRouteConfiguration(String defaultNamespace, String defaultMode, List<String> watchedNamespace) {
        this.defaultNamespace = defaultNamespace;
        this.defaultMode = defaultMode;
        this.watchedNamespace = watchedNamespace;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public String getDefaultMode() {
        return defaultMode;
    }

    public List<String> getWatchedNamespace() {
        return watchedNamespace;
    }
}
