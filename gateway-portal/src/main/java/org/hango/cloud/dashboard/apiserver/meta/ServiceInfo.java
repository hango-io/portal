package org.hango.cloud.dashboard.apiserver.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 服务 信息
 *
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2017/12/26 15:57.
 */
public class ServiceInfo implements Serializable {

    /**
     * 服务发布状态，已发布
     **/
    public static final int STATUS_PUBLISHED = 1;
    /**
     * 服务发布状态，未发布
     **/
    public static final int STATUS_UNPUBLISHED = 0;
    private static final long serialVersionUID = 774007362752420257L;
    private long id;
    private long createDate;
    private long modifyDate;
    //服务名称
    private String displayName;
    //服务标识
    private String serviceName;
    private String contacts;
    private String description;
    private int status;
    private long publishedCount = 0; //表示已发布到的网关环境 数量

    private String healthInterfacePath;
    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * webservice服务对应的wsdl地址
     */
    private String wsdlUrl;

    /**
     * 服务所属的项目id
     */
    private long projectId;

    /**
     * 同步状态 0-本地数据 1-同步 2-失步
     */
    private int syncStatus;

    /**
     * 外部serviceId
     */
    private long extServiceId;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getPublishedCount() {
        return publishedCount;
    }

    public void setPublishedCount(long publishedCount) {
        this.publishedCount = publishedCount;
    }

    public String getHealthInterfacePath() {
        return healthInterfacePath;
    }

    public void setHealthInterfacePath(String healthInterfacePath) {
        this.healthInterfacePath = healthInterfacePath;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public long getExtServiceId() {
        return extServiceId;
    }

    public void setExtServiceId(long extServiceId) {
        this.extServiceId = extServiceId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}
