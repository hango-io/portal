package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.healthcheck.dto.HealthCheckRuleDto;
import org.hango.cloud.gdashboard.api.util.Const;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * 元数据发布信息相关dto
 *
 * @author hanjiahao
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiceProxyDto extends CommonExtensionDto implements Serializable {
    private static final long serialVersionUID = -2075260227746685133L;
    /**
     * 数据库主键自增id
     */
    @JSONField(name = "Id")
    private Long id;

    /**
     * 服务别名
     */
    @JSONField(name = "Alias")
    @Pattern(regexp = Const.REGEX_SERVICE_ALIAS)
    private String alias;

    /**
     * 服务名称
     */
    @JSONField(name = "Name")
    @NotBlank
    @Pattern(regexp = Const.REGEX_SERVICE_NAME)
    private String name;

    /**
     * 服务发布所选服务名称（网关真实名称）
     * 静态发布，则为后端服务host；注册中心发布k8s service
     */
    @JSONField(name = "BackendService")
    @NotBlank
    private String backendService;

    /**
     * 注册中心类型Consul/Kubernetes/Eureka/Zookeeper，DYNAMIC时必填，默认Kubernetes
     */
    @JSONField(name = "RegistryCenterType")
    private String registryCenterType;


    /**
     * 服务协议，http/dubbo等
     */
    @JSONField(name = "Protocol")
    @NotBlank
    private String protocol;

    /**
     * 服务发布策略，DYNAMIC,STATIC
     */
    @JSONField(name = "PublishType")
    @NotBlank
    private String publishType;

    /**
     * 服务发布指定的虚拟网关id
     */
    @JSONField(name = "VirtualGwId")
    @Min(1)
    private long virtualGwId;

    /**
     * 服务发布指定的网关类型
     */
    @JSONField(name = "GwType")
    private String gwType;

    /**
     * 网关名称，用于前端展示，不进行存储
     */
    @JSONField(name = "VirtualGwName")
    private String virtualGwName;

    /**
     * 网关标识
     */
    @JSONField(name = "VirtualGwCode")
    private String virtualGwCode;

    /**
     * 网关集群名称
     */
    @JSONField(name = "GwClusterName")
    private String gwClusterName;

    @JSONField(name = "GwAddr")
    private String gwAddr;

    @JSONField(name = "EnvId")
    private String envId;

    /**
     * 域名,多个以,隔离
     */
    @JSONField(name = "Hosts")
    private String hosts;

    /**
     * 创建时间
     */
    @JSONField(name = "CreateTime")
    private long createTime;

    /**
     * 更新时间
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;

    @JSONField(name = "LoadBalancer")
    @Pattern(regexp = "|ROUND_ROBIN|LEAST_CONN|RANDOM")
    @Builder.Default
    private String loadBalancer = "ROUND_ROBIN";
    /**
     * 服务健康状态：0表示异常；1表示健康；2表示部分健康
     */
    @JSONField(name = "HealthyStatus")
    private Integer healthyStatus;

    /**
     * 服务发布后，后端服务对应的port信息
     * 只有动态发布，才有port信息，静态发布不存在port信息
     */
    @JSONField(name = "Port")
    private List<Integer> port;

    /**
     * 版本集合
     */
    @JSONField(name = "Subsets")
    private List<SubsetDto> subsets;

    /**
     * 高级配置包含负载均衡策略和连接池
     */
    @JSONField(name = "TrafficPolicy")
    @Valid
    private ServiceTrafficPolicyDto trafficPolicy;

    /**
     * 主动健康检查
     */
    @JSONField(name = "HealthCheck")
    private HealthCheckRuleDto healthCheckRule;

    /**
     * 项目id
     */
    @JSONField(name = "ProjectId")
    private long projectId;


    /**
     * 版本号
     */
    @JSONField(serialize = false)
    private long version;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
