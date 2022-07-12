package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleMatchInfo;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RouteRuleMatchDto {
    /**
     * 路由规则，uriMatchDto
     */
    @JSONField(name = "Uri")
    @Valid
    EnvoyRouteStringMatchDto uriMatchDto;

    /**
     * 路由规则，methodMatchDto
     */
    @JSONField(name = "Method")
    @Valid
    EnvoyRouteStringMatchDto methodMatchDto;

    /**
     * 路由规则，hostMatchDto
     */
    @JSONField(name = "Host")
    @Valid
    EnvoyRouteStringMatchDto hostMatchDto;

    /**
     * 路由规则，headers
     */
    @JSONField(name = "Headers")
    @Valid
    List<EnvoyRouteRuleMapMatchDto> headers;

    /**
     * 路由规则，queryParams
     */
    @JSONField(name = "QueryParams")
    @Valid
    List<EnvoyRouteRuleMapMatchDto> queryParams;

    /**
     * 路由规则优先级, 默认为50
     */
    @JSONField(name = "Priority")
    long priority = 50;

    public EnvoyRouteStringMatchDto getUriMatchDto() {
        return uriMatchDto;
    }

    public void setUriMatchDto(EnvoyRouteStringMatchDto uriMatchDto) {
        this.uriMatchDto = uriMatchDto;
    }

    public EnvoyRouteStringMatchDto getMethodMatchDto() {
        return methodMatchDto;
    }

    public void setMethodMatchDto(EnvoyRouteStringMatchDto methodMatchDto) {
        this.methodMatchDto = methodMatchDto;
    }

    public EnvoyRouteStringMatchDto getHostMatchDto() {
        return hostMatchDto;
    }

    public void setHostMatchDto(EnvoyRouteStringMatchDto hostMatchDto) {
        this.hostMatchDto = hostMatchDto;
    }

    public List<EnvoyRouteRuleMapMatchDto> getHeaders() {
        return headers;
    }

    public void setHeaders(List<EnvoyRouteRuleMapMatchDto> headers) {
        this.headers = headers;
    }

    public List<EnvoyRouteRuleMapMatchDto> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<EnvoyRouteRuleMapMatchDto> queryParams) {
        this.queryParams = queryParams;
    }


    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public void toRouteMeta(RouteRuleMatchInfo sourceMatchInfo) {
        //path值按照字典序排序
        this.uriMatchDto.setValue(this.uriMatchDto.getValue().stream().sorted().collect(Collectors.toList()));
        sourceMatchInfo.setUriMatchInfo(EnvoyRouteStringMatchDto.toMeta(this.uriMatchDto));
        sourceMatchInfo.setUri(JSON.toJSONString(sourceMatchInfo.getUriMatchInfo()));
        int routeNumber = 1;
        sourceMatchInfo.setPriority(this.priority);
        if (this.methodMatchDto != null) {
            //method值按照字典序排序
            this.methodMatchDto.setValue(methodMatchDto.getValue().stream().sorted().collect(Collectors.toList()));
            sourceMatchInfo.setMethodMatchInfo(EnvoyRouteStringMatchDto.toMeta(methodMatchDto));
            sourceMatchInfo.setMethod(JSON.toJSONString(sourceMatchInfo.getMethodMatchInfo()));
            routeNumber++;
        } else {
            sourceMatchInfo.setMethodMatchInfo(null);
            sourceMatchInfo.setMethod(null);
        }

        if (this.hostMatchDto != null) {
            //host值按照字典序排序
            this.hostMatchDto.setValue(hostMatchDto.getValue().stream().sorted().collect(Collectors.toList()));
            sourceMatchInfo.setHostMatchInfo(EnvoyRouteStringMatchDto.toMeta(hostMatchDto));
            sourceMatchInfo.setHost(JSON.toJSONString(sourceMatchInfo.getHostMatchInfo()));
            routeNumber++;
        } else {
            sourceMatchInfo.setHostMatchInfo(null);
            sourceMatchInfo.setHost(null);
        }

        if (CollectionUtils.isNotEmpty(this.headers)) {
            //headers值按照字典序排序
            this.headers = headers.stream().map(EnvoyRouteRuleMapMatchDto::sortValue).collect(Collectors.toList());
            sourceMatchInfo.setHeaderList(headers.stream().map(EnvoyRouteRuleMapMatchDto::toMeta).collect(Collectors.toList()));
            sourceMatchInfo.setHeader(JSON.toJSONString(sourceMatchInfo.getHeaderList()));
            routeNumber++;
        } else {
            sourceMatchInfo.setHeader(null);
            sourceMatchInfo.setHeaderList(new ArrayList<>());
        }

        if (CollectionUtils.isNotEmpty(this.queryParams)) {
            //queryParam值按照字典序排序
            this.queryParams = queryParams.stream().map(EnvoyRouteRuleMapMatchDto::sortValue).collect(Collectors.toList());
            sourceMatchInfo.setQueryParamList(queryParams.stream().map(EnvoyRouteRuleMapMatchDto::toMeta).collect(Collectors.toList()));
            sourceMatchInfo.setQueryParam(JSON.toJSONString(sourceMatchInfo.getQueryParamList()));
            routeNumber++;
        } else {
            sourceMatchInfo.setQueryParam(null);
            sourceMatchInfo.setQueryParamList(new ArrayList<>());
        }


        int pathLength = sourceMatchInfo.getUri().length();
        int isExact = Const.URI_TYPE_EXACT.equals(sourceMatchInfo.getUriMatchInfo().getType()) ? 2 :
                Const.URI_TYPE_PREFIX.equals(sourceMatchInfo.getUriMatchInfo().getType()) ? 1 : 0;

        //构造orders :  priority * 1000000 + isExact * 200000 + pathLength * 20 + routeNumber
        long orders = this.priority * 1000000 + isExact * 200000 + pathLength * 20 + routeNumber;
        sourceMatchInfo.setOrders(orders);
    }

    public void fromRouteMeta(RouteRuleMatchInfo matchInfo) {
        //uri
        this.setUriMatchDto(EnvoyRouteStringMatchDto.fromMeta(matchInfo.getUriMatchInfo()));
        //method
        this.setMethodMatchDto(EnvoyRouteStringMatchDto.fromMeta(matchInfo.getMethodMatchInfo()));
        //host
        this.setHostMatchDto(EnvoyRouteStringMatchDto.fromMeta(matchInfo.getHostMatchInfo()));
        //query param
        if (StringUtils.isNotEmpty(matchInfo.getQueryParam())) {
            this.setQueryParams(JSON.parseArray(matchInfo.getQueryParam(), EnvoyRouteRuleMapMatchDto.class));
        } else {
            this.setQueryParams(new ArrayList<>());
        }
        //header
        if (StringUtils.isNotEmpty(matchInfo.getHeader())) {
            this.setHeaders(JSON.parseArray(matchInfo.getHeader(), EnvoyRouteRuleMapMatchDto.class));
        } else {
            this.setHeaders(new ArrayList<>());
        }
        this.setPriority(matchInfo.getPriority());
    }
}
