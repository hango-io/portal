package org.hango.cloud.dashboard.apiserver.dto.servicedto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 服务基本信息Dto，仅存储基本信息
 *
 * @author hanjiahao
 */
public class ServiceInfoDto {

    /**
     * 服务id
     */
    @JSONField(name = "ServiceId")
    private long id;

    /**
     * 服务名称,对应数据库存储为display_name
     */
    @JSONField(name = "ServiceName")
    @NotNull
    @Pattern(regexp = Const.REGEX_SERVICE_NAME)
    private String displayName;

    /**
     * 服务标识,对应数据库存储为service_name
     */
    @JSONField(name = "ServiceTag")
    @NotNull
    @Pattern(regexp = Const.REGEX_SERVICE_TAG)
    private String serviceName;

    /**
     * 服务联系人
     */
    @JSONField(name = "Contacts")
    private String contacts;

    /**
     * 服务类型--http,dubbo
     */
    @JSONField(name = "ServiceType")
    @NotNull
    @Pattern(regexp = Const.REGEX_SERVICE_TYPE)
    private String serviceType;

    /**
     * 服务描述
     */
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    /**
     * webservice url
     */
    @JSONField(name = "WsdlUrl")
    private String wsdlUrl;

    /**
     * 1.0服务支持健康健康
     */
    @JSONField(name = "HealthInterfacePath")
    @Pattern(regexp = Const.REGEX_HEALTH_INTERFACE)
    private String healthInterfacePath;

    /**
     * 发布状态,用于控制服务标识是否修改
     * 0未发布，1已发布
     */
    @JSONField(name = "PublishedStatus")
    private int status;


    @JSONField(name = "ApplicationName")
    private String applicationName;

    /**
     * 创建时间
     */
    @JSONField(name = "CreateDate")
    private long createDate;

    /**
     * 更新时间
     */
    @JSONField(name = "ModifyDate")
    private long modifyDate;

    /**
     * 同步状态
     */
    @JSONField(name = "SyncStatus")
    private int syncStatus;

    public static ServiceInfoDto fromMeta(ServiceInfo serviceInfo) {
        return BeanUtil.copy(serviceInfo, ServiceInfoDto.class);
    }

    public static ServiceInfo toMeta(ServiceInfoDto dto) {
        return BeanUtil.copy(dto, ServiceInfo.class);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getHealthInterfacePath() {
        return healthInterfacePath;
    }

    public void setHealthInterfacePath(String healthInterfacePath) {
        this.healthInterfacePath = healthInterfacePath;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
