package org.hango.cloud.common.infra.routeproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.common.RouteRuleMatchDto;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.HttpRetryDto;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * 路由规则发布Dto
 *
 * @author hzchenzhongyang 2019-09-19
 */
public class RouteRuleProxyDto extends RouteRuleMatchDto {
    @JSONField(name = "Id")
    private Long id;

    /**
     * 发布指定的路由规则id
     */
    @JSONField(name = "RouteRuleId")
    @Min(1)
    private long routeRuleId;

    /**
     * 路由规则名称，用于前端展示
     */
    @JSONField(name = "RouteRuleName")
    private String routeRuleName;

    /**
     * 路由规则发布指定的网关id
     */
    @NotNull
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;

    /**
     * 路由规则发布指定的网关类型
     */
    @JSONField(name = "GwType")
    private String gwType;


    /**
     * 网关名称，用于前端展示，不进行存储
     */
    @JSONField(name = "GwName")
    private String gwName;

    /**
     * 网关地址，用于前端展示，不进行存储
     */
    @JSONField(name = "GwAddr")
    private String gwAddr;

    /**
     * 路由所属服务id
     */
    @JSONField(name = "ServiceId")
    private long serviceId;

    /**
     * 路由对应服务名称，用于前端展示
     */
    @JSONField(name = "ServiceName")
    private String serviceName;

    /**
     * 目标服务，用于前端展示，服务类型，不进行存储
     */
    @JSONField(name = "ServiceType")
    private String serviceType;

    /**
     * 路由规则发布时间
     */
    @JSONField(name = "CreateTime")
    private long createTime;

    /**
     * 路由规则发布信息更新时间
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;

    /**
     * 路由规则指定后端服务，支持选择多个服务并且填写权重,
     * 权重后端控制，权重为100
     * 后续作为version扩展
     */
    @JSONField(name = "DestinationServices")
    private List<DestinationDto> destinationServices;

    /**
     * 使能状态
     */
    @JSONField(name = "EnableState")
    @Pattern(regexp = "enable|disable")
    private String enableState = BaseConst.ROUTE_RULE_ENABLE_STATE;

    @JSONField(name = "Hosts")
    private List<String> hosts;

    /**
     * 路由超时时间
     */
    @JSONField(name = "Timeout")
    @Min(0)
    private long timeout = 60000;

    /**
     * route retry policy
     */
    @JSONField(name = "HttpRetry")
    @Valid
    private HttpRetryDto httpRetryDto;


    /**
     * 流量镜像规则
     */
    @JSONField(name = "MirrorTraffic")
    private DestinationDto mirrorTraffic;

    /**
     * 流量镜像开关，0关闭，1打开
     */
    @Range(min = 0, max = 1)
    @JSONField(name = "MirrorSwitch")
    private int mirrorSwitch;

    /**
     * 路由规则所属项目id
     */
    @JSONField(name = "ProjectId")
    private long projectId;

    @JSONField(name = "MetaMap")
    private Map<String, String> metaMap;

    public int getMirrorSwitch() {
        return mirrorSwitch;
    }

    public void setMirrorSwitch(int mirrorSwitch) {
        this.mirrorSwitch = mirrorSwitch;
    }

    public DestinationDto getMirrorTraffic() {
        return mirrorTraffic;
    }

    public void setMirrorTraffic(DestinationDto mirrorTraffic) {
        this.mirrorTraffic = mirrorTraffic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getRouteRuleId() {
        return routeRuleId;
    }

    public void setRouteRuleId(long routeRuleId) {
        this.routeRuleId = routeRuleId;
    }


    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public List<DestinationDto> getDestinationServices() {
        return destinationServices;
    }

    public void setDestinationServices(List<DestinationDto> destinationServices) {
        this.destinationServices = destinationServices;
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

    public String getRouteRuleName() {
        return routeRuleName;
    }

    public void setRouteRuleName(String routeRuleName) {
        this.routeRuleName = routeRuleName;
    }

    public String getEnableState() {
        return enableState;
    }

    public void setEnableState(String enableState) {
        this.enableState = enableState;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public HttpRetryDto getHttpRetryDto() {
        return httpRetryDto;
    }

    public void setHttpRetryDto(HttpRetryDto httpRetryDto) {
        this.httpRetryDto = httpRetryDto;
    }

    public Long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(Long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public Map<String, String> getMetaMap() {
        return metaMap;
    }

    public void setMetaMap(Map<String, String> metaMap) {
        this.metaMap = metaMap;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
