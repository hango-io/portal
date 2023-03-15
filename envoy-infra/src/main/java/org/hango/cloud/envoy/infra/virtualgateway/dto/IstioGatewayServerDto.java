package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/10/25
 */
public class IstioGatewayServerDto {
    /**
     * 端口协议
     */
    @JSONField(name = "Protocol")
    private String protocol;

    /**
     * 端口号
     */
    @JSONField(name = "Number")
    private Integer number;

    /**
     * 域名列表
     */
    @JSONField(name = "Hosts")
    private List<String> hosts;

    /**
     * tls配置
     */
    @JSONField(name = "TLSSettings")
    private IstioGatewayTlsDto portalIstioGatewayTLSDTO;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public IstioGatewayTlsDto getPortalIstioGatewayTLSDTO() {
        return portalIstioGatewayTLSDTO;
    }

    public void setPortalIstioGatewayTLSDTO(IstioGatewayTlsDto portalIstioGatewayTLSDTO) {
        this.portalIstioGatewayTLSDTO = portalIstioGatewayTLSDTO;
    }
}
