package org.hango.cloud.envoy.infra.grpc.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author xin li
 * @date 2022/5/19 16:23
 */

@Getter
@Setter
@Builder
public class GrpcEnvoyFilterDto {

    /**
     * filter名称
     */
    @JsonProperty(value = "Name")
    private String name;
    /**
     * 网关
     */
    @JSONField(name = "GwCluster")
    private String gwCluster;
    /**
     * 网关的端口号
     */
    @JSONField(name = "PortNumber")
    private int portNumber;

    /**
     * pb文件
     */
    @JSONField(name = "ProtoDescriptorBin")
    private String protoDescriptorBin;

    /**
     * 要支持协议转换的services
     */
    @JSONField(name = "Services")
    private List<String> services;
}
