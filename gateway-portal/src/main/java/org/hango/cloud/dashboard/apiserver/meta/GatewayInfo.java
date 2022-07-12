package org.hango.cloud.dashboard.apiserver.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 存储各个环境的信息
 *
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/17 下午5:22.
 */
public class GatewayInfo implements Serializable {
    private static final long serialVersionUID = 7147341067988626279L;

    /**
     * 数据库主键id
     */
    private long id;
    /**
     * 创建时间
     */
    private long createDate;
    /**
     * 更新时间
     */
    private long modifyDate;
    /**
     * 网关名称
     */
    private String gwName;
    /**
     * 网关地址
     */
    private String gwAddr;
    /**
     * 网关描述
     */
    private String description;
    /**
     * g0网关健康检查状态
     */
    private int status;
    /**
     * g0网关上次检查时间
     */
    private long lastCheckTime;
    /**
     * g0网关健康检查接口
     */
    private String healthInterfacePath;
    /**
     * 网关所属项目id
     */
    private String projectId;
    /**
     * 网关实例所属环境的环境id
     */
    private String envId;
    /**
     * 网关实例认证中心地址
     */
    private String authAddr;
    /**
     * 网关mongo地址
     */
    private String mongoAddr;
    /**
     * g0网关mysql地址
     */
    private String mysqlAddr;
    /**
     * g0网关数据源切换 mongo/mysql
     */
    private String auditDatasourceSwitch;

    /**
     * 审计数据源
     */
    private String auditDbConfig;
    /**
     * 监控地址
     */
    private String metricUrl;

    private String gwUniId;

    /**
     * 网关类型, envoy/Spring Cloud Gateway
     */
    private String gwType;

    /**
     * envoy网关api-plane地址
     */
    private String apiPlaneAddr;

    /**
     * envoy网关gwClusterName地址
     */
    private String gwClusterName;

    /**
     * Prometheus 地址
     */
    private String promAddr;

    private List<EnvoyVirtualHostInfo> virtualHostList;

    /**
     * camel实例的地址
     */
    private String camelAddr;
    /**
     * 网关端口号
     */
    private int listenerPort;

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

    public String getHealthInterfacePath() {
        return healthInterfacePath;
    }

    public void setHealthInterfacePath(String healthInterfacePath) {
        this.healthInterfacePath = healthInterfacePath;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getAuthAddr() {
        return authAddr;
    }

    public void setAuthAddr(String authAddr) {
        this.authAddr = authAddr;
    }

    public String getMongoAddr() {
        return mongoAddr;
    }

    public void setMongoAddr(String mongoAddr) {
        this.mongoAddr = mongoAddr;
    }

    public String getMysqlAddr() {
        return mysqlAddr;
    }

    public void setMysqlAddr(String mysqlAddr) {
        this.mysqlAddr = mysqlAddr;
    }

    public String getAuditDatasourceSwitch() {
        return auditDatasourceSwitch;
    }

    public void setAuditDatasourceSwitch(String auditDatasourceSwitch) {
        this.auditDatasourceSwitch = auditDatasourceSwitch;
    }

    public String getGwUniId() {
        return gwUniId;
    }

    public void setGwUniId(String gwUniId) {
        this.gwUniId = gwUniId;
    }

    public String getMetricUrl() {
        return metricUrl;
    }

    public void setMetricUrl(String metricUrl) {
        this.metricUrl = metricUrl;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatewayInfo that = (GatewayInfo) o;
        return getId() == that.getId();
    }

    public String getAuditDbConfig() {
        return auditDbConfig;
    }

    public void setAuditDbConfig(String auditDbConfig) {
        this.auditDbConfig = auditDbConfig;
    }

    public String getPromAddr() {
        return promAddr;
    }

    public void setPromAddr(String promAddr) {
        this.promAddr = promAddr;
    }

    public List<EnvoyVirtualHostInfo> getVirtualHostList() {
        return virtualHostList;
    }

    public void setVirtualHostList(List<EnvoyVirtualHostInfo> virtualHostList) {
        this.virtualHostList = virtualHostList;
    }

    public String getCamelAddr() {
        return camelAddr;
    }

    public void setCamelAddr(String camelAddr) {
        this.camelAddr = camelAddr;
    }

    /**
     * 将项目ID字符串转化为项目ID集合
     * 主要用于转化从数据库中取出的ID集合字符串
     *
     * @return 拆分的项目ID集合
     */
    public List<Long> getProjectIdList() {
        return CommonUtil.splitStringToLongList(projectId, ",");
    }

    public int getListenerPort() {
        return listenerPort;
    }

    public void setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
