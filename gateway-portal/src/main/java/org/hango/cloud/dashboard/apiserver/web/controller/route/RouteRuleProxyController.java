package org.hango.cloud.dashboard.apiserver.web.controller.route;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.ICopyRouteRuleProxy;
import org.hango.cloud.dashboard.envoy.service.ISyncRouteProxyService;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.SyncRouteRuleGwDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 路由规则发布管理Controller
 *
 * @author hzchenzhongyang 2019-09-19
 */
@Validated
@RestController
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class RouteRuleProxyController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(RouteRuleProxyController.class);

    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;
    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private ICopyRouteRuleProxy copyRouteRuleProxy;
    @Autowired
    private ISyncRouteProxyService syncRouteProxyService;


    @GetMapping(params = {"Action=DescribeGatewayForPublishedRule"})
    public Object describeGatewayForPublishedRule(@RequestParam(value = "RuleId") long ruleId) {
        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(ruleId);
        if (routeRuleInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchRouteRule);
        }
        List<GatewayDto> gatewayDtos = serviceProxyService.getPublishedServiceGateway(routeRuleInfo.getServiceId());
        Map<String, Object> result = new HashMap<>();
        result.put("GatewayInfos", gatewayDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "PublishRouteRule", description = "发布路由规则")
    @RequestMapping(params = {"Action=PublishRouteRule"}, method = RequestMethod.POST)
    public String publishRouteRule(@Valid @RequestBody RouteRuleProxyDto routeRulePublishDto) {
        logger.info("发布路由规则, publishRouteRuleDto:{}", routeRulePublishDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, routeRulePublishDto.getRouteRuleId(), routeRulePublishDto.getRouteRuleName());
        AuditResourceHolder.set(resource);
        ErrorCode checkResult = routeRuleProxyService.checkPublishParam(routeRulePublishDto);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        if (CollectionUtils.isEmpty(routeRulePublishDto.getGwIds())) {
            RouteRuleProxyInfo routeRuleProxyInfo = routeRuleProxyService.toMeta(routeRulePublishDto);
            if (routeRuleProxyService.isSameRouteRuleProxyInfo(routeRuleProxyInfo)) {
                logger.error("更新路由规则，参数完全相同，不允许更新");
                return apiReturn(CommonErrorCode.SameParamRouteRuleExist);
            }
            long routeRuleProxyId = routeRuleProxyService.publishRouteRule(routeRuleProxyService.toMeta(routeRulePublishDto), Lists.newArrayList(), true);
            if (Const.ERROR_RESULT == routeRuleProxyId) {
                return apiReturn(CommonErrorCode.InternalServerError);
            }
            return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
        }

        List<String> errorGwName = routeRuleProxyService.publishRouteRuleBatch(routeRulePublishDto.getGwIds(), routeRulePublishDto);
        if (CollectionUtils.isEmpty(errorGwName)) {
            return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
        }
        return apiReturn(CommonErrorCode.BatchPublishRouteError(errorGwName.toString()));
    }

    @MethodReentrantLock
    @Audit(eventName = "PublishRouteMirror", description = "发布路由流量镜像规则")
    @RequestMapping(params = {"Action=PublishRouteMirror"}, method = RequestMethod.POST)
    public String publishRouteMirror(@Valid @RequestBody RouteRuleProxyDto routeRulePublishDto) {
        logger.info("发布路由流量镜像规则, publishRouteRuleDto:{}", routeRulePublishDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, routeRulePublishDto.getRouteRuleId(), null);
        AuditResourceHolder.set(resource);

        //流量镜像校验
        ErrorCode checkResult = routeRuleProxyService.checkPublishMirror(routeRulePublishDto);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        long id = routeRuleProxyService.publishRouteMirror(routeRulePublishDto);
        if (id == Const.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @RequestMapping(params = {"Action=DescribePublishRouteRuleList"}, method = RequestMethod.GET)
    public String getPublishRouteRuleList(@Min(0) @RequestParam(value = "GwId", required = false, defaultValue = "0") long gwId,
                                          @Min(0) @RequestParam(value = "ServiceId", required = false, defaultValue = "0") long serviceId,
                                          @Min(1) @Max(1000) @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                                          @Min(0) @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                          @RequestParam(value = "SortByKey", required = false) String sortKey,
                                          @RequestParam(value = "SortByValue", required = false) String sortValue,
                                          @RequestParam(value = "Pattern", required = false) String pattern) {
        logger.info("分页查询已发布路由规则列表, gwId:{}, serviceId:{}, pattern:{}, limit:{}, offset:{}", gwId, serviceId, pattern, limit, offset);
        //查询参数校验
        ErrorCode errorCode = routeRuleInfoService.checkDescribeParam(sortKey, sortValue, offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        long count = routeRuleProxyService.getRouteRuleProxyCount(gwId, serviceId, pattern);
        List<RouteRuleProxyInfo> routeRuleProxy = routeRuleProxyService.getRouteRuleProxyList(gwId, serviceId,
                pattern, sortKey,
                sortValue, offset,
                limit);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put(TOTAL_COUNT, count);
        result.put("RouteRuleProxyList", routeRuleProxy.stream().map(routeRuleProxyService::fromMeta).collect(Collectors.toList()));
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribePublishRouteRuleListByService"}, method = RequestMethod.GET)
    public String getPublishRouteRuleList(@RequestParam(value = "GwId") long gwId,
                                          @RequestParam(value = "ServiceName") String serviceName) {
        ServiceInfo serviceInfo = serviceInfoService.describeDisplayName(serviceName, ProjectTraceHolder.getProId());
        if (serviceInfo == null) {
            return apiReturn(CommonErrorCode.ServiceNotFound);
        }
        List<RouteRuleProxyInfo> routeRuleProxy = routeRuleProxyService.getRouteRuleProxyList(gwId,
                serviceInfo.getId(),
                StringUtils.EMPTY,
                null, null, 0, 1000);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("RouteRuleProxyList",
                routeRuleProxy.stream().map(routeRuleProxyService::fromMeta).collect(Collectors.toList()));
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribePublishRouteRuleById"}, method = RequestMethod.GET)
    public String describePublishRouteRuleById(@RequestParam(value = "Id") long id) {
        logger.info("根据路由规则id:{},查询路由规则", id);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        RouteRuleProxyInfo routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(id);
        if (routeRuleProxy != null) {
            RouteRuleProxyDto routeRuleProxyDto = routeRuleProxyService.fromMeta(routeRuleProxy);
            result.put("RouteRuleProxy", routeRuleProxyDto);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }


    @RequestMapping(params = {"Action=DescribePublishedRouteRule"}, method = RequestMethod.GET)
    public String describePublishRouteRuleById(@Min(1) @RequestParam(value = "GwId") long gwId,
                                               @Min(1) @RequestParam(value = "RouteRuleId") long routeRuleId) {
        logger.info("根据路由规则id:{},网关id查询路由规则", routeRuleId, gwId);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        RouteRuleProxyInfo routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(gwId, routeRuleId);
        if (routeRuleProxy != null) {
            RouteRuleProxyDto routeRuleProxyDto = routeRuleProxyService.fromMeta(routeRuleProxy);
            result.put("RouteRuleProxy", routeRuleProxyDto);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }


    @RequestMapping(params = {"Action=DescribePublishRouteRuleByRouteRuleId"}, method = RequestMethod.GET)
    public String describePublishRouteRule(@RequestParam(value = "RouteRuleId") long routeRuleId) {
        logger.info("根据路由规则routeRuleId:{},查询路由规则", routeRuleId);
        List<RouteRuleProxyInfo> routeRuleProxyInfos = routeRuleProxyService.getRouteRuleProxyByRouteRuleId(routeRuleId);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("RouteRuleProxyList", routeRuleProxyInfos.stream().map(routeRuleProxyService::fromMeta).collect(Collectors.toList()));
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "DeletePublishedRouteRule", description = "下线路由规则")
    @RequestMapping(params = {"Action=DeletePublishedRouteRule"}, method = RequestMethod.GET)
    public String deletePublishedRouteRule(@Min(1) @RequestParam(value = "GwId") long gwId,
                                           @Min(1) @RequestParam(value = "RouteRuleId") long routeRuleId,
                                           @RequestParam(value = "ServiceIds", required = false) List<Long> serviceIds) {
        logger.info("根据网关id gwId:{},路由规则id:{}下线路由规则,下线serviceId:{}", gwId, routeRuleId, serviceIds);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, routeRuleId, null);
        AuditResourceHolder.set(resource);

        //参数校验
        ErrorCode checkResult = routeRuleProxyService.checkDeleteRouteRuleProxy(gwId, routeRuleId, serviceIds);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        boolean deleteSuccess = routeRuleProxyService.deleteRouteRuleProxy(gwId, routeRuleId);
        if (!deleteSuccess) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdateRouteRuleEnableState", description = "更新发布路由规则状态")
    @RequestMapping(params = {"Action=UpdateRouteRuleEnableState"}, method = RequestMethod.GET)
    public Object updateRouteRuleEnableState(@Min(1) @RequestParam(value = "RouteRuleId") long routeRuleId,
                                             @Min(1) @RequestParam(value = "GwId") long gwId,
                                             @RequestParam(value = "EnableState", defaultValue = "enable") String enableState) {
        logger.info("根据路由id：{},网关id：{}, 使能状态:{} 更新路由发布信息", routeRuleId, gwId, enableState);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, routeRuleId, null);
        AuditResourceHolder.set(resource);

        ErrorCode errorCode = routeRuleProxyService.checkUpdateEnableState(gwId, routeRuleId, enableState);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        RouteRuleProxyInfo routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(gwId, routeRuleId);
        if (enableState.equals(Const.ROUTE_RULE_ENABLE_STATE) && routeRuleProxyService.isSameRouteRuleProxyInfo(routeRuleProxy)) {
            logger.error("更新路由规则，参数完全相同，不允许更新");
            return apiReturn(CommonErrorCode.SameParamRouteRuleExist);
        }
        long id = routeRuleProxyService.updateEnableState(gwId, routeRuleId, enableState);
        if (id == Const.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @MethodReentrantLock
    @RequestMapping(params = {"Action=UpdateRouteRuleProxy"}, method = RequestMethod.POST)
    public String updateRouteRuleProxy(@Valid @RequestBody RouteRuleProxyDto routeRulePublishDto) {
        logger.info("更新路由规则, publishRouteRuleDto:{}", routeRulePublishDto);
        ErrorCode checkResult = routeRuleProxyService.checkUpdateParam(routeRulePublishDto);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        RouteRuleProxyInfo routeRuleProxyInfo = routeRuleProxyService.toMeta(routeRulePublishDto);
        if (Const.ROUTE_RULE_ENABLE_STATE.equals(routeRulePublishDto.getEnableState())
                && routeRuleProxyService.isSameRouteRuleProxyInfo(routeRuleProxyInfo)) {
            logger.error("更新路由规则，参数完全相同，不允许更新");
            return apiReturn(CommonErrorCode.SameParamRouteRuleExist);
        }

        long routeRuleProxyId = routeRuleProxyService.updateEnvoyRouteRuleProxy(routeRuleProxyInfo);
        if (Const.ERROR_RESULT == routeRuleProxyId) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }

        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("Id", routeRuleProxyId);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribeServiceProxyForPublishRoute"}, method = RequestMethod.GET)
    public Object describeServiceProxy(@Min(1) @RequestParam(value = "ServiceId") long serviceId,
                                       @RequestParam(value = "GwIds", required = false) List<Long> gwIds,
                                       @RequestParam(value = "GwId", required = false, defaultValue = "0") long gwId) {
        logger.info("根据服务id：{},网关id：{}，查询服务相关端口", serviceId, gwIds);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        ServiceProxyInfo serviceProxyInDb = null;
        if (CollectionUtils.isNotEmpty(gwIds)) {
            serviceProxyInDb = serviceProxyService.getServiceProxyInterByServiceIdAndGwIds(gwIds, serviceId);
        } else if (gwId != 0) {
            serviceProxyInDb = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, serviceId);
        }
        if (serviceProxyInDb != null) {
            result.put("EnvoyServiceProxy", serviceProxyService.fromMetaWithPort(serviceProxyInDb));
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @RequestMapping(params = {"Action=CopyRouteRuleProxy"}, method = RequestMethod.GET)
    public Object copyRouteRuleProxy(@Min(1) @RequestParam(value = "RouteRuleId") long routeRuleId,
                                     @Min(1) @RequestParam(value = "OriginGwId") long originGwId,
                                     @Min(1) @RequestParam(value = "DesGwId") long desGwId) {
        logger.info("一键发布已发布路由,路由id:{}, 源网关originGwId:{},目标网关desGwId:{}", new Object[]{routeRuleId, originGwId, desGwId});
        ErrorCode checkResult = copyRouteRuleProxy.checkCopyRouteRuleProxy(routeRuleId, originGwId, desGwId);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        boolean result = copyRouteRuleProxy.copyRouteRuleProxy(routeRuleId, originGwId, desGwId);
        if (!result) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    /**
     * 根据已发布路由id和目标网关，进行已发布路由复制
     *
     * @param routeProxyId 已发布路由id
     * @param desGwId      目标网关id
     * @return 路由复制的结果
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CopyRouteRuleProxyByProxyId"}, method = RequestMethod.GET)
    public Object copyRouteRuleProxy(@Min(1) @RequestParam(value = "RouteProxyId") long routeProxyId,
                                     @Min(1) @RequestParam(value = "DesGwId") long desGwId) {
        logger.info("一键发布已发布路由,已发布路由id:{}, 目标网关desGwId:{}", routeProxyId, desGwId);
        RouteRuleProxyInfo routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(routeProxyId);
        if (!Optional.ofNullable(routeRuleProxy).isPresent()) {
            logger.info("一键复制已发布路由，已发布路由id错误， routeProxyId:{}", routeProxyId);
            return apiReturn(CommonErrorCode.RouteRuleNotPublished);
        }
        ErrorCode checkResult = copyRouteRuleProxy.checkCopyRouteRuleProxy(routeRuleProxy.getRouteRuleId(), routeRuleProxy.getGwId(), desGwId);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        boolean result = copyRouteRuleProxy.copyRouteRuleProxy(routeRuleProxy.getRouteRuleId(), routeRuleProxy.getGwId(), desGwId);
        if (!result) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(CommonErrorCode.Success);
    }

    @MethodReentrantLock
    @Audit(eventName = "SyncRouteProxy", description = "同步路由基本信息")
    @RequestMapping(params = {"Action=SyncRouteProxy"}, method = RequestMethod.POST)
    public String SyncRouteProxy(@Valid @RequestBody RouteRuleProxyDto routeRulePublishDto) {
        logger.info("同步路由基本信息, routeRulePublishDto:{}", routeRulePublishDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, routeRulePublishDto.getRouteRuleId(), routeRulePublishDto.getRouteRuleName());
        AuditResourceHolder.set(resource);
        ErrorCode checkResult = syncRouteProxyService.checkSyncRouteProxy(routeRulePublishDto);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        if (CollectionUtils.isEmpty(routeRulePublishDto.getGwIds())) {
            long routeRuleProxyId = syncRouteProxyService.syncRouteProxy(routeRulePublishDto.getGwId(), routeRulePublishDto.getRouteRuleId());
            if (Const.ERROR_RESULT == routeRuleProxyId) {
                return apiReturn(CommonErrorCode.InternalServerError);
            }
            return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
        }

        List<String> errorGwName = syncRouteProxyService.syncRouteRuleBatch(routeRulePublishDto.getGwIds(), routeRulePublishDto);
        if (CollectionUtils.isEmpty(errorGwName)) {
            return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
        }
        return apiReturn(CommonErrorCode.BatchPublishRouteError(errorGwName.toString()));
    }

    /**
     * 获取同步路由网关信息
     *
     * @param ruleId 路由id
     * @return
     */
    @GetMapping(params = {"Action=DescribeGatewayForSyncRule"})
    public Object describeGatewayForSyncRule(@RequestParam(value = "RouteRuleId") long ruleId) {
        List<SyncRouteRuleGwDto> syncRouteRuleGwDtos = syncRouteProxyService.describeGatewayForSyncRule(ruleId);
        Map<String, Object> result = new HashMap<>();
        result.put("SyncRouteRuleGwDto", syncRouteRuleGwDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }
}
