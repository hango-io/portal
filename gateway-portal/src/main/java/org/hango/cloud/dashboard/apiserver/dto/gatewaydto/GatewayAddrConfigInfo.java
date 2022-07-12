package org.hango.cloud.dashboard.apiserver.dto.gatewaydto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Pattern;

/**
 * @author hanjiahao
 * 网关gateway相关地址配置信息
 */
@Validated
public class GatewayAddrConfigInfo {

    /**
     * 网关认证中心配置
     */
    @JSONField(name = "AuthAddr")
    private String authAddr;

    /**
     * 网关环境配置
     */
    @JSONField(name = "EnvId")
    private String envId;

    /**
     * 网关真实英文唯一id
     */
    @JSONField(name = "GwUniId")
    private String gwUniId;

    /**
     * 网关对接的审计数据源类型
     */
    @JSONField(name = "AuditDatasourceSwitch")
    @Pattern(regexp = "mongo|mysql|elasticsearch", message = "网关对接的审计数据源类型不合法")
    private String auditDatasourceSwitch;

    /**
     * 网关对接的审计数据源的地址
     */
    @JSONField(name = "AuditDbConfig")
    private String auditDbConfig;

    /**
     * 网关监控地址
     */
    @JSONField(name = "MetricUrl")
    @Pattern(regexp = "^(http://|https://)\\S{5,254}", message = "网关对接监控地址不合法")
    private String metricUrl;

    public String getAuthAddr() {
        return authAddr;
    }

    public void setAuthAddr(String authAddr) {
        this.authAddr = authAddr;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getGwUniId() {
        return gwUniId;
    }

    public void setGwUniId(String gwUniId) {
        this.gwUniId = gwUniId;
    }

    public String getAuditDatasourceSwitch() {
        return auditDatasourceSwitch;
    }

    public void setAuditDatasourceSwitch(String auditDatasourceSwitch) {
        this.auditDatasourceSwitch = auditDatasourceSwitch;
    }

    public String getAuditDbConfig() {
        return auditDbConfig;
    }

    public void setAuditDbConfig(String auditDConfig) {
        this.auditDbConfig = auditDConfig;
    }

    public String getMetricUrl() {
        return metricUrl;
    }

    public void setMetricUrl(String metricUrl) {
        this.metricUrl = metricUrl;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
