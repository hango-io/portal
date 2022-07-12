package com.netease.cloud.nsf.server;

import io.grpc.stub.StreamObserver;
import nsb.route.ResourceSourceGrpc;
import nsb.route.Service;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/8/3
 **/
public class ResourceSourceImpl extends ResourceSourceGrpc.ResourceSourceImplBase {

    private ResourceWatcher watcher;
    private ServerOptions options;

    public ResourceSourceImpl(ResourceWatcher watcher, ServerOptions options) {
        this.watcher = watcher;
        this.options = options;
    }

    @Override
    public StreamObserver<Service.RequestResources> establishResourceStream(StreamObserver<Service.Resources> responseObserver) {
        Connection conn = new Connection(responseObserver, watcher, options);
        return new StreamObserver<Service.RequestResources>() {
            @Override
            public void onNext(Service.RequestResources requestResources) {
                conn.processClientRequest(requestResources);
            }

            @Override
            public void onError(Throwable throwable) {
                conn.close(throwable);
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}
