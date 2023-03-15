package org.hango.cloud.common.infra.service.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.gdashboard.api.util.Const;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 服务基本信息Dto，仅存储基本信息
 * @date 2022/9/5
 */
public class ServiceDto extends CommonExtensionDto implements Serializable {

    private static final long serialVersionUID = -6854085017604520388L;
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
    @Pattern(regexp = BaseConst.REGEX_SERVICE_TYPE)
    private String serviceType;

    /**
     * 服务描述
     */
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    /**
     * 发布状态,用于控制服务标识是否修改
     * 0未发布，1已发布
     */
    @JSONField(name = "PublishedStatus")
    private int status;

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
     * 基于项目隔离，项目id
     */

    @JSONField(name = "ProjectId")
    private long projectId;


    /**
     * 服务协议相关扩展信息，如wsdl的地址
     */

    @JSONField(name = "ExtensionInfo")
    private String extensionInfo;


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
