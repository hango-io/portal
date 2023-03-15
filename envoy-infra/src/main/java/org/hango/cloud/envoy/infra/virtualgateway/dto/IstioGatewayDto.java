package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class IstioGatewayDto {

    /**
     * 网关名称
     */
    @JSONField(name = "Name")
    private String name;

    /**
     * 网关集群信息
     */
    @JSONField(name = "GwCluster")
    private String gwCluster;

    /**
     * 自定义Ip地址获取方式
     */
    @JSONField(name = "CustomIpAddressHeader")
    private String customIpAddressHeader;

    /**
     * 配置记录XFF右起第几跳IP(默认为1)
     */
    @JSONField(name = "XffNumTrustedHops")
    private Integer xffNumTrustedHops = 1 ;

    /**
     * 配置是否记录上一代理的地址(默认false)
     */
    @JSONField(name = "UseRemoteAddress")
    private Boolean useRemoteAddress;


    /**
     * server配置
     */
    @JSONField(name = "Servers")
    private List<IstioGatewayServerDto> servers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }

    public String getCustomIpAddressHeader() {
        return customIpAddressHeader;
    }

    public void setCustomIpAddressHeader(String customIpAddressHeader) {
        this.customIpAddressHeader = customIpAddressHeader;
    }

    public Integer getXffNumTrustedHops() {
        return xffNumTrustedHops;
    }

    public void setXffNumTrustedHops(Integer xffNumTrustedHops) {
        this.xffNumTrustedHops = xffNumTrustedHops;
    }

    public Boolean getUseRemoteAddress() {
        return useRemoteAddress;
    }

    public void setUseRemoteAddress(Boolean useRemoteAddress) {
        this.useRemoteAddress = useRemoteAddress;
    }

    public List<IstioGatewayServerDto> getServers() {
        return servers;
    }

    public void setServers(List<IstioGatewayServerDto> servers) {
        this.servers = servers;
    }
}
