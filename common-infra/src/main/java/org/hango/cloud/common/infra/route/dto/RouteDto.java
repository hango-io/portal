package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hibernate.validator.constraints.Range;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.RegexConst.REGEX_NAME;

/**
 * 路由创建 Dto
 *
 * @author yutao04
 * @since 2023.03.23
 */
@Data
public class RouteDto extends RouteMatchDto {
    @JSONField(name = "Id")
    private Long id;

    /**
     * 路由规则名称，用于前端展示
     */
    @Pattern(regexp = REGEX_NAME, message = "路由名称格式非法")
    @JSONField(name = "Name")
    private String name;

    /**
     * 路由规则别名，可选填
     */
    @Size(max = 100, message = "路由别名长度不能超过100个字符")
    @JSONField(name = "Alias")
    private String alias;

    /**
     * 描述信息
     */
    @Size(max = 200, message = "路由描述信息长度不能超过200个字符")
    @JSONField(name = "Description")
    private String description;

    /**
     * 路由规则发布指定的网关id
     */
    @NotNull
    @Min(value = 1, message = "非法的发布网关ID")
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;

    @JSONField(name = "VirtualGwName")
    private String virtualGwName;

    @JSONField(name = "VirtualGwType")
    private String virtualGwType;

    @JSONField(name = "VirtualGwCode")
    private String virtualGwCode;

    /**
     * 来源于网关信息
     */
    @JSONField(name = "EnvId")
    private String envId;

    /**
     * 路由规则发布指定的网关类型
     */
    @Pattern(regexp = "envoy|scg", message = "路由规则发布指定的网关类型")
    @JSONField(name = "GwType")
    private String gwType;

    /**
     * 路由规则发布时间
     */
    @JSONField(name = "CreateTime")
    private long createTime;

    /**
     * 路由规则发布信息更新时间
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;

    /**
     * 路由关联服务信息
     */
    @JSONField(name = "ServiceMetaForRoute")
    @Valid
    private List<ServiceMetaForRouteDto> serviceMetaForRouteDtos;

    /**
     * 关联服务的域名合集（已去重）
     */
    @JSONField(name = "Hosts")
    private List<String> hosts;

    /**
     * 使能状态
     */
    @JSONField(name = "EnableState")
    @Pattern(regexp = "enable|disable", message = "无效的路由使能状态")
    private String enableState = BaseConst.DISABLE_STATE;

    /**
     * 路由超时时间
     */
    @JSONField(name = "Timeout")
    @Min(0)
    @Max(1024000)
    private long timeout = 60000;

    /**
     * route retry policy
     */
    @JSONField(name = "HttpRetry")
    @Valid
    private HttpRetryDto httpRetryDto;


    /**
     * 流量镜像规则
     */
    @JSONField(name = "MirrorTraffic")
    private DestinationDto mirrorTraffic;

    /**
     * 流量镜像开关，0关闭，1打开
     */
    @Range(min = 0, max = 1)
    @JSONField(name = "MirrorSwitch")
    private int mirrorSwitch;

    /**
     * 路由规则所属项目id
     */
    @JSONField(name = "ProjectId")
    private long projectId;

    /**
     * 存储元数据信息，不存储数据库，仅用于信息传递（需要传给api-plane作为VirtualService资源的metadata信息）
     * 主要用于传播一些功能开关，典型使用方式见如下类
     * @see org.hango.cloud.envoy.infra.dubbo.service.impl.DubboBindingServiceImpl#publishToEnvoy
     *
     */
    @JSONField(name = "MetaMap")
    private Map<String, String> metaMap;

    /**
     * 基于服务元数据获取服务ID集合
     *
     * @return 服务ID集合
     */
    public List<Long> getServiceIds() {
        if (CollectionUtils.isEmpty(serviceMetaForRouteDtos)) {
            return new ArrayList<>();
        }
        return serviceMetaForRouteDtos.stream().map(ServiceMetaForRouteDto::getServiceId).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
