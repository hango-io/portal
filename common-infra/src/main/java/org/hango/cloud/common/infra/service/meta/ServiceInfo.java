package org.hango.cloud.common.infra.service.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 服务元信息
 * @date 2022/09/05
 */
public class ServiceInfo extends CommonExtension implements Serializable {

    private static final long serialVersionUID = -7589565563805122798L;


    /**
     * 主键
     */
    private long id;


    /**
     * 创建时间
     */
    private long createDate;


    /**
     * 修改时间
     */
    private long modifyDate;


    /**
     * 服务显示名称
     */
    private String displayName;


    /**
     * 服务标识
     */
    private String serviceName;


    /**
     * 负责人
     */
    private String contacts;


    /**
     * 描述
     */
    private String description;


    /**
     * 状态，0表示未发布，1表示已发布
     */
    private int status;


    /**
     * 服务类型
     */
    private String serviceType;


    /**
     * 基于项目隔离，项目id
     */
    private long projectId;


    /**
     * 服务协议相关扩展信息，如wsdl的地址
     */
    private String extensionInfo;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getExtensionInfo() {
        return extensionInfo;
    }

    public void setExtensionInfo(String extensionInfo) {
        this.extensionInfo = extensionInfo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}