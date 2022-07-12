package com.netease.cloud.nsf.server;

import io.grpc.stub.StreamObserver;
import nsb.route.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/8/3
 **/
public class Connection {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private final StreamObserver<Service.Resources> stream;
    private final ResourceWatcher watcher;
    private final ServerOptions options;
    private final String establishTime;

    public Connection(StreamObserver<Service.Resources> stream, ResourceWatcher watcher, ServerOptions options) {
        this.stream = stream;
        this.watcher = watcher;
        this.options = options;
        this.establishTime = String.format("%s %s", LocalDate.now(), LocalTime.now());
    }

    public StreamObserver<Service.Resources> getStream() {
        return stream;
    }

    public void processClientRequest(Service.RequestResources request) {
        if (StringUtils.isEmpty(request.getResponseNonce())) {
            logger.info("GRpc: establish new connection {} by node={}", this, request.getSinkNode().getId());
            watcher.watch(this);
        } else {
            if (request.hasErrorDetail()) {
                // NACK Response
                logger.warn("GRpc: connection {} node={} NACK with nonce={} error={}", // nolint: lll
                        this, request.getSinkNode().getId(), request.getResponseNonce(), request.getErrorDetail());
            } else {
                // ASK Response
                logger.info("GRpc: connection {} node={} ASK with nonce={}", this, request.getSinkNode().getId(), request.getResponseNonce());
            }
        }
    }

    public void push(Service.Resources snapshot) {
        logger.info("GRpc: connection {} SEND nonce={}", this, snapshot.getNonce());
        try {
            getStream().onNext(snapshot);
        } catch (Exception e) {
            logger.warn("GRpc: connection {} an error occurs when SEND nonce={} err={}",
                    this, snapshot.getNonce(), e.getMessage());
            this.close(e);
        }
    }

    public void close(Throwable throwable) {
        throwable.printStackTrace();
        logger.info("GRpc: close connection {}", this);
        try {
            getStream().onError(throwable);
        } catch (Exception e) {
            logger.warn("GRpc: connect{} an error occurs when CLOSE connection", this);
        } finally {
            watcher.release(this);
        }
    }
}
