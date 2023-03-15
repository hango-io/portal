package org.hango.cloud.envoy.infra.grpc.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author xin li
 * @date 2022/5/19 16:23
 */
public class GrpcEnvoyFilterDto {
    /**
     * 网关
     */
    @JSONField(name = "GwCluster")
    private String gwCluster;
    /**
     * 网关的端口号
     */
    @JSONField(name = "PortNumber")
    private int portNumber;

    /**
     * pb文件
     */
    @JSONField(name = "ProtoDescriptorBin")
    private String protoDescriptorBin;

    /**
     * 要支持协议转换的services
     */
    @JSONField(name = "Services")
    private List<String> services;

    public GrpcEnvoyFilterDto() {
    }

    public GrpcEnvoyFilterDto(String gwCluster, int portNumber, String protoDescriptorBin, List<String> services) {
        this.gwCluster = gwCluster;
        this.portNumber = portNumber;
        this.protoDescriptorBin = protoDescriptorBin;
        this.services = services;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }

    public String getProtoDescriptorBin() {
        return protoDescriptorBin;
    }

    public void setProtoDescriptorBin(String protoDescriptorBin) {
        this.protoDescriptorBin = protoDescriptorBin;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }
}
