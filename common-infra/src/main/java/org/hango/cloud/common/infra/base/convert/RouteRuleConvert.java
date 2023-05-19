package org.hango.cloud.common.infra.base.convert;

import com.alibaba.fastjson.JSONObject;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.HttpRetryDto;
import org.hango.cloud.common.infra.route.dto.RouteMapMatchDto;
import org.hango.cloud.common.infra.route.dto.RouteMatchDto;
import org.hango.cloud.common.infra.route.dto.RouteQueryDto;
import org.hango.cloud.common.infra.route.dto.RouteStringMatchDto;
import org.hango.cloud.common.infra.route.pojo.DestinationInfo;
import org.hango.cloud.common.infra.route.pojo.RouteMapMatchInfo;
import org.hango.cloud.common.infra.route.pojo.RouteMatchInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.route.pojo.HttpRetryPO;
import org.hango.cloud.common.infra.route.pojo.RouteStringMatchInfo;
import org.hango.cloud.gdashboard.api.util.BeanUtil;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author zhufengwei
 * @Date 2023/1/12
 */
public class RouteRuleConvert {
    public static RouteStringMatchInfo toMeta(RouteStringMatchDto dto) {
        RouteStringMatchInfo meta = new RouteStringMatchInfo();
        meta.setType(dto.getType());
        meta.setValue(dto.getValue().stream().map(String::trim).sorted().collect(Collectors.toList()));
        return meta;
    }

    public static RouteStringMatchDto fromMeta(RouteStringMatchInfo meta) {
        return BeanUtil.copy(meta, RouteStringMatchDto.class);
    }

    public static RouteMapMatchInfo toMeta(RouteMapMatchDto dto) {
        RouteMapMatchInfo meta = new RouteMapMatchInfo();
        meta.setKey(dto.getKey());
        meta.setType(dto.getType());
        meta.setValue(dto.getValue().stream().map(String::trim).sorted().collect(Collectors.toList()));
        return meta;
    }

    public static RouteMapMatchDto fromMeta(RouteMapMatchInfo meta) {
        return BeanUtil.copy(meta, RouteMapMatchDto.class);
    }


    public static void fillMatchMeta(RouteMatchInfoPO target, RouteMatchDto source){
        //uri
        if (source.getUriMatchDto() != null){
            target.setUri(toMeta(source.getUriMatchDto()));
        }
        //method
        if (source.getMethod() != null){
            target.setMethod(source.getMethod());
        }
        //query param
        if (!CollectionUtils.isEmpty(source.getQueryParams())) {
            List<RouteMapMatchInfo> routeMapMatchInfos = source.getQueryParams().stream().map(RouteRuleConvert::toMeta).collect(Collectors.toList());
            target.setQueryParam(routeMapMatchInfos);
        }
        //header
        if (!CollectionUtils.isEmpty(source.getHeaders())) {
            List<RouteMapMatchInfo> routeMapMatchInfos = source.getHeaders().stream().map(RouteRuleConvert::toMeta).collect(Collectors.toList());
            target.setHeader(routeMapMatchInfos);
        }
        target.setPriority(source.getPriority());
        target.setOrders(calOrders(source));
    }

    public static Long calOrders(RouteMatchDto matchDto){
        int routeNumber = 1;
        //uri
        if (matchDto.getUriMatchDto() != null){
            routeNumber++;
        }
        //method
        if (matchDto.getMethod() != null){
            routeNumber++;
        }
        //query param
        if (!CollectionUtils.isEmpty(matchDto.getQueryParams())) {
            routeNumber++;
        }
        //header
        if (!CollectionUtils.isEmpty(matchDto.getHeaders())) {
            routeNumber++;
        }
        int pathLength = JSONObject.toJSONString(matchDto.getUriMatchDto()).length();

        int isExact = 0;
        switch (matchDto.getUriMatchDto().getType()){
            case BaseConst.URI_TYPE_EXACT:
                isExact = 2;
                break;
            case BaseConst.URI_TYPE_PREFIX:
                isExact = 1;
                break;
        }
        //构造orders :  priority * 1000000 + isExact * 200000 + pathLength * 20 + routeNumber
       return matchDto.getPriority() * 1000000 + isExact * 200000 + pathLength * 20L + routeNumber;
    }

    public static RouteQuery toMeta(RouteQueryDto queryDto){
        return RouteQuery.builder()
                .pattern(queryDto.getPattern())
                .virtualGwId(queryDto.getVirtualGwId())
                .enableStatus(queryDto.getEnableStatus())
                .serviceId(queryDto.getServiceId())
                .projectId(queryDto.getProjectId())
                .sortKey(queryDto.getSortByKey())
                .sortValue(queryDto.getSortByValue())
                .routeIds(queryDto.getRouteRuleIds())
                .build();
    }

    public static void fillMatchView(RouteMatchDto target, RouteMatchInfoPO source) {
        //uri
        target.setUriMatchDto(RouteRuleConvert.fromMeta(source.getUri()));
        //method
        target.setMethod(source.getMethod());
        //query param
        if (!CollectionUtils.isEmpty(source.getQueryParam())) {
            List<RouteMapMatchDto> routeMapMatchDtos = source.getQueryParam().stream().map(RouteRuleConvert::fromMeta).collect(Collectors.toList());
            target.setQueryParams(routeMapMatchDtos);
        }
        if (!CollectionUtils.isEmpty(source.getHeader())) {
            List<RouteMapMatchDto> routeMapMatchDtos = source.getHeader().stream().map(RouteRuleConvert::fromMeta).collect(Collectors.toList());
            target.setHeaders(routeMapMatchDtos);
        }
        target.setOrders(source.getOrders());
        target.setPriority(source.getPriority());
    }

    public static DestinationInfo toMeta(DestinationDto destinationDto){
        DestinationInfo destinationInfo = new DestinationInfo();
        destinationInfo.setServiceId(destinationDto.getServiceId());
        destinationInfo.setMirrorType(destinationDto.getMirrorType());
        destinationInfo.setWeight(destinationDto.getWeight());
        destinationInfo.setSubsetName(destinationDto.getSubsetName());
        destinationInfo.setPort(destinationDto.getPort());
        return destinationInfo;
    }

    public static DestinationDto toView(DestinationInfo destinationInfo){
        DestinationDto destinationDto = new DestinationDto();
        destinationDto.setServiceId(destinationInfo.getServiceId());
        destinationDto.setMirrorType(destinationInfo.getMirrorType());
        destinationDto.setWeight(destinationInfo.getWeight());
        destinationDto.setSubsetName(destinationInfo.getSubsetName());
        destinationDto.setPort(destinationInfo.getPort());
        return destinationDto;
    }


    public static HttpRetryPO toMeta(HttpRetryDto httpRetryDto){
        return HttpRetryPO.builder()
                .isRetry(httpRetryDto.isRetry())
                .retryOn(httpRetryDto.getRetryOn())
                .attempts(httpRetryDto.getAttempts())
                .perTryTimeout(httpRetryDto.getPerTryTimeout())
                .build();
    }

    public static HttpRetryDto toView(HttpRetryPO httpRetryPO){
        HttpRetryDto httpRetryDto = new HttpRetryDto();
        httpRetryDto.setRetry(httpRetryPO.getIsRetry());
        httpRetryDto.setRetryOn(httpRetryPO.getRetryOn());
        httpRetryDto.setAttempts(httpRetryPO.getAttempts());
        httpRetryDto.setPerTryTimeout(httpRetryPO.getPerTryTimeout());
        return httpRetryDto;
    }

}
