package org.hango.cloud.envoy.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceTrafficPolicyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SubsetDto;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;


/**
 * 发布服务相关info，与api-plane进行通信
 *
 * @author hanjiahao
 */
@Getter
@Setter
public class DpServiceProxyDto {
    /**
     * 服务唯一标识
     */
    @JSONField(name = "Code")
    private String code;
    /**
     * 网关集群名称
     */
    @JSONField(name = "Gateway")
    private String gateway;
    /**
     * 后端服务
     */
    @JSONField(name = "BackendService")
    private String backendService;

    /**
     * 服务注册类型
     */
    @JSONField(name = "Type")
    private String type;

    @JSONField(name = "Protocol")
    private String protocol;

    /**
     * 服务发布服务标识
     */
    @JSONField(name = "ServiceTag")
    private String serviceTag;

    @JSONField(name = "LoadBalancer")
    private String loadBalancer;

    /**
     * 版本
     */
    @Valid
    @JSONField(name = "Subsets")
    private List<SubsetDto> subsets;

    /**
     * 高级配置，包含负载均衡和连接池
     */
    @JSONField(name = "TrafficPolicy")
    private ServiceTrafficPolicyDto trafficPolicy;

    /**
     * 版本号
     */
    @JSONField(name = "Version")
    private Long version;

    /**
     * meta数据传输集
     * Map<mata_type,meta_data>
     * mata_type meta类型
     *
     * mata_type: 服务meta数据类型
     * meta_data: 服务meta<label_key,label_value>
     */
    @JSONField(name = "MetaMap")
    private Map<String, Map<String,String>> metaMap;

}

