package org.hango.cloud.dashboard.envoy.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleHeaderOperationDto;
import org.hango.cloud.dashboard.envoy.web.dto.HttpRetryDto;

import java.util.List;
import java.util.Map;

/**
 * 路由规则发布信息
 *
 * @author hanjiahao
 */
public class RouteRuleProxyInfo extends RouteRuleMatchInfo {
    private long id;
    /**
     * 路由规则发布指定网关id
     */
    private long gwId;

    /**
     * 路由规则发布指定的网关类型
     */
    private String gwType;

    /**
     * 发布时指定的路由规则id
     */
    private long routeRuleId;

    /**
     * 路由规则发布时间
     */
    private long createTime;
    /**
     * 路由规则发布信息更新时间
     */
    private long updateTime;
    /**
     * 路由规则发布指定目标服务
     */
    private List<EnvoyDestinationInfo> destinationServiceList;
    private String destinationServices;

    /**
     * 路由规则所发布的服务id，用于已发布路由规则搜索
     */
    private long serviceId;

    /**
     * 路由规则发布所属项目id
     */
    private long projectId;

    /**
     * 使能状态
     */
    private String enableState;

    /**
     * virtual service中hosts列表
     */
    private String hosts;

    /**
     * HttpRetry 路由重试
     */
    private HttpRetryDto httpRetryDto;

    /**
     * 路由重试条件，数据库string存储
     */
    private String httpRetry;

    /**
     * 路由超时时间
     */
    private long timeout;

    /**
     * virtualCluster 路由监控信息
     */
    private String virtualCluster;

    private Boolean needRouteMetric;

    /**
     * 路由头操作 Request/Response Add & Remove
     */
    private EnvoyRouteRuleHeaderOperationDto headerOperation;

    /**
     * 流量镜像配置
     */
    private String mirrorTraffic;
    private EnvoyDestinationInfo mirrorTrafficValue;

    /**
     * 流量镜像指向的服务
     */
    private long mirrorServiceId;

    /**
     * meta数据传输集
     * Map<mata_type,meta_data>
     * mata_type meta类型
     *
     * mata_type: 路由meta数据类型
     * meta_data: 路由meta数据值，使用JSON传输
     * eg.
     * {
     * "DubboMeta": {
     * "ObjectType": "route",
     * "ObjectId": 259,
     * "Params": [
     * {
     * "Key": "str",
     * "Value": "java.lang.String",
     * "GenericInfo": ".dataA:com.demo.B,.dataAA:com.demo.B,.dataA.dataB:com.demo.C,.dataAA.dataB:com.demo.C",
     * "Required": false,
     * "DefaultValue": "sdfsdfs",
     * "_formTableKey": 1660649073793,
     * "index": 0
     * },
     * {
     * "Key": "wer",
     * "Value": "java.lang.Integer",
     * "GenericInfo": null,
     * "Required": true,
     * "DefaultValue": null,
     * "_formTableKey": 1660649073793
     * }
     * ],
     * "Method": "echoStrAndInt",
     * "CustomParamMapping": true,
     * "ParamSource": "body",
     * "Attachment": [
     * {
     * "ClientParamName": "xcvxcv",
     * "Description": "cxvxcv",
     * "ParamPosition": "Header",
     * "ServerParamName": "cvcv",
     * "distinctName": "Headerxcvxcv",
     * "_formTableKey": 1660648830195
     * }
     * ],
     * "MethodWorks": true
     * },
     * "StatsMeta": [
     * "/test",
     * "/test1"
     * ]
     * }
     */
    private Map<String, String> metaMap;

    public long getMirrorServiceId() {
        return mirrorServiceId;
    }

    public void setMirrorServiceId(long mirrorServiceId) {
        this.mirrorServiceId = mirrorServiceId;
    }

    public EnvoyDestinationInfo getMirrorTrafficValue() {
        return mirrorTrafficValue;
    }

    public void setMirrorTrafficValue(EnvoyDestinationInfo mirrorTrafficValue) {
        this.mirrorTrafficValue = mirrorTrafficValue;
    }

    public String getMirrorTraffic() {
        return mirrorTraffic;
    }

    public void setMirrorTraffic(String mirrorTraffic) {
        this.mirrorTraffic = mirrorTraffic;
    }

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

    public List<EnvoyDestinationInfo> getDestinationServiceList() {
        return destinationServiceList;
    }

    public void setDestinationServiceList(List<EnvoyDestinationInfo> destinationServiceList) {
        this.destinationServiceList = destinationServiceList;
    }

    public String getDestinationServices() {
        return destinationServices;
    }

    public void setDestinationServices(String destinationServices) {
        this.destinationServices = destinationServices;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getEnableState() {
        return enableState;
    }

    public void setEnableState(String enableState) {
        this.enableState = enableState;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public HttpRetryDto getHttpRetryDto() {
        return httpRetryDto;
    }

    public void setHttpRetryDto(HttpRetryDto httpRetryDto) {
        this.httpRetryDto = httpRetryDto;
    }

    public String getHttpRetry() {
        return httpRetry;
    }

    public void setHttpRetry(String httpRetry) {
        this.httpRetry = httpRetry;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getVirtualCluster() {
        return virtualCluster;
    }

    public void setVirtualCluster(String virtualCluster) {
        this.virtualCluster = virtualCluster;
    }

    public Boolean getNeedRouteMetric() {
        return needRouteMetric;
    }

    public void setNeedRouteMetric(final Boolean needRouteMetric) {
        this.needRouteMetric = needRouteMetric;
    }

    public EnvoyRouteRuleHeaderOperationDto getHeaderOperation() {
        return headerOperation;
    }

    public void setHeaderOperation(EnvoyRouteRuleHeaderOperationDto headerOperation) {
        this.headerOperation = headerOperation;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
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
