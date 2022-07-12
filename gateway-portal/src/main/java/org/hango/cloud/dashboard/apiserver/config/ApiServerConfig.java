package org.hango.cloud.dashboard.apiserver.config;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Weng Yanghui(wengyanghui@corp.netease.com)
 * @Date: 创建时间: 2017/12/6 9:16.
 */
@Service
@PropertySource("classpath:gdashboard-application.properties")
public class ApiServerConfig {

    /**
     * API Server Host url
     */
    @Value("${api-server.host.url}")
    private String apiServerUrl;

    /**
     * 统计数据自动保存开关
     */
    @Value("${statisticsInfoAutoSave.enable}")
    private boolean statisticsInfoAutoSaveEnabled;

    /**
     * 统计数据自动保存最大天数
     */
    @Value("${statisticsInfoAutoSave.maxDay}")
    private int statisticsInfoAutoSaveMaxDay;

    /**
     * 是否处于轻舟环境下，验证方式不同
     */
    private boolean skiffAuth;

    /**
     * 轻舟认证平台地址
     */
    @Value("${skiffAuthorityAddr}")
    private String skiffAuthorityAddr;

    @Value("${nsf.audit.url}")
    private String auditUrl;

    /**
     * 元数据地址
     */
    @Value("${nsfMetaUrl}")
    private String nsfMetaUrl;

    @Value("${authConfig:true}")
    private boolean authConfig;

    /**
     * 告警地址
     */
    @Value("${alertRuleUrl}")
    private String alertRuleUrl;

    /**
     * nsf 认证相关配置
     */
    @Value("${nsf.registry.auth.ak}")
    private String authAccessKey;
    @Value("${nsf.registry.auth.sk}")
    private String authSecretKey;
    @Value("${nsf.registry.auth.enable:false}")
    private boolean nsfAuthEnable = false;
    /**
     * 配置变更审计开关，默认为false
     */
    @Value("${configUpdateAudit:false}")
    private boolean configUpdateAudit = false;

    /**
     * 插件配置额外信息
     */
    @Value("${pluginManagerExtra:#{null}}")
    private String pluginManagerExtra;

    @Value("${g0AuditDatabasePrefix:nce_gateway_}")
    private String g0AuditDatabasePrefix = "nce_gateway_";

    @Value("${bakApiPlaneAddr:#{null}}")
    private String bakApiPlaneAddr;


    @Value("${permissionScopeAccount:admin}")
    private String permissionScopeAccount = "admin";

    @Value("${metaRefreshInterval:30000}")
    private Integer metaRefreshInterval;

    /**
     * 路由stats指标从path获取
     */
    @Value("${routeMetricPathStats:false}")
    private boolean routeMetricPathStats = false;
    private String auditDescription;
    private Boolean auditEnable = false;

    public boolean getConfigUpdateAudit() {
        return configUpdateAudit;
    }

    public void setConfigUpdateAudit(boolean configUpdateAudit) {
        this.configUpdateAudit = configUpdateAudit;
    }

    public String getAuditDescription() {
        return auditDescription;
    }

    public void setAuditDescription(String auditDescription) {
        this.auditDescription = auditDescription;
    }

    public String getAuthAccessKey() {
        return authAccessKey;
    }

    public void setAuthAccessKey(String authAccessKey) {
        this.authAccessKey = authAccessKey;
    }

    public String getAuthSecretKey() {
        return authSecretKey;
    }

    public void setAuthSecretKey(String authSecretKey) {
        this.authSecretKey = authSecretKey;
    }

    public boolean getNsfAuthEnable() {
        return nsfAuthEnable;
    }

    public void setNsfAuthEnable(boolean nsfAuthEnable) {
        this.nsfAuthEnable = nsfAuthEnable;
    }

    public String getAlertRuleUrl() {
        return alertRuleUrl;
    }

    public void setAlertRuleUrl(String alertRuleUrl) {
        this.alertRuleUrl = alertRuleUrl;
    }

    public String getApiServerUrl() {
        return apiServerUrl;
    }

    public void setApiServerUrl(String apiServerUrl) {
        this.apiServerUrl = apiServerUrl;
    }

    public String getAuditUrl() {
        return auditUrl;
    }

    public Boolean getAuditEnable() {
        return auditEnable;
    }

    public Boolean getStatisticsInfoAutoSaveEnabled() {
        return statisticsInfoAutoSaveEnabled;
    }

    public void setStatisticsInfoAutoSaveEnabled(Boolean statisticsInfoAutoSaveEnabled) {
        this.statisticsInfoAutoSaveEnabled = statisticsInfoAutoSaveEnabled;
    }

    public int getStatisticsInfoAutoSaveMaxDay() {
        return statisticsInfoAutoSaveMaxDay;
    }

    public void setStatisticsInfoAutoSaveMaxDay(int statisticsInfoAutoSaveMaxDay) {
        this.statisticsInfoAutoSaveMaxDay = statisticsInfoAutoSaveMaxDay;
    }

    public String getSkiffAuthorityAddr() {
        return skiffAuthorityAddr;
    }

    public void setSkiffAuthorityAddr(String skiffAuthorityAddr) {
        this.skiffAuthorityAddr = skiffAuthorityAddr;
    }


    public String getNsfMetaUrl() {
        return nsfMetaUrl;
    }

    public void setNsfMetaUrl(String nsfMetaUrl) {
        this.nsfMetaUrl = nsfMetaUrl;
    }

    public boolean getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(boolean authConfig) {
        this.authConfig = authConfig;
    }

    public String getPluginManagerExtra() {
        return pluginManagerExtra;
    }

    public void setPluginManagerExtra(String pluginManagerExtra) {
        this.pluginManagerExtra = pluginManagerExtra;
    }

    public Map<String, String> getPluginManagerMap() {
        HashMap<String, String> pluginManagerMap = Maps.newHashMap();
        String pluginManagerExtra = getPluginManagerExtra();
        if (StringUtils.isBlank(pluginManagerExtra)) {
            return pluginManagerMap;
        }
        String[] split = pluginManagerExtra.split(",");
        for (String item : split) {
            String[] content = item.split(":");
            pluginManagerMap.put(content[0], content[1]);
        }
        return pluginManagerMap;
    }

    public String getG0AuditDatabasePrefix() {
        return g0AuditDatabasePrefix;
    }

    public void setG0AuditDatabasePrefix(String g0AuditDatabasePrefix) {
        this.g0AuditDatabasePrefix = g0AuditDatabasePrefix;
    }

    public String getBakApiPlaneAddr() {
        return bakApiPlaneAddr;
    }

    public void setBakApiPlaneAddr(String bakApiPlaneAddr) {
        this.bakApiPlaneAddr = bakApiPlaneAddr;
    }

    public Integer getMetaRefreshInterval() {
        return metaRefreshInterval;
    }

    public void setMetaRefreshInterval(Integer metaRefreshInterval) {
        this.metaRefreshInterval = metaRefreshInterval;
    }

    public boolean getRouteMetricPathStats() {
        return routeMetricPathStats;
    }

    public void setRouteMetricPathStats(final boolean routeMetricPathStats) {
        this.routeMetricPathStats = routeMetricPathStats;
    }

    public String getPermissionScopeAccount() {
        return permissionScopeAccount;
    }

    public void setPermissionScopeAccount(final String permissionScopeAccount) {
        this.permissionScopeAccount = permissionScopeAccount;
    }
}
