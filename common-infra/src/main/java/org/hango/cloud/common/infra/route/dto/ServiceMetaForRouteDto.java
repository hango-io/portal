package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 路由创建流程中一组关联服务信息，由ServiceId检索全量服务信息
 *
 * @author yutao04
 */
@Data
public class ServiceMetaForRouteDto {

    @Min(value = 1, message = "非法的服务ID")
    @NotNull
    @JSONField(name = "ServiceId")
    private Long serviceId;

    @JSONField(name = "ServiceName")
    private String serviceName;

    @JSONField(name = "Port")
    private Integer port;

    @JSONField(name = "VirtualGateway")
    private String virtualGateway;

    @JSONField(name = "ServiceSource")
    private String serviceSource;

    /**
     * 默认 HTTP 类型
     */
    @JSONField(name = "Protocol")
    private String protocol;

    @JSONField(name = "BackendService")
    private String backendService;

    @Range(min = 0, max = 100, message = "请输入正确范围的服务权重（0 - 100）")
    @NotNull
    @JSONField(name = "Weight")
    private Integer weight;

    @Valid
    @JSONField(name = "DestinationServices")
    private List<DestinationDto> destinationServices;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}