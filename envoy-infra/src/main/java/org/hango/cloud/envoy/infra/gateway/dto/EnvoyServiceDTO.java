package org.hango.cloud.envoy.infra.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/3/13
 */
public class EnvoyServiceDTO {

    @JsonProperty("GwClusterName")
    private String gwClusterName;

    @JsonProperty("ServiceType")
    private String serviceType;


    /**
     * clusterIp: 内部访问地址不展示ip:port
     * nodeport: ip:port 列表,ip 为网关pod所在节点的ip地址，port为动态生成nodeport
     * loadbalance: ip:port 列表, ip 4层lb的外部访问地址， port为监听端口
     * hostnetwork: ip:port 列表,  ip 为网关pod所在节点的ip地址，port为监听端口。
     */
    @JsonProperty("Ports")
    private List<EnvoyServicePortDTO> ports;

    @JsonProperty("Ips")
    private List<String> ips;

    public String getGwClusterName() {
        return gwClusterName;
    }

    public void setGwClusterName(String gwClusterName) {
        this.gwClusterName = gwClusterName;
    }

    public List<EnvoyServicePortDTO> getPorts() {
        return ports;
    }

    public void setPorts(List<EnvoyServicePortDTO> ports) {
        this.ports = ports;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }
}
