package org.hango.cloud.common.advanced.metric.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.dto.TimeQueryDto;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/7
 */
@Getter
@Setter
public class CountDataQueryDto extends TimeQueryDto {

    /**
     * 维度标识，网关为网关GwClusterName,虚拟网关为Code,服务、路由为Name
     */
    @JSONField(name = "DimensionCode")
    private String dimensionCode;

    @JSONField(name = "DimensionType")
    private String dimensionType;

    /**
     * 虚拟网关ID
     */
    @JSONField(name = "VirtualGwCode")
    @NotBlank
    private String virtualGwCode;
}
