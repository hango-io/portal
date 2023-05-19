package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.annotation.Regex;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class RouteMatchDto extends CommonExtensionDto {
    /**
     * 路由规则，uriMatchDto
     */
    @JSONField(name = "Uri")
    @Valid
    @Regex(message = "无效的正则表达式", condition = "type=regex", regex = "value")
    RouteStringMatchDto uriMatchDto;

    /**
     * 路由规则，methodMatchDto
     */
    @JSONField(name = "Method")
    @Valid
    List<String> method;

    /**
     * 路由规则，headers
     */
    @Size(max = 5, message = "请求头匹配条件至多配置5组")
    @JSONField(name = "Headers")
    @Valid
    List<RouteMapMatchDto> headers;

    /**
     * 路由规则，queryParams
     */
    @Size(max = 5, message = "请求参数匹配条件至多配置5组")
    @JSONField(name = "QueryParams")
    @Valid
    List<RouteMapMatchDto> queryParams;

    /**
     * 路由规则优先级, 默认为50
     */
    @Range(min = 0, max = 100, message = "非法的路由规则优先级（0 - 100）")
    @JSONField(name = "Priority")
    long priority = 50;


    /**
     * 路由规则orders，发送至api-plane
     * orders = priority * 100000 + isExact * 20000 + pathLength * 20 + routeNumber
     */
    @JSONField(name = "Orders")
    long orders;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
