package org.hango.cloud.envoy.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceTrafficPolicyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SubsetDto;


import javax.validation.Valid;
import java.util.List;


/**
 * 发布服务相关info，与api-plane进行通信
 *
 * @author hanjiahao
 */
public class DpServiceProxyDto {
    /**
     * 服务唯一标识
     */
    @JSONField(name = "Code")
    private String code;
    /**
     * 网关集群名称
     */
    @JSONField(name = "Gateway")
    private String gateway;
    /**
     * 后端服务
     */
    @JSONField(name = "BackendService")
    private String backendService;

    /**
     * 服务注册类型
     */
    @JSONField(name = "Type")
    private String type;

    @JSONField(name = "Protocol")
    private String protocol;

    /**
     * 服务发布服务标识
     */
    @JSONField(name = "ServiceTag")
    private String serviceTag;

    @JSONField(name = "LoadBalancer")
    private String loadBalancer;

    /**
     * 版本
     */
    @Valid
    @JSONField(name = "Subsets")
    private List<SubsetDto> subsets;

    /**
     * 高级配置，包含负载均衡和连接池
     */
    @JSONField(name = "TrafficPolicy")
    private ServiceTrafficPolicyDto trafficPolicy;

    /**
     * 版本号
     */
    @JSONField(name = "Version")
    private Long version;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getBackendService() {
        return backendService;
    }

    public void setBackendService(String backendService) {
        this.backendService = backendService;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public List<SubsetDto> getSubsets() {
        return subsets;
    }

    public void setSubsets(List<SubsetDto> subsets) {
        this.subsets = subsets;
    }

    public ServiceTrafficPolicyDto getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(ServiceTrafficPolicyDto trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

