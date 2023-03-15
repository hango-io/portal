package org.hango.cloud.common.infra.route.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.annotation.MethodReentrantLock;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.operationaudit.annotation.Audit;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleQueryDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleInfoPO;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 路由规则管理Controller层
 *
 * @author hzchenzhongyang 2019-09-11
 */
@RestController
@Validated
@RequestMapping(value = {BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class RouteRuleInfoController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(RouteRuleInfoController.class);

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;

    @MethodReentrantLock
    @Audit(eventName = "CreateRouteRule", description = "创建路由规则")
    @RequestMapping(params = {"Action=CreateRouteRule"}, method = RequestMethod.POST)
    public String createRouteRule(@Validated @RequestBody RouteRuleDto routeRuleDto) {
        logger.info("创建路由规则，routeRuleDto:{}", routeRuleDto);
        routeRuleDto.setProjectId(ProjectTraceHolder.getProId());
        ErrorCode checkResult = routeRuleInfoService.checkCreateParam(routeRuleDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        long id = routeRuleInfoService.create(routeRuleDto);
        Map<String, Object> result = new HashMap<>(BaseConst.DEFAULT_MAP_SIZE);
        result.put("RouteRuleId", id);
        return apiReturn(HttpStatus.SC_CREATED, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdateRouteRule", description = "更新路由规则")
    @RequestMapping(params = {"Action=UpdateRouteRule"}, method = RequestMethod.POST)
    public String updateRouteRule(@Validated @RequestBody RouteRuleDto routeRuleDto) {
        logger.info("更新路由规则，routeRuleDto: {}", routeRuleDto);

        ErrorCode checkResult = routeRuleInfoService.checkUpdateParam(routeRuleDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        routeRuleInfoService.update(routeRuleDto);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @RequestMapping(params = {"Action=DescribeRouteRule"}, method = RequestMethod.GET)
    public String getRouteRule(@Min(1) @RequestParam(value = "RouteRuleId") long id) {
        logger.info("根据路由规则id查询路由规则详情，id:{}", id);
        RouteRuleDto routeRuleDto = routeRuleInfoService.get(id);
        if (null == routeRuleDto) {
            return apiReturn(CommonErrorCode.NO_SUCH_ROUTE_RULE);
        }
        Map<String, Object> result = new HashMap<>(BaseConst.DEFAULT_MAP_SIZE);
        result.put("RouteRule", routeRuleDto);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribeRouteRuleList"}, method = RequestMethod.GET)
    public Object routeRuleList(@Validated RouteRuleQueryDto routeRuleQueryDto) {
        logger.info("分页查询路由规则，query:{}}", JSONObject.toJSONString(routeRuleQueryDto));
        //查询参数校验
        routeRuleQueryDto.setProjectId(ProjectTraceHolder.getProId());
        Page<RouteRuleInfoPO> pageResult = routeRuleInfoService.getRouteRulePage(routeRuleQueryDto);
        Map<String, Object> result = Maps.newHashMap();
        result.put(TOTAL_COUNT, pageResult.getTotal());
        result.put("RouteRuleList", pageResult.getRecords().stream().map(o -> routeRuleInfoService.toView(o)).collect(Collectors.toList()));
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "DeleteRouteRule", description = "删除路由规则")
    @RequestMapping(params = {"Action=DeleteRouteRule"}, method = RequestMethod.GET)
    public Object deleteRule(@Min(1) @RequestParam(value = "RouteRuleId") long id) {
        logger.info("根据路由规则id:{}，删除路由规则", id);
        RouteRuleDto routeRuleDto = routeRuleInfoService.get(id);
        if (routeRuleDto != null) {
            ErrorCode errorCode = routeRuleInfoService.checkDeleteParam(routeRuleDto);
            if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
                return apiReturn(errorCode);
            }
            routeRuleInfoService.delete(routeRuleDto);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }
}
