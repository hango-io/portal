package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.gdashboard.api.util.Const;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/5/4
 */
@Getter
@Setter
public class ServiceProxyUpdateDto implements Serializable {
    private static final long serialVersionUID = -2816483055101943401L;

    /**
     * 数据库主键自增id
     */
    @NotNull
    @JSONField(name = "Id")
    private Long id;

    /**
     * 服务别名
     */
    @JSONField(name = "Alias")
    @Pattern(regexp = Const.REGEX_SERVICE_ALIAS)
    private String alias;


    /**
     * 域名,多个以,隔离
     */
    @NotBlank
    @JSONField(name = "Hosts")
    private String hosts;

    /**
     * 服务发布所选服务名称（网关真实名称）
     * 静态发布，则为后端服务host；注册中心发布k8s service
     */
    @NotBlank
    @JSONField(name = "BackendService")
    private String backendService;

    /**
     * 高级配置包含负载均衡策略和连接池
     */
    @Valid
    @JSONField(name = "TrafficPolicy")
    private ServiceTrafficPolicyDto trafficPolicy;


    /**
     * 版本集合
     */
    @JSONField(name = "Subsets")
    private List<SubsetDto> subsets;
}
