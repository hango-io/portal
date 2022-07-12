package org.hango.cloud.dashboard.apiserver.dto;


import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


public class MicroServiceDto {

    public static final String NAME_PATTERN = "[a-zA-Z0-9]{1,256}";


    @JsonProperty("Id")
    private Long id;

    @JSONField(name = "ProjectId")
    @NotNull
    private Long projectId;

    @NotNull
    @JSONField(name = "Name")
    @Pattern(regexp = NAME_PATTERN)
    private String name;

    @JSONField(name = "CreateTime")
    private long createTime;

    @JSONField(name = "Owner")
    private String owner;

    @JSONField(name = "Desc")
    private String desc;

    @JSONField(name = "IsOffline")
    private Integer isOffline;

    @JSONField(name = "OwnerEmail")
    private String ownerEmail;

    @JSONField(name = "OwnerPhone")
    private String ownerPhone;

    @JSONField(name = "EnvironmentName")
    private String environment;

    @JSONField(name = "EnvId")
    private String envId;

    @JSONField(name = "AccessType")
    private String accessType;

    @JSONField(name = "ServiceType")
    private String serviceType;

    @JSONField(name = "GroupName")
    private String groupName;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public Integer getIsOffline() {
        return isOffline;
    }

    public void setIsOffline(Integer isOffline) {
        this.isOffline = isOffline;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "MicroServiceDto{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", name='" + name + '\'' +
                ", createTime=" + createTime +
                ", owner='" + owner + '\'' +
                ", desc='" + desc + '\'' +
                ", isOffline=" + isOffline +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", ownerPhone='" + ownerPhone + '\'' +
                ", environment='" + environment + '\'' +
                ", envId='" + envId + '\'' +
                ", accessType='" + accessType + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
