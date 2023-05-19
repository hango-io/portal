package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * 路由复制Dto
 *
 * @author hanjiahao
 */
@Getter
@Setter
public class CopyRuleDto {
    /**
     * 路由规则id
     */
    @JSONField(name = "RouteRuleId")
    @Min(1)
    private long routeRuleId;
    /**
     * 复制目标服务id
     */
    @JSONField(name = "ServiceId")
    @Min(1)
    private long serviceId;
    /**
     * 复制后的路由规则优先级
     */
    @JSONField(name = "Priority")
    private long priority;
    /**
     * 复制后的路由规则名称
     */
    @JSONField(name = "RouteRuleName")
    @Pattern(regexp = "([\\s\\S]){1,254}")
    private String routeRuleName;
    /**
     * 复制后的路由规则描述
     */
    @JSONField(name = "Description")
    private String description;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
