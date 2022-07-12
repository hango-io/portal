package org.hango.cloud.dashboard.apiserver.web.controller.route;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ActionInfoHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.audit.meta.AuditMetaData;
import org.hango.cloud.dashboard.audit.service.IAuditConfigService;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyCopyRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 路由规则管理Controller层
 *
 * @author hzchenzhongyang 2019-09-11
 */
@RestController
@Validated
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class RouteRuleInfoController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(RouteRuleInfoController.class);

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IAuditConfigService auditConfigService;

    @MethodReentrantLock
    @Audit(eventName = "CreateRouteRule", description = "创建路由规则")
    @RequestMapping(params = {"Action=CreateRouteRule"}, method = RequestMethod.POST)
    public String createRouteRule(@Validated @RequestBody RouteRuleDto routeRuleDto) {
        logger.info("创建路由规则，routeRuleDto:{}", routeRuleDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, null, routeRuleDto.getRouteRuleName());
        AuditResourceHolder.set(resource);

        //配置审计,创建路由规则
        auditConfigService.record(new AuditMetaData(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                ActionInfoHolder.getAction(), JSONObject.parseObject(JSON.toJSONString(routeRuleDto))));
        ErrorCode checkResult = routeRuleInfoService.checkAddParam(routeRuleDto);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        RouteRuleInfo routeRuleInfo = routeRuleDto.toMeta();
        routeRuleInfo.setProjectId(ProjectTraceHolder.getProId());
        if (routeRuleInfoService.isSameRouteRuleInfo(routeRuleInfo)) {
            logger.info("创建路由规则，参数完全相同，不允许创建");
            return apiReturn(CommonErrorCode.SameParamRouteRuleExist);
        }
        long id = routeRuleInfoService.addRouteRule(routeRuleInfo);
        if (-1 == id) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }

        resource.setResourceId(id);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("RouteRuleId", id);
        return apiReturn(HttpStatus.SC_CREATED, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdateRouteRule", description = "更新路由规则")
    @RequestMapping(params = {"Action=UpdateRouteRule"}, method = RequestMethod.POST)
    public String updateRouteRule(@Validated @RequestBody RouteRuleDto routeRuleDto) {
        logger.info("更新路由规则，routeRuleDto", routeRuleDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, routeRuleDto.getId(), routeRuleDto.getRouteRuleName());
        AuditResourceHolder.set(resource);

        ErrorCode checkResult = routeRuleInfoService.checkUpdateParam(routeRuleDto);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        RouteRuleInfo routeRuleInfo = routeRuleDto.toMeta();
        routeRuleInfo.setProjectId(ProjectTraceHolder.getProId());
        if (routeRuleInfoService.isSameRouteRuleInfo(routeRuleInfo)) {
            logger.info("修改路由规则，存在参数完全相同路由规则，不允许修改");
            return apiReturn(CommonErrorCode.SameParamRouteRuleExist);
        }
        routeRuleInfo.setPublishStatus(routeRuleInfoService.getRouteRuleInfoById(routeRuleDto.getId()).getPublishStatus());
        boolean updateSuccess = routeRuleInfoService.updateRouteRule(routeRuleInfo);
        if (!updateSuccess) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @RequestMapping(params = {"Action=DescribeRouteRule"}, method = RequestMethod.GET)
    public String getRouteRule(@Min(1) @RequestParam(value = "RouteRuleId") long id) {
        logger.info("根据路由规则id查询路由规则详情，id:{}", id);
        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(id);
        if (null == routeRuleInfo) {
            return apiReturn(CommonErrorCode.NoSuchRouteRule);
        }
        RouteRuleDto routeRuleDto = routeRuleInfoService.fromMeta(routeRuleInfo);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("RouteRule", routeRuleDto);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribeRouteRuleList"}, method = RequestMethod.GET)
    public Object routeRuleList(@RequestParam(value = "Pattern", required = false) String pattern,
                                @RequestParam(value = "ServiceId", required = false, defaultValue = "0") int serviceId,
                                @RequestParam(value = "PublishStatus", required = false, defaultValue = "-1") int publishStatus,
                                @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                                @RequestParam(value = "SortByKey", required = false) String sortKey,
                                @RequestParam(value = "SortByValue", required = false) String sortValue) {
        logger.info("分页查询路由规则，pattern:{}, serviceId:{}", pattern, serviceId);
        //查询参数校验
        ErrorCode errorCode = routeRuleInfoService.checkDescribeParam(sortKey, sortValue, offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        List<RouteRuleInfo> routeRuleList = routeRuleInfoService.getRouteRuleInfoByPattern(pattern, publishStatus,
                serviceId, ProjectTraceHolder.getProId(), sortKey, sortValue, offset, limit);

        List<RouteRuleDto> envoyServiceInfoDtos = routeRuleList.stream().map(routeRuleInfoService::fromMeta).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put(TOTAL_COUNT, routeRuleInfoService.getRouteRuleInfoCount(pattern, publishStatus, serviceId, ProjectTraceHolder.getProId()));
        result.put("RouteRuleList", envoyServiceInfoDtos);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "DeleteRouteRule", description = "删除路由规则")
    @RequestMapping(params = {"Action=DeleteRouteRule"}, method = RequestMethod.GET)
    public Object deleteRule(@Min(1) @RequestParam(value = "RouteRuleId") long id) {
        logger.info("根据路由规则id:{}，删除路由规则", id);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, id, null);
        AuditResourceHolder.set(resource);

        ErrorCode errorCode = routeRuleInfoService.checkDeleteParam(id);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        routeRuleInfoService.deleteRouteRule(id);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @RequestMapping(params = {"Action=CopyRouteRule"}, method = RequestMethod.POST)
    public Object copyRouteRule(@Validated @RequestBody EnvoyCopyRuleDto copyRuleDto) {
        logger.info("复制路由规则,copyRuleDto:{}", copyRuleDto);
        ErrorCode errorCode = routeRuleInfoService.checkCopyParam(copyRuleDto);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        long id = routeRuleInfoService.copyRouteRule(copyRuleDto);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("RouteRuleId", id);
        return apiReturn(HttpStatus.SC_CREATED, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

}
