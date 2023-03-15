package org.hango.cloud.common.infra.gateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 网关信息表
 * @date 2022/10/25
 */
public class GatewayDto extends CommonExtensionDto implements Serializable {

    /**
     * 网关ID
     */
    @JSONField(name = "GwId")
    private long id;


    /**
     * 网关名称
     */
    @NotBlank(message = "网关名称不能为空")
    @Pattern(regexp = BaseConst.REGEX_GATEWAY_NAME, message = "网关名称格式错误")
    @JSONField(name = "Name")
    private String name;


    /**
     * 所属环境
     */
    @NotBlank(message = "网关环境ID不能为空")
    @JSONField(name = "EnvId")
    private String envId;


    /**
     * 网关service类型， ClusterIP/NodePort
     */
    @NotBlank(message = "网关service类型不能为空")
    @JSONField(name = "SvcType")
    private String svcType;


    /**
     * 网关service名称
     */
    @JSONField(name = "SvcName")
    private String svcName;


    /**
     * 网关类型
     */
    @NotBlank(message = "网关类型不能为空")
    @JSONField(name = "Type")
    private String type;


    /**
     * 网关集群名称
     */
    @NotBlank(message = "网关集群名称不能为空")
    @Pattern(regexp = BaseConst.REGEX_GATEWAY_CODE, message = "网关集群名称格式错误")
    @JSONField(name = "GwClusterName")
    private String gwClusterName;


    /**
     * 配置下发地址
     */
    @NotBlank(message = "网关配置下发地址不能为空")
    @JSONField(name = "ConfAddr")
    private String confAddr;


    /**
     * 备注
     */
    @Pattern(regexp = BaseConst.REGEX_DESCRIPTION, message = "网关描述信息格式错误")
    @JSONField(name = "Description")
    private String description;


    /**
     * 创建时间
     */

    @JSONField(name = "CreateTime")
    private long createTime;


    /**
     * 修改时间
     */

    @JSONField(name = "ModifyTime")
    private long modifyTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getSvcType() {
        return svcType;
    }

    public void setSvcType(String svcType) {
        this.svcType = svcType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGwClusterName() {
        return gwClusterName;
    }

    public void setGwClusterName(String gwClusterName) {
        this.gwClusterName = gwClusterName;
    }

    public String getConfAddr() {
        return confAddr;
    }

    public void setConfAddr(String confAddr) {
        this.confAddr = confAddr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getSvcName() {
        return svcName;
    }

    public void setSvcName(String svcName) {
        this.svcName = svcName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}