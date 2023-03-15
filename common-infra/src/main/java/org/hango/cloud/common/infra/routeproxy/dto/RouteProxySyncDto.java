package org.hango.cloud.common.infra.routeproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/11
 */
@Getter
@Setter
public class RouteProxySyncDto {

    /**
     * 路由规则发布指定的网关id
     */
    @NotEmpty
    @JSONField(name = "VirtualGwIds")
    private List<Long> virtualGwIds;

    /**
     * 发布指定的路由规则id
     */
    @JSONField(name = "RouteRuleId")
    @Min(1)
    private Long routeRuleId;
}
