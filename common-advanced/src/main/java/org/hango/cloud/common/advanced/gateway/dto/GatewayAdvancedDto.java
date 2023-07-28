package org.hango.cloud.common.advanced.gateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 与前端交互的网关dto
 */
public class GatewayAdvancedDto extends GatewayDto implements Serializable {

    private static final long serialVersionUID = 4869081435639481929L;
    /**
     * 网关监控地址
     */
    @JSONField(name = "MetricUrl")
    @Pattern(regexp = "^(http://|https://)\\S{5,254}", message = "网关对接监控地址不合法")
    private String metricUrl;

    /**
     * 网关对接的审计数据源的地址
     */
    @JSONField(name = "AuditDbConfig")
    private String auditDbConfig;

    public GatewayAdvancedDto() {
    }

    public GatewayAdvancedDto cast(GatewayDto gatewayDto) {
        if (gatewayDto == null) {
            return this;
        }
        this.setId(gatewayDto.getId());
        this.setCreateTime(gatewayDto.getCreateTime());
        this.setModifyTime(gatewayDto.getModifyTime());
        this.setName(gatewayDto.getName());
        this.setDescription(gatewayDto.getDescription());
        this.setType(gatewayDto.getType());
        this.setConfAddr(gatewayDto.getConfAddr());
        this.setSvcName(gatewayDto.getSvcName());
        this.setSvcType(gatewayDto.getSvcType());
        this.setGwClusterName(gatewayDto.getGwClusterName());
        this.setEnvId(gatewayDto.getEnvId());
        return this;
    }

    public String getMetricUrl() {
        return metricUrl;
    }

    public void setMetricUrl(String metricUrl) {
        this.metricUrl = metricUrl;
    }


    public String getAuditDbConfig() {
        return auditDbConfig;
    }

    public void setAuditDbConfig(String auditDbConfig) {
        this.auditDbConfig = auditDbConfig;
    }

}
