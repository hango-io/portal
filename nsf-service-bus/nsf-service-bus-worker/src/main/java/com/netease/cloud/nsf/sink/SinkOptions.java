package com.netease.cloud.nsf.sink;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/19
 **/
public class SinkOptions {
    private String sinkId;
    private String sourceHost;
    private Integer sourcePort;
    private Long gRpcKeepAliveTime = 30000L;
    private Long gRpcKeepAliveTimeout = 10000L;
    private Long reestablishStreamDelay = 500L;

    public String getSinkId() {
        return sinkId;
    }

    public void setSinkId(String sinkId) {
        this.sinkId = sinkId;
    }

    public Long getReestablishStreamDelay() {
        return reestablishStreamDelay;
    }

    public void setReestablishStreamDelay(Long reestablishStreamDelay) {
        this.reestablishStreamDelay = reestablishStreamDelay;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public Integer getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    public Long getGRpcKeepAliveTime() {
        return gRpcKeepAliveTime;
    }

    public void setGRpcKeepAliveTime(Long gRpcKeepAliveTime) {
        this.gRpcKeepAliveTime = gRpcKeepAliveTime;
    }

    public Long getGRpcKeepAliveTimeout() {
        return gRpcKeepAliveTimeout;
    }

    public void setGRpcKeepAliveTimeout(Long gRpcKeepAliveTimeout) {
        this.gRpcKeepAliveTimeout = gRpcKeepAliveTimeout;
    }
}
