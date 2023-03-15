package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.healthcheck.dto.HealthCheckRuleDto;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * 元数据发布信息相关dto
 *
 * @author hanjiahao
 */
public class ServiceProxyDto extends CommonExtensionDto implements Serializable {
    private static final long serialVersionUID = -2075260227746685133L;
    /**
     * 数据库主键自增id
     */
    @JSONField(name = "Id")
    private long id;

    @JSONField(name = "ServiceId")
    @Min(value = 1)
    private long serviceId;

    /**
     * 服务名称，用于前端展示，不进行存储
     */
    @JSONField(name = "ServiceName")
    private String serviceName;

    /**
     * 服务标识，用于前端展示及告警获取，不进行存储
     */
    @JSONField(name = "ServiceTag")
    private String serviceTag;

    /**
     * 服务类型，用于前端显示
     */
    @JSONField(name = "ServiceType")
    private String serviceType;

    /**
     * 服务唯一标识
     */
    @JSONField(name = "Code")
    private String code;

    /**
     * 服务发布所选服务名称（网关真实名称）
     * 静态发布，则为后端服务host；注册中心发布k8s service
     */
    @JSONField(name = "BackendService")
    @NotBlank
    private String backendService;

    /**
     * 注册中心类型Consul/Kubernetes/Eureka/Zookeeper，DYNAMIC时必填，默认Kubernetes
     */
    @JSONField(name = "RegistryCenterType")
    private String registryCenterType;


    /**
     * 路由发布协议，可以为空，默认为
     */
    @JSONField(name = "PublishProtocol")
    @Pattern(regexp = "http|https|grpc")
    private String publishProtocol = "http";

    /**
     * 服务发布策略，DYNAMIC,STATIC
     */
    @JSONField(name = "PublishType")
    @NotBlank
    private String publishType;

    /**
     * 服务发布指定的网关网关id
     */
    @JSONField(name = "VirtualGwId")
    @Min(1)
    private long virtualGwId;

    /**
     * 服务发布指定的网关类型
     */
    @JSONField(name = "GwType")
    private String gwType;

    /**
     * 网关名称，用于前端展示，不进行存储
     */
    @JSONField(name = "VirtualGwName")
    private String virtualGwName;

    @JSONField(name = "VirtualGwCode")
    private String virtualGwCode;

    /**
     * 网关集群名称
     */
    @JSONField(name = "GwClusterName")
    private String gwClusterName;

    @JSONField(name = "GwAddr")
    private String gwAddr;

    @JSONField(name = "EnvId")
    private String envId;

    /**
     * 发布时间
     */
    @JSONField(name = "CreateTime")
    private long createTime;

    /**
     * 更新时间
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;

    @JSONField(name = "LoadBalancer")
    @Pattern(regexp = "|ROUND_ROBIN|LEAST_CONN|RANDOM")
    private String loadBalancer = "ROUND_ROBIN";
    /**
     * 服务健康状态：0表示异常；1表示健康；2表示部分健康
     */
    @JSONField(name = "HealthyStatus")
    private Integer healthyStatus;

    /**
     * 服务发布后，后端服务对应的port信息
     * 只有动态发布，才有port信息，静态发布不存在port信息
     */
    @JSONField(name = "Port")
    private List<Integer> port;

    /**
     * 版本集合
     */
    @JSONField(name = "Subsets")
    private List<SubsetDto> subsets;

    /**
     * 高级配置包含负载均衡策略和连接池
     */
    @JSONField(name = "TrafficPolicy")
    private ServiceTrafficPolicyDto trafficPolicy;

    /**
     * 主动健康检查
     */
    @JSONField(name = "HealthCheck")
    private HealthCheckRuleDto healthCheckRule;

    /**
     * 项目id
     */
    @JSONField(name = "ProjectId")
    private long projectId;


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

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVirtualGwName() {
        return virtualGwName;
    }

    public void setVirtualGwName(String virtualGwName) {
        this.virtualGwName = virtualGwName;
    }

    public String getVirtualGwCode() {
        return virtualGwCode;
    }

    public void setVirtualGwCode(String virtualGwCode) {
        this.virtualGwCode = virtualGwCode;
    }

    public String getGwClusterName() {
        return gwClusterName;
    }

    public void setGwClusterName(String gwClusterName) {
        this.gwClusterName = gwClusterName;
    }

    public String getGwAddr() {
        return gwAddr;
    }

    public void setGwAddr(String gwAddr) {
        this.gwAddr = gwAddr;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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

    public Integer getHealthyStatus() {
        return healthyStatus;
    }

    public void setHealthyStatus(Integer healthyStatus) {
        this.healthyStatus = healthyStatus;
    }

    public List<Integer> getPort() {
        return port;
    }

    public void setPort(List<Integer> port) {
        this.port = port;
    }

    public List<SubsetDto> getSubsets() {
        return subsets;
    }

    public void setSubsets(List<SubsetDto> subsets) {
        this.subsets = subsets;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public String getRegistryCenterType() {
        return registryCenterType;
    }

    public void setRegistryCenterType(String registryCenterType) {
        this.registryCenterType = registryCenterType;
    }

    public ServiceTrafficPolicyDto getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(ServiceTrafficPolicyDto trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public HealthCheckRuleDto getHealthCheckRule() {
        return healthCheckRule;
    }

    public void setHealthCheckRule(HealthCheckRuleDto healthCheckRule) {
        this.healthCheckRule = healthCheckRule;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
