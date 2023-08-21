package org.hango.cloud.common.infra.route.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.route.dto.*;
import org.hango.cloud.common.infra.route.pojo.RoutePO;
import org.hango.cloud.common.infra.route.service.ICopyRoute;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 路由创建 Web 层（创建即发布）
 *
 * @author yutao04
 * @since 2023.03.23
 */
@Validated
@RestController
@RequestMapping(value = {BaseConst.ROUTE_PATH_V1})
public class RouteV1Controller extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(RouteV1Controller.class);

    @Autowired
    private IRouteService routeService;

    @Autowired
    private ICopyRoute copyRouteRuleProxy;

    @PostMapping(params = {"Action=CreateRoute"})
    public String createRoute(@Valid @RequestBody RouteDto routeDto) {
        logger.info("创建路由（创建即发布）, CreateRoute dto:{}", routeDto);

        ErrorCode result = routeService.fillRouteInfo(routeDto);
        if (!CommonErrorCode.SUCCESS.equals(result)) {
            return apiReturn(result);
        }

        result = routeService.checkCreateParam(routeDto);
        if (!CommonErrorCode.SUCCESS.equals(result)) {
            return apiReturn(result);
        }

        long routeId = routeService.create(routeDto);
        return apiReturn(new Result(routeId));
    }

    @GetMapping(params = {"Action=DescribeRouteList"})
    public String describeRouteList(@Validated RouteQueryDto ruleQueryDto) {
        logger.info("分页查询路由规则列表, ruleQueryDto：{}", JSON.toJSONString(ruleQueryDto));

        ruleQueryDto.setProjectId(ProjectTraceHolder.getProId());
        Page<RoutePO> page = routeService.getRoutePage(ruleQueryDto);
        Map<String, Object> result = Maps.newHashMap();
        result.put(TOTAL_COUNT, page.getTotal());
        if (page.getTotal() > 0) {
            List<RouteDto> routeDtoList = page.getRecords().stream().map(routeService::toView).collect(Collectors.toList());
            result.put(BaseConst.ROUTE_RULE_PROXY_LIST, routeDtoList);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @GetMapping(params = {"Action=DescribeRoute"})
    public String describeRoute(@Min(1) @RequestParam(value = "Id") long id) {
        logger.info("根据路由ID查询路由详情，路由ID：{}", id);

        RouteDto routeDto = routeService.get(id);

        Map<String, Object> result = Maps.newHashMap();
        result.put("Route", routeDto);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @PostMapping(params = {"Action=UpdateRoute"})
    public String updateRoute(@Validated @RequestBody UpdateRouteDto updateRouteDto) {
        logger.info("更新路由规则，routeDto: {}", updateRouteDto);
        RouteDto routeDto = routeService.fillUpdateInfo(updateRouteDto);
        ErrorCode result = routeService.fillRouteInfo(routeDto);
        if (!CommonErrorCode.SUCCESS.equals(result)) {
            return apiReturn(result);
        }

        ErrorCode checkResult = routeService.checkUpdateParam(routeDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        routeService.update(routeDto);
        return apiReturn(new Result(routeDto.getId()));
    }

    @PostMapping(params = {"Action=DeleteRoute"})
    public String deleteRoute(@Min(1) @RequestParam(value = "RouteId") long routeId) {
        logger.info("根据路由规则删除路由 id: {}", routeId);
        RouteDto routeDto = routeService.get(routeId);
        if (routeDto == null) {
            logger.info("下线路由规则，路由未发布");
            return apiReturn(CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED);
        }
        ErrorCode errorCode = routeService.checkDeleteParam(routeDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        routeService.delete(routeDto);
        return apiReturn(new Result(routeDto.getId()));
    }

    @PostMapping(params = {"Action=UpdateRouteEnableState"})
    public Object updateRouteEnableState(@Min(1) @RequestParam(value = "RouteId") long routeId,
                                         @RequestParam(value = "EnableState", defaultValue = "enable") String enableState) {
        logger.info("根据路由id：{}，使能状态:{} 更新路由发布信息", routeId, enableState);
        RouteDto routeDto = routeService.get(routeId);
        ErrorCode errorCode = routeService.checkUpdateParam(routeDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        routeDto.setEnableState(enableState);
        long id = routeService.update(routeDto);
        if (id == BaseConst.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturnSuccess(null);
    }

    @PostMapping(params = {"Action=CopyRoute"})
    public Object copyRoute(@Min(1) @RequestParam(value = "RouteId") long routeId,
                            @Min(1) @RequestParam(value = "OriginGwId") long originGwId,
                            @Min(1) @RequestParam(value = "DesGwId") long desGwId) {
        logger.info("[copy route] 一键发布已发布路由,路由id: {}, 源网关originGwId: {},目标网关desGwId: {}",
                routeId, originGwId, desGwId);

        ErrorCode checkResult = copyRouteRuleProxy.checkCopyRoute(routeId, originGwId, desGwId);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        CopyRouteDTO copyRouteId = copyRouteRuleProxy.copyRoute(routeId, originGwId, desGwId);
        if (copyRouteId == null || !copyRouteId.isCopySuccess()) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturnSuccess(copyRouteId);
    }

    @PostMapping(params = {"Action=PublishRouteMirror"})
    public String publishRouteMirror(@Valid @RequestBody RouteMirrorDto routeMirrorDto) {
        logger.info("发布路由流量镜像规则, routeMirrorDto:{}", routeMirrorDto);

        ErrorCode checkResult = routeService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        ErrorCode filResult = routeService.fillRouteMirrorDto(routeMirrorDto);
        if (!CommonErrorCode.SUCCESS.equals(filResult)) {
            return apiReturn(filResult);
        }
        long id = routeService.publishMirrorTraffic(routeMirrorDto);
        if (id == BaseConst.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturnSuccess(null);
    }
}