package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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
     * server配置
     */
    @JSONField(name = "Servers")
    private List<IstioGatewayServerDto> servers;

}
