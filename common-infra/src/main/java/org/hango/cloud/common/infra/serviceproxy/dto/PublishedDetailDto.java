package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author hanjiahao
 * 发布详情dto
 */
public class PublishedDetailDto {


    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

    /**
     * 网关名称
     */
    @JSONField(name = "GwName")
    private String gwName;

    /**
     * 网关类型,前端根据网关类型，分别进行删除
     */
    @JSONField(name = "GwType")
    private String gwType;

    /**
     * 注册中心类型Consul/Kubernetes/Eureka/Zookeeper，DYNAMIC时必填，默认Kubernetes
     */
    @JSONField(name = "RegistryCenterType")
    private String registryCenterType;

    /**
     * 注册中心地址
     */
    @JSONField(name = "RegistryCenterAddr")
    private String registryCenterAddr;

    /**
     * 服务地址，如果是注册中心，则为实例地址
     */
    @JSONField(name = "ServiceAddr")
    private String[] serviceAddr;

    /**
     * 服务健康状态：0表示异常；1表示健康；2表示部分健康
     */
    @JSONField(name = "HealthyStatus")
    private Integer healthyStatus;

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }

    public String[] getServiceAddr() {
        return serviceAddr;
    }

    public void setServiceAddr(String[] serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
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

    public Integer getHealthyStatus() {
        return healthyStatus;
    }

    public void setHealthyStatus(Integer healthyStatus) {
        this.healthyStatus = healthyStatus;
    }
}
