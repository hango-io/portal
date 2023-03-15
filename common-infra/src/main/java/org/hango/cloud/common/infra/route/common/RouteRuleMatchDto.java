package org.hango.cloud.common.infra.route.common;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.base.annotation.Regex;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.route.dto.RouteStringMatchDto;

import javax.validation.Valid;
import java.util.List;

public class RouteRuleMatchDto extends CommonExtensionDto {
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
    RouteStringMatchDto methodMatchDto;

    /**
     * 路由规则，hostMatchDto
     */
    @JSONField(name = "Host")
    @Valid
    RouteStringMatchDto hostMatchDto;

    /**
     * 路由规则，headers
     */
    @JSONField(name = "Headers")
    @Valid
    List<RouteRuleMapMatchDto> headers;

    /**
     * 路由规则，queryParams
     */
    @JSONField(name = "QueryParams")
    @Valid
    List<RouteRuleMapMatchDto> queryParams;

    /**
     * 路由规则优先级, 默认为50
     */
    @JSONField(name = "Priority")
    long priority = 50;


    /**
     * 路由规则orders，发送至api-plane
     * orders = priority * 100000 + isExact * 20000 + pathLength * 20 + routeNumber
     */
    @JSONField(name = "Orders")
    long orders;
    public RouteStringMatchDto getUriMatchDto() {
        return uriMatchDto;
    }

    public void setUriMatchDto(RouteStringMatchDto uriMatchDto) {
        this.uriMatchDto = uriMatchDto;
    }

    public RouteStringMatchDto getMethodMatchDto() {
        return methodMatchDto;
    }

    public void setMethodMatchDto(RouteStringMatchDto methodMatchDto) {
        this.methodMatchDto = methodMatchDto;
    }

    public RouteStringMatchDto getHostMatchDto() {
        return hostMatchDto;
    }

    public void setHostMatchDto(RouteStringMatchDto hostMatchDto) {
        this.hostMatchDto = hostMatchDto;
    }

    public List<RouteRuleMapMatchDto> getHeaders() {
        return headers;
    }

    public void setHeaders(List<RouteRuleMapMatchDto> headers) {
        this.headers = headers;
    }

    public List<RouteRuleMapMatchDto> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<RouteRuleMapMatchDto> queryParams) {
        this.queryParams = queryParams;
    }


    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public long getOrders() {
        return orders;
    }

    public void setOrders(long orders) {
        this.orders = orders;
    }

}
