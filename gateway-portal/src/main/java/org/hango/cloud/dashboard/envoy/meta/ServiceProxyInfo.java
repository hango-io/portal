package org.hango.cloud.dashboard.envoy.meta;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;

public class ServiceProxyInfo {
    /**
     * 数据库主键自增id
     */
    private long id;

    /**
     * 服务元数据id
     */
    private long serviceId;

    /**
     * 服务元数据唯一标识
     */
    private String code;

    /**
     * 服务发布所选服务名称（网关真实名称）
     */
    private String backendService;

    /**
     * 服务发布protocol
     * 支持http, https
     */
    private String publishProtocol;

    /**
     * 服务发布策略，dynamic, static
     */
    private String publishType;

    /**
     * 注册中心类型Consul/Kubernetes/Eureka/Zookeeper，DYNAMIC时必填，默认Kubernetes
     */
    private String registryCenterType;

    /**
     * 注册中心地址
     */
    private String registryCenterAddr;

    /**
     * 服务发布指定的网关id
     */
    private long gwId;

    /**
     * 服务发布指定的网关类型
     */
    private String gwType;

    /**
     * 发布时间
     */
    private long createTime;

    /**
     * 更新时间
     */
    private long updateTime;

    /**
     * 项目id
     */
    private long projectId;

    /**
     * 负载均衡策略 （已废弃）
     */
    private String loadBalancer;

    /**
     * 负载均衡策略 & 连接池配置
     */
    private String trafficPolicy;

    /**
     * 版本集合
     */
    private String subsets;

    public static ServiceProxyInfo fromDto(ServiceProxyDto serviceProxyDto) {
        ServiceProxyInfo serviceProxyInfo = BeanUtil.copy(serviceProxyDto, ServiceProxyInfo.class);
        if (serviceProxyDto.getSubsets() != null) {
            serviceProxyInfo.setSubsets(JSON.toJSONString(serviceProxyDto.getSubsets()));
        }
        serviceProxyInfo.setCode(serviceProxyDto.getPublishType() + "-" + serviceProxyDto.getServiceId());
        return serviceProxyInfo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBackendService() {
        return backendService;
    }

    public void setBackendService(String backendService) {
        this.backendService = backendService;
    }

    public String getPublishType() {
        return publishType;
    }

    public void setPublishType(String publishType) {
        this.publishType = publishType;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getPublishProtocol() {
        return publishProtocol;
    }

    public void setPublishProtocol(String publishProtocol) {
        this.publishProtocol = publishProtocol;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public String getSubsets() {
        return subsets;
    }

    public void setSubsets(String subsets) {
        this.subsets = subsets;
    }

    public String getRegistryCenterType() {
        return registryCenterType;
    }

    public void setRegistryCenterType(String registryCenterType) {
        this.registryCenterType = registryCenterType;
    }

    public String getRegistryCenterAddr() {
        return registryCenterAddr;
    }

    public void setRegistryCenterAddr(String registryCenterAddr) {
        this.registryCenterAddr = registryCenterAddr;
    }

    public String getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(String trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
