package org.hango.cloud.envoy.infra.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author zhufengwei
 * @Date 2023/3/13
 */
public class EnvoyServicePortDTO {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Port")
    private Integer port;

    @JsonProperty("Protocol")
    private String protocol;

    @JsonProperty("NodePort")
    private Integer nodePort;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }
}
