package org.hango.cloud.dashboard.apiserver.dto.gatewaydto;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyVirtualHostDto;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 与前端交互的网关dto
 */
public class GatewayDto implements Serializable {

    private static final long serialVersionUID = -289652590295163660L;
    /**
     * 网关真实配置信息
     */
    @JSONField(name = "GatewayConfigInfo")
    GatewayAddrConfigInfo gatewayAddrConfigInfo;
    @JSONField(name = "GwId")
    private long id;
    /**
     * 网关类型，envoy/Spring Cloud Gateway
     */
    @JSONField(name = "GwType")
    @NotEmpty(message = "网关类型不能为空")
    @Pattern(regexp = Const.GATEWAY_TYPE_PATTERN, message = "网关类型填写错误")
    private String gwType;
    /**
     * 网关名称
     */
    @JSONField(name = "GwName")
    @NotEmpty
    @Pattern(regexp = Const.REGEX_GATEWAY_NAME)
    private String gwName;
    /**
     * 网关地址
     */
    @JSONField(name = "GwAddr")
    @NotEmpty
    @Pattern(regexp = Const.REGEX_GATEWAY_URL)
    private String gwAddr;
    /**
     * Envoy网关对接的api-plane的地址
     */
    @JSONField(name = "ApiPlaneAddr")
    @Pattern(regexp = "^(http://|https://)\\S{5,254}|", message = "网关对接api-plane地址不合法")
    private String apiPlaneAddr;
    /**
     * Envoy网关部署时gw_cluster标签的值，用于区分envoy示例所属网关集群
     */
    @JSONField(name = "GwClusterName")
    @Pattern(regexp = "^[\\s\\S]{1,127}|", message = "参数网关集群名称不能为空且长度不能超过128字符")
    private String gwClusterName;
    /**
     * 网关描述
     */
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;
    /**
     * 健康检查地址
     */
    @JSONField(name = "HealthInterfacePath")
    @Pattern(regexp = Const.REGEX_HEALTH)
    private String healthInterfacePath;
    /**
     * 网关所属项目id
     */
    @JSONField(name = "ProjectIds")
    private List<Long> projectIdList;
    /**
     * 网关对应的环境id
     */
    @JSONField(name = "EnvId")
    private String envId;

    /**
     * 网关创建时间
     */
    @JSONField(name = "CreateDate")
    private long createDate;

    /**
     * 网关更新时间
     */
    @JSONField(name = "ModifiedDate")
    private long modifyDate;

    /**
     * 网关健康检查状态
     */
    @JSONField(name = "Status")
    private int status;

    /**
     * 网关上次检查时间
     */
    @JSONField(name = "LastCheckTime")
    private long lastCheckTime;

    /**
     * 网关中virtual host列表
     */
    @JSONField(name = "VirtualHostList")
    private List<EnvoyVirtualHostDto> virtualHostList;

    /**
     * camel实例地址
     */
    @JSONField(name = "CamelAddr")
    private String camelAddr;

    public static GatewayDto fromMeta(GatewayInfo gatewayInfo) {
        GatewayDto dto = BeanUtil.copy(gatewayInfo, GatewayDto.class);
        if (!CollectionUtils.isEmpty(gatewayInfo.getVirtualHostList())) {
            dto.setVirtualHostList(gatewayInfo.getVirtualHostList().stream().map(EnvoyVirtualHostDto::fromMeta).collect(Collectors.toList()));
        }
        List<Long> projectIdList = Lists.newArrayList();
        if (StringUtils.isNotBlank(gatewayInfo.getProjectId())) {
            projectIdList = gatewayInfo.getProjectIdList();
        }
        dto.setProjectIdList(projectIdList);
        return dto;
    }

    public static GatewayInfo toMeta(GatewayDto dto) {
        GatewayInfo meta = new GatewayInfo();
        meta.setId(dto.getId());
        meta.setGwName(dto.getGwName());
        meta.setGwAddr(dto.getGwAddr());
        meta.setDescription(dto.getDescription());
        meta.setGwType(dto.getGwType());
        meta.setCamelAddr(dto.getCamelAddr());
        if (dto.getGatewayAddrConfigInfo() != null) {
            meta.setAuditDatasourceSwitch(StringUtils.trim(dto.getGatewayAddrConfigInfo().getAuditDatasourceSwitch()));
            meta.setAuditDbConfig(StringUtils.trim(dto.getGatewayAddrConfigInfo().getAuditDbConfig()));
            meta.setAuthAddr(StringUtils.trim(dto.getGatewayAddrConfigInfo().getAuthAddr()));
            meta.setEnvId(StringUtils.trim(dto.getGatewayAddrConfigInfo().getEnvId()));
            meta.setGwUniId(StringUtils.trim(dto.getGatewayAddrConfigInfo().getGwUniId()));
            meta.setPromAddr(StringUtils.trim(dto.getGatewayAddrConfigInfo().getMetricUrl()));
            meta.setMetricUrl(StringUtils.trim(dto.getGatewayAddrConfigInfo().getMetricUrl()));
        }
        List<Long> projectIds = dto.getProjectIdList() == null ? Lists.newArrayList() : dto.getProjectIdList();
        if (Const.ENVOY_GATEWAY_TYPE.equals(dto.getGwType())) {
            meta.setApiPlaneAddr(dto.getApiPlaneAddr());
            meta.setGwClusterName(dto.getGwClusterName());
            //默认envoy网关为正常状态
            meta.setStatus(NumberUtils.INTEGER_ONE);
            if (!CollectionUtils.isEmpty(dto.getVirtualHostList())) {
                projectIds.addAll(dto.getVirtualHostList().stream().map(EnvoyVirtualHostDto::getProjectId).collect(Collectors.toList()));
            }
        }
        meta.setProjectId(projectIds.stream().map(Object::toString).collect(Collectors.joining(",")));
        return meta;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHealthInterfacePath() {
        return healthInterfacePath;
    }

    public void setHealthInterfacePath(String healthInterfacePath) {
        this.healthInterfacePath = healthInterfacePath;
    }

    public List<Long> getProjectIdList() {
        return projectIdList;
    }

    public void setProjectIdList(List<Long> projectIdList) {
        this.projectIdList = projectIdList;
    }

    public GatewayAddrConfigInfo getGatewayAddrConfigInfo() {
        return gatewayAddrConfigInfo;
    }

    public void setGatewayAddrConfigInfo(GatewayAddrConfigInfo gatewayAddrConfigInfo) {
        this.gatewayAddrConfigInfo = gatewayAddrConfigInfo;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
    }

    public String getApiPlaneAddr() {
        return apiPlaneAddr;
    }

    public void setApiPlaneAddr(String apiPlaneAddr) {
        this.apiPlaneAddr = apiPlaneAddr;
    }

    public String getGwClusterName() {
        return gwClusterName;
    }

    public void setGwClusterName(String gwClusterName) {
        this.gwClusterName = gwClusterName;
    }

    public List<EnvoyVirtualHostDto> getVirtualHostList() {
        return virtualHostList;
    }

    public void setVirtualHostList(List<EnvoyVirtualHostDto> virtualHostList) {
        this.virtualHostList = virtualHostList;
    }

    public String getCamelAddr() {
        return camelAddr;
    }

    public void setCamelAddr(String camelAddr) {
        this.camelAddr = camelAddr;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
