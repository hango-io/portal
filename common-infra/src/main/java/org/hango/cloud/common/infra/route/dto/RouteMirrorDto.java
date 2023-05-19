package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @Author zhufengwei
 * @Date 2023/1/13
 */
@Getter
@Setter
public class RouteMirrorDto {
    /**
     * 发布指定的路由规则id
     */
    @JSONField(name = "RouteId")
    @Min(1)
    private long routeId;

    /**
     * 路由规则发布指定的网关id
     */
    @NotNull
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;

    /**
     * 流量镜像开关，0关闭，1打开
     */
    @Range(min = 0, max = 1)
    @JSONField(name = "MirrorSwitch")
    private int mirrorSwitch;

    /**
     * 流量镜像规则
     */
    @JSONField(name = "MirrorTraffic")
    private DestinationDto mirrorTraffic;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
