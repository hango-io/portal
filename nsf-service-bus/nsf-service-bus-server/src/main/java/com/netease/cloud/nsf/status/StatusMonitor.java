package com.netease.cloud.nsf.status;

import java.util.function.BiConsumer;

/**
 * status监听器，status保存了当前资源的状态，当status更新时，说明资源也有更新。会触发一次构建Snapshot并分发的过程。
 * 默认实现status保存在status表，定期轮询status value是否发生改变。
 *
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public interface StatusMonitor {
    /**
     * 注册status event handler
     * @param key
     * @param handle
     */
    void registerHandler(String key, BiConsumer<Event, Status.Property> handle);

    /**
     * 启动StatusMonitor
     */
    void start();

    /**
     * 停止StatusMonitor
     */
    void shutdown();

    enum Event {
        ADD, UPDATE, DELETE
    }
}