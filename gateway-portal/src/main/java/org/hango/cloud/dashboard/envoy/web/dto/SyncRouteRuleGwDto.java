package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;

/**
 * 同步路由获取网关及同步状态是否相同
 *
 * @author hanjiahao
 */
public class SyncRouteRuleGwDto {
    /**
     * 网关信息
     */
    @JSONField(name = "GatewayDto")
    private GatewayDto gatewayDto;

    /**
     * 路由元数据是否相同
     */
    @JSONField(name = "IsSameRaw")
    private Boolean isSameRaw;

    public SyncRouteRuleGwDto(GatewayDto gatewayDto, Boolean isSameRaw) {
        this.gatewayDto = gatewayDto;
        this.isSameRaw = isSameRaw;
    }

    public GatewayDto getGatewayDto() {
        return gatewayDto;
    }

    public void setGatewayDto(GatewayDto gatewayDto) {
        this.gatewayDto = gatewayDto;
    }

    public Boolean getSameRaw() {
        return isSameRaw;
    }

    public void setSameRaw(Boolean sameRaw) {
        isSameRaw = sameRaw;
    }
}
