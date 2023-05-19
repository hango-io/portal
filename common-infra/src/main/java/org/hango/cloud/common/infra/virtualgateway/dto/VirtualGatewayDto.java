package org.hango.cloud.common.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.RegexConst.*;

/**
 * 与前端交互的网关dto
 */
@JsonIgnoreProperties({"ConfAddr"})
public class VirtualGatewayDto extends CommonExtensionDto implements Serializable {

    private static final Long serialVersionUID = -289652590295163660L;

    /**
     * 虚拟网关ID
     */
    @JSONField(name = "VirtualGwId")
    private long id;


    /**
     * 网关ID
     */
    @NotNull
    @JSONField(name = "GwId")
    private long gwId;


    /**
     * 虚拟网关名称
     */
    @NotBlank
    @Pattern(regexp = REGEX_GATEWAY_NAME)
    @JSONField(name = "Name")
    private String name;

    /**
     * 网关名称
     */
    @JSONField(name = "GwName")
    private String gwName;


    /**
     * 虚拟网关标识
     */
    @NotBlank
    @Pattern(regexp = REGEX_GATEWAY_CODE)
    @JSONField(name = "Code")
    private String code;


    /**
     * 虚拟网关访问地址
     */
    @Pattern(regexp = REGEX_DESCRIPTION)
    @JSONField(name = "Addr")
    private String addr;


    /**
     * 网关所属项目id
     */
    @JSONField(name = "ProjectIds")
    private List<Long> projectIdList = new ArrayList<>();


    /**
     * 虚拟网关描述
     */

    @Pattern(regexp = REGEX_DESCRIPTION)
    @JSONField(name = "Description")
    private String description;


    /**
     * 所属环境
     */
    @JSONField(name = "EnvId")
    private String envId;


    /**
     * 虚拟网关类型
     */
    @NotBlank
    @JSONField(name = "Type")
    private String type;


    /**
     * 监听协议类型
     */
    @NotBlank
    @Pattern(regexp = BaseConst.PROTOCOL_SCHEME_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE)
    @JSONField(name = "Protocol")
    private String protocol;


    /**
     * 监听端口
     */
    @NotNull
    @JSONField(name = "Port")
    private int port;

    /**
     * 配置下发地址
     */
    @JSONField(name = "ConfAddr",serialize = false)
    private String confAddr;

    /**
     * 网关集群名称
     */
    @JSONField(name = "GwClusterName")
    private String gwClusterName;

    /**
     * 网关类型
     */
    @JSONField(name = "GwType")
    private String gwType;


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


    /**
     * 域名列表
     */
    @JSONField(name = "DomainInfos")
    private List<DomainInfoDTO> domainInfos;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public List<Long> getProjectIdList() {
        return projectIdList;
    }

    public void setProjectIdList(List<Long> projectIdList) {
        this.projectIdList = projectIdList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public String getConfAddr() {
        return confAddr;
    }

    public void setConfAddr(String confAddr) {
        this.confAddr = confAddr;
    }

    public String getGwClusterName() {
        return gwClusterName;
    }

    public void setGwClusterName(String gwClusterName) {
        this.gwClusterName = gwClusterName;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
    }

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public List<DomainInfoDTO> getDomainInfos() {
        return domainInfos;
    }

    public void setDomainInfos(List<DomainInfoDTO> domainInfos) {
        this.domainInfos = domainInfos;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}
