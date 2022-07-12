package com.netease.cloud.nsf.server;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/8/3
 **/
public class ServerOptions {
    // grpc端口
    private int grpcPort = 8999;
    // 默认30s发送一次心跳
    private long grpcKeepAliveTime = 30000L;
    // 默认10s心跳请求超时
    private long grpcKeepAliveTimeout = 10000L;
    // 默认最大message为128M
    private int grpcMaxMessageSize = 128 * 1024 * 1024;
    // status轮询间隔，要求不能小于Snapshot的构建时间
    private long statusCheckInterval = 1000L;

    public int getGrpcPort() {
        return grpcPort;
    }

    public void setGrpcPort(int grpcPort) {
        this.grpcPort = grpcPort;
    }

    public long getGrpcKeepAliveTime() {
        return grpcKeepAliveTime;
    }

    public void setGrpcKeepAliveTime(long grpcKeepAliveTime) {
        this.grpcKeepAliveTime = grpcKeepAliveTime;
    }

    public long getGrpcKeepAliveTimeout() {
        return grpcKeepAliveTimeout;
    }

    public void setGrpcKeepAliveTimeout(long grpcKeepAliveTimeout) {
        this.grpcKeepAliveTimeout = grpcKeepAliveTimeout;
    }

    public int getGrpcMaxMessageSize() {
        return grpcMaxMessageSize;
    }

    public void setGrpcMaxMessageSize(int grpcMaxMessageSize) {
        this.grpcMaxMessageSize = grpcMaxMessageSize;
    }

    public long getStatusCheckInterval() {
        return statusCheckInterval;
    }

    public void setStatusCheckInterval(long statusCheckInterval) {
        this.statusCheckInterval = statusCheckInterval;
    }
}
