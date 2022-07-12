package com.netease.cloud.nsf.sink;

import io.grpc.*;
import nsb.route.ResourceSourceGrpc;
import nsb.route.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/19
 **/
public class SinkClient {
    private static final Logger logger = LoggerFactory.getLogger(SinkClient.class);

    private SinkOptions options;
    private ResourceHandler handler;
    private ManagedChannel channel;
    private Service.SinkNode sinkNode;
    private ScheduledExecutorService delayExecutors;

    public SinkClient(SinkOptions options, ResourceHandler handler) {
        this.options = options;
        this.handler = handler;
        // init channel
        this.channel = ManagedChannelBuilder.
                forAddress(options.getSourceHost(), options.getSourcePort())
                .usePlaintext()
                .keepAliveTime(options.getGRpcKeepAliveTime(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(options.getGRpcKeepAliveTimeout(), TimeUnit.MILLISECONDS).build();
        // init sinkNode
        this.sinkNode = Service.SinkNode.newBuilder().setId(options.getSinkId()).build();
        this.delayExecutors = Executors.newSingleThreadScheduledExecutor();
    }

    public void establish() {
        logger.info("GRpc: establish a stream with server.");
        ClientCall<Service.RequestResources, Service.Resources> call = channel.newCall(ResourceSourceGrpc.getEstablishResourceStreamMethod(), CallOptions.DEFAULT);
        call.start(new ClientCall.Listener<Service.Resources>() {
            @Override
            public void onMessage(Service.Resources message) {
                if (Objects.isNull(message)) {
                    logger.warn("GRpc: receive none value message. skip.");
                    return;
                }
                logger.info("GRpc: receive message. nonce={} resource size={}", message.getNonce(), Objects.isNull(message.getResourcesList()) ? 0 : message.getResourcesList().size());
                try {
                    String responseNonce = handler.handler(message);
                    call.sendMessage(Service.RequestResources.newBuilder().setSinkNode(sinkNode).setResponseNonce(responseNonce).build());
                    call.request(1);
                } catch (Exception e) {
                    logger.info("GRpc: an error occurs when handler message. message nonce={} exception={}", message.getNonce(), e.getMessage());
                    e.printStackTrace();
                    reestablish(call, e);
                }
            }

            @Override
            public void onClose(Status status, Metadata trailers) {
                if (status.isOk()) {
                    logger.info("GRpc: stream closed by server.");
                    reestablish(call, null);
                } else {
                    RuntimeException exception = status.asRuntimeException(trailers);
                    logger.info("GRpc: an error occurs when exchange message: {}.", exception.getMessage());
                    exception.printStackTrace();
                    reestablish(call, exception);
                }
            }
        }, new Metadata());
        call.request(1);
        initRequest(call);
    }

    private void reestablish(ClientCall<Service.RequestResources, Service.Resources> call, @Nullable Throwable throwable) {
        call.cancel("an error occurs when exchange message.", throwable);
        Long delay = options.getReestablishStreamDelay();
        logger.info("GRpc: try reestablish connection. delay={}ms", delay);
        delayExecutors.schedule(this::establish, delay, TimeUnit.MILLISECONDS);
    }

    private void initRequest(ClientCall<Service.RequestResources, Service.Resources> call) {
        logger.info("GRpc: send init message.");
        // response nonce = ""， 表明是初始化请求
        call.sendMessage(Service.RequestResources.newBuilder().setSinkNode(sinkNode).setResponseNonce("").build());
    }
}
