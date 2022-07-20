package org.hango.cloud.dashboard.apiserver.config;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Weng Yanghui(wengyanghui)
 * @Date: 创建时间: 2017/12/6 9:16.
 */
@Service
@PropertySource("classpath:gdashboard-application.properties")
public class ApiServerConfig {


    @Value("${authConfig:true}")
    private boolean authConfig;

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


    @Value("${bakApiPlaneAddr:#{null}}")
    private String bakApiPlaneAddr;


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

    public Boolean getAuditEnable() {
        return auditEnable;
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
}
