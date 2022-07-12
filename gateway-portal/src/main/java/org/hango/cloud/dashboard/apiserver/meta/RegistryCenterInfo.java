package org.hango.cloud.dashboard.apiserver.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * apigw_gportal_registry_center
 *
 * @author
 */
public class RegistryCenterInfo implements Serializable {

    private static final long serialVersionUID = -9135751753351114743L;

    /**
     * 注册中心配置主键
     */
    private Long id;

    /**
     * 注册中心类型
     */
    private String registryType;

    /**
     * 注册中心地址
     */
    private String registryAddr;

    /**
     * 注册中心别名
     */
    private String registryAlias;

    /**
     * 创建时间
     */
    private Long createDate;

    /**
     * 修改时间
     */
    private Long modifyDate;

    /**
     * 项目ID
     */
    private long projectId;

    /**
     * 是否项目共享
     */
    private int isShared;

    /**
     * 对应网关注册中心，网关id
     */
    private long gwId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getRegistryAddr() {
        return registryAddr;
    }

    public void setRegistryAddr(String registryAddr) {
        this.registryAddr = registryAddr;
    }

    public String getRegistryAlias() {
        return registryAlias;
    }

    public void setRegistryAlias(String registryAlias) {
        this.registryAlias = registryAlias;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }

    public Long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public int getIsShared() {
        return isShared;
    }

    public void setIsShared(int isShared) {
        this.isShared = isShared;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(final long gwId) {
        this.gwId = gwId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}