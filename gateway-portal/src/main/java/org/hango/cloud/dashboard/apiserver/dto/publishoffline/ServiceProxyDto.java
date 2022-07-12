package org.hango.cloud.dashboard.apiserver.dto.publishoffline;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2021/11/15
 */
public class ServiceProxyDto {

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
     * envoy网关id
     */
    @JSONField(name = "GwId")
    @Min(1)
    private long gwId;

    /**
     * 网关名称，用于前端展示，不进行存储
     */
    @JSONField(name = "GwName")
    private String gwName;

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

    /**
     * 服务健康状态：0表示异常；1表示健康；2表示部分健康
     */
    @JSONField(name = "HealthyStatus")
    private Integer healthyStatus;


    /**
     * 发布所属项目id
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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

    public String getRegistryCenterType() {
        return registryCenterType;
    }

    public void setRegistryCenterType(String registryCenterType) {
        this.registryCenterType = registryCenterType;
    }

    public String getPublishProtocol() {
        return publishProtocol;
    }

    public void setPublishProtocol(String publishProtocol) {
        this.publishProtocol = publishProtocol;
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

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }

    public String getGwAddr() {
        return gwAddr;
    }

    public void setGwAddr(String gwAddr) {
        this.gwAddr = gwAddr;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
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

    public Integer getHealthyStatus() {
        return healthyStatus;
    }

    public void setHealthyStatus(Integer healthyStatus) {
        this.healthyStatus = healthyStatus;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}
