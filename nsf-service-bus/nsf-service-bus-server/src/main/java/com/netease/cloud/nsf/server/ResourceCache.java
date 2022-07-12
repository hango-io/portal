package com.netease.cloud.nsf.server;

import nsb.route.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/8/4
 **/
public class ResourceCache implements ResourceDistributor, ResourceWatcher {
    private static final Logger logger = LoggerFactory.getLogger(ResourceCache.class);

    private ExecutorService scheduleThread = Executors.newSingleThreadExecutor();

    private Service.Resources snapshot;

    private final Set<Connection> connections = new HashSet<>();

    @Override
    public synchronized void setSnapshot(Service.Resources resources) {
        this.snapshot = resources;
        scheduleThread.execute(() -> distribute(snapshot));
    }

    @Override
    public synchronized void clearSnapshot() {
        snapshot = null;
    }


    @Override
    public synchronized void watch(Connection connection) {
        connections.add(connection);
        if (Objects.nonNull(snapshot)) {
            scheduleThread.execute(() -> distribute(connection, snapshot));
        }
    }

    @Override
    public synchronized void release(Connection connection) {
        connections.remove(connection);
        logger.info("MCP: release connection:{}, remain count:{}", connection, connections.size());
    }

    private void distribute(Service.Resources snapshot) {
        for (Connection connection : connections) {
            distribute(connection, snapshot);
        }
    }

    private void distribute(Connection connection, Service.Resources snapshot) {
        connection.push(snapshot);
    }
}
