package org.hango.cloud.common.infra.base.convert;

import com.alibaba.fastjson.JSONObject;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.common.*;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.HttpRetryDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleQueryDto;
import org.hango.cloud.common.infra.route.dto.RouteStringMatchDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleMatchInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.routeproxy.meta.HttpRetryPO;
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

    public static RouteRuleMapMatchInfo toMeta(RouteRuleMapMatchDto dto) {
        RouteRuleMapMatchInfo meta = new RouteRuleMapMatchInfo();
        meta.setKey(dto.getKey());
        meta.setType(dto.getType());
        meta.setValue(dto.getValue().stream().map(String::trim).sorted().collect(Collectors.toList()));
        return meta;
    }

    public static RouteRuleMapMatchDto fromMeta(RouteRuleMapMatchInfo meta) {
        return BeanUtil.copy(meta, RouteRuleMapMatchDto.class);
    }


    public static void fillMatchMeta(RouteRuleMatchInfoPO target, RouteRuleMatchDto source){
        //uri
        if (source.getUriMatchDto() != null){
            target.setUri(toMeta(source.getUriMatchDto()));
        }
        //method
        if (source.getMethodMatchDto() != null){
            target.setMethod(toMeta(source.getMethodMatchDto()));
        }
        //host
        if (source.getHostMatchDto() != null){
            target.setHost(toMeta(source.getHostMatchDto()));
        }
        //query param
        if (!CollectionUtils.isEmpty(source.getQueryParams())) {
            List<RouteRuleMapMatchInfo> routeRuleMapMatchInfos = source.getQueryParams().stream().map(RouteRuleConvert::toMeta).collect(Collectors.toList());
            target.setQueryParam(routeRuleMapMatchInfos);
        }
        //header
        if (!CollectionUtils.isEmpty(source.getHeaders())) {
            List<RouteRuleMapMatchInfo> routeRuleMapMatchInfos = source.getHeaders().stream().map(RouteRuleConvert::toMeta).collect(Collectors.toList());
            target.setHeader(routeRuleMapMatchInfos);
        }
        target.setPriority(source.getPriority());
        target.setOrders(calOrders(source));
    }

    public static Long calOrders(RouteRuleMatchDto matchDto){
        int routeNumber = 1;
        //uri
        if (matchDto.getUriMatchDto() != null){
            routeNumber++;
        }
        //method
        if (matchDto.getMethodMatchDto() != null){
            routeNumber++;
        }
        //host
        if (matchDto.getHostMatchDto() != null){
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

    public static RouteRuleQuery toMeta(RouteRuleQueryDto queryDto){
        return RouteRuleQuery.builder()
                .pattern(queryDto.getPattern())
                .virtualGwId(queryDto.getVirtualGwId())
                .publishStatus(queryDto.getPublishStatus())
                .serviceId(queryDto.getServiceId())
                .projectId(queryDto.getProjectId())
                .sortKey(queryDto.getSortByKey())
                .sortValue(queryDto.getSortByValue())
                .build();
    }

    public static void fillMatchView(RouteRuleMatchDto target, RouteRuleMatchInfoPO source) {
        //uri
        target.setUriMatchDto(RouteRuleConvert.fromMeta(source.getUri()));
        //method
        target.setMethodMatchDto(RouteRuleConvert.fromMeta(source.getMethod()));
        //host
        target.setHostMatchDto(RouteRuleConvert.fromMeta(source.getHost()));
        //query param
        if (!CollectionUtils.isEmpty(source.getQueryParam())) {
            List<RouteRuleMapMatchDto> routeRuleMapMatchDtos = source.getQueryParam().stream().map(RouteRuleConvert::fromMeta).collect(Collectors.toList());
            target.setQueryParams(routeRuleMapMatchDtos);
        }
        if (!CollectionUtils.isEmpty(source.getHeader())) {
            List<RouteRuleMapMatchDto> routeRuleMapMatchDtos = source.getHeader().stream().map(RouteRuleConvert::fromMeta).collect(Collectors.toList());
            target.setHeaders(routeRuleMapMatchDtos);
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
