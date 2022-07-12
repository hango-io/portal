package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.util.Const;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 路由规则发布Dto
 *
 * @author hzchenzhongyang 2019-09-19
 */
public class RouteRuleProxyDto extends RouteRuleMatchDto {
    @JSONField(name = "Id")
    private long id;

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
    @JSONField(name = "GwId")
    private long gwId;

    /**
     * 路由规则发布指定的网关类型
     */
    @JSONField(name = "GwType")
    private String gwType;

    /**
     * 发布路由多网关id，兼容之前逻辑
     */
    @JSONField(name = "GwIds")
    private List<Long> gwIds;

    /**
     * 网关名称，用于前端展示，不进行存储
     */
    @JSONField(name = "GwName")
    private String gwName;

    /**
     * 网关envId, 用于前端展示
     */
    @JSONField(name = "EnvId")
    private String envId;

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
    private List<EnvoyDestinationDto> destinationServices;

    /**
     * 使能状态
     */
    @JSONField(name = "EnableState")
    @Pattern(regexp = "enable|disable")
    private String enableState = Const.ROUTE_RULE_ENABLE_STATE;

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
     * https://istio.io/docs/reference/config/networking/virtual-service/#HTTPRetry
     */
    @JSONField(name = "HttpRetry")
    @Valid
    private HttpRetryDto httpRetryDto;

    /**
     * 路由来源
     */
    @JSONField(name = "RouteRuleSource")
    @Pattern(regexp = "Gateway|NSF")
    private String routeRuleSource = "Gateway";

    /**
     * 路由头操作 Request/Response Add & Remove
     */
    @JSONField(name = "HeaderOperation")
    @Valid
    private EnvoyRouteRuleHeaderOperationDto headerOperation;

    /**
     * VirtualCluster，路由指标信息，开启路由指标
     */
    @JSONField(name = "VirtualCluster")
    private VirtualClusterDto virtualClusterDto;

    @JSONField(name = "NeedRouteMetric")
    private Boolean needRouteMetric = false;

    /**
     * 流量镜像规则
     */
    @JSONField(name = "MirrorTraffic")
    private EnvoyDestinationDto mirrorTraffic;

    /**
     * 流量镜像开关，0关闭，1打开
     */
    @JSONField(name = "MirrorSwitch")
    private int mirrorSwitch;

    public int getMirrorSwitch() {
        return mirrorSwitch;
    }

    public void setMirrorSwitch(int mirrorSwitch) {
        this.mirrorSwitch = mirrorSwitch;
    }

    public EnvoyDestinationDto getMirrorTraffic() {
        return mirrorTraffic;
    }

    public void setMirrorTraffic(EnvoyDestinationDto mirrorTraffic) {
        this.mirrorTraffic = mirrorTraffic;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRouteRuleId() {
        return routeRuleId;
    }

    public void setRouteRuleId(long routeRuleId) {
        this.routeRuleId = routeRuleId;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
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

    public List<EnvoyDestinationDto> getDestinationServices() {
        return destinationServices;
    }

    public void setDestinationServices(List<EnvoyDestinationDto> destinationServices) {
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

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getRouteRuleSource() {
        return routeRuleSource;
    }

    public void setRouteRuleSource(String routeRuleSource) {
        this.routeRuleSource = routeRuleSource;
    }

    public EnvoyRouteRuleHeaderOperationDto getHeaderOperation() {
        return headerOperation;
    }

    public void setHeaderOperation(EnvoyRouteRuleHeaderOperationDto headerOperation) {
        this.headerOperation = headerOperation;
    }

    public List<Long> getGwIds() {
        return gwIds;
    }

    public void setGwIds(List<Long> gwIds) {
        this.gwIds = gwIds;
    }

    public VirtualClusterDto getVirtualClusterDto() {
        return virtualClusterDto;
    }

    public void setVirtualClusterDto(VirtualClusterDto virtualClusterDto) {
        this.virtualClusterDto = virtualClusterDto;
    }

    public Boolean getNeedRouteMetric() {
        return needRouteMetric;
    }

    public void setNeedRouteMetric(Boolean needRouteMetric) {
        this.needRouteMetric = needRouteMetric;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
