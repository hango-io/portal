package org.hango.cloud.common.infra.routeproxy.controller;

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
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.operationaudit.annotation.Audit;
import org.hango.cloud.common.infra.route.dto.CopyRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleQueryDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.routeproxy.dto.RouteMirrorDto;
import org.hango.cloud.common.infra.routeproxy.dto.RouteProxySyncDto;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.dto.SyncRouteRuleGwDto;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyPO;
import org.hango.cloud.common.infra.routeproxy.service.ICopyRouteRuleProxy;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.routeproxy.service.ISyncRouteProxyService;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 路由规则发布管理Controller
 *
 * @author hzchenzhongyang 2019-09-19
 */
@Validated
@RestController
@RequestMapping(value = {BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
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

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @GetMapping(params = {"Action=DescribeGatewayForPublishedRule"})
    public Object describeGatewayForPublishedRule(@RequestParam(value = "RuleId") long ruleId) {
        List<VirtualGatewayDto> virtualGatewayDtos = virtualGatewayInfoService.getPublishedServiceGateway(ruleId);
        Map<String, Object> result = Maps.newHashMap();
        result.put("GatewayInfos", virtualGatewayDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "PublishRouteRule", description = "发布路由规则")
    @RequestMapping(params = {"Action=PublishRouteRule"}, method = RequestMethod.POST)
    public String publishRouteRule(@Valid @RequestBody RouteRuleProxyDto routeRulePublishDto) {
        logger.info("发布路由规则, publishRouteRuleDto:{}", routeRulePublishDto);
        ErrorCode result = routeRuleProxyService.fillRouteRuleProxy(routeRulePublishDto);
        if (!CommonErrorCode.SUCCESS.equals(result)) {
            return apiReturn(result);
        }
        result = routeRuleProxyService.checkCreateParam(routeRulePublishDto);
        if (!CommonErrorCode.SUCCESS.equals(result)) {
            return apiReturn(result);
        }

        long routeRuleProxyId = routeRuleProxyService.create(routeRulePublishDto);
        return apiReturn(new Result(routeRuleProxyId));

    }


    @MethodReentrantLock
    @Audit(eventName = "PublishRouteMirror", description = "发布路由流量镜像规则")
    @RequestMapping(params = {"Action=PublishRouteMirror"}, method = RequestMethod.POST)
    public String publishRouteMirror(@Valid @RequestBody RouteMirrorDto routeMirrorDto) {
        logger.info("发布路由流量镜像规则, routeMirrorDto:{}", routeMirrorDto);
        //流量镜像校验
        ErrorCode checkResult = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        long id = routeRuleProxyService.publishMirrorTraffic(routeMirrorDto);
        if (id == BaseConst.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }


    @RequestMapping(params = {"Action=DescribePublishRouteRuleList"}, method = RequestMethod.GET)
    public String getPublishRouteRuleList(@Validated RouteRuleQueryDto routeRuleQueryDto) {
        logger.info("分页查询已发布路由规则列表, routeRuleQueryDto：{}", JSONObject.toJSONString(routeRuleQueryDto));
        //查询参数校验
        routeRuleQueryDto.setProjectId(ProjectTraceHolder.getProId());
        Page<RouteRuleProxyPO> page = routeRuleProxyService.getRouteRuleProxyPage(routeRuleQueryDto);
        Map<String, Object> result = Maps.newHashMap();
        result.put(TOTAL_COUNT, page.getTotal());
        if (page.getTotal() > 0){
            List<RouteRuleProxyDto> routeRuleProxyDtos = page.getRecords().stream().map(routeRuleProxyService::toView).collect(Collectors.toList());
            result.put(BaseConst.ROUTE_RULE_PROXY_LIST, routeRuleProxyDtos);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribePublishRouteRuleListByService"}, method = RequestMethod.GET)
    public String getPublishRouteRuleList(@RequestParam(value = "VirtualGwId") long virtualGwId,
                                          @RequestParam(value = "ServiceName") String serviceName) {
        ServiceDto serviceInfo = serviceInfoService.describeDisplayName(serviceName, ProjectTraceHolder.getProId());
        if (serviceInfo == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        RouteRuleQuery queryDto = RouteRuleQuery.builder().virtualGwId(virtualGwId).serviceId(serviceInfo.getId()).build();
        List<RouteRuleProxyDto> routeRuleProxyList = routeRuleProxyService.getRouteRuleProxyList(queryDto);
        Map<String, Object> result = Maps.newHashMap();
        result.put(BaseConst.ROUTE_RULE_PROXY_LIST, routeRuleProxyList);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @RequestMapping(params = {"Action=DescribePublishRouteRuleById"}, method = RequestMethod.GET)
    public String describePublishRouteRuleById(@RequestParam(value = "Id") long id) {
        logger.info("根据路由规则id:{},查询路由规则", id);
        Map<String, Object> result = Maps.newHashMap();
        RouteRuleProxyDto routeRuleProxy = routeRuleProxyService.get(id);
        result.put("RouteRuleProxy", routeRuleProxy);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }


    @RequestMapping(params = {"Action=DescribePublishedRouteRule"}, method = RequestMethod.GET)
    public String describePublishedRouteRule(@Min(1) @RequestParam(value = "VirtualGwId") long virtualGwId,
                                               @Min(1) @RequestParam(value = "RouteRuleId") long routeRuleId) {
        logger.info("根据路由规则id:{},网关id查询路由规则:{}", routeRuleId, virtualGwId);
        Map<String, Object> result = Maps.newHashMap();
        RouteRuleProxyDto routeRuleProxyDto = routeRuleProxyService.getRouteRuleProxy(virtualGwId, routeRuleId);
        result.put("RouteRuleProxy", routeRuleProxyDto);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }


    @RequestMapping(params = {"Action=DescribePublishRouteRuleByRouteRuleId"}, method = RequestMethod.GET)
    public String describePublishRouteRule(@RequestParam(value = "RouteRuleId") long routeRuleId) {
        logger.info("根据路由规则routeRuleId:{},查询路由规则", routeRuleId);
        List<RouteRuleProxyDto> routeRuleProxyInfos = routeRuleProxyService.getRouteRuleProxyByRouteRuleId(routeRuleId);
        Map<String, Object> result = Maps.newHashMap();
        result.put(BaseConst.ROUTE_RULE_PROXY_LIST, routeRuleProxyInfos);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "DeletePublishedRouteRule", description = "下线路由规则")
    @RequestMapping(params = {"Action=DeletePublishedRouteRule"}, method = RequestMethod.GET)
    public String deletePublishedRouteRule(@Min(1) @RequestParam(value = "VirtualGwId") long virtualGwId,
                                           @Min(1) @RequestParam(value = "RouteRuleId") long routeRuleId,
                                           @RequestParam(value = "ServiceIds", required = false) List<Long> serviceIds) {
        logger.info("根据网关id virtualGwId:{},路由规则id:{}下线路由规则,下线serviceId:{}", virtualGwId, routeRuleId, serviceIds);

        //参数校验
        RouteRuleProxyDto routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(virtualGwId, routeRuleId);
        if (routeRuleProxy == null) {
            logger.info("下线路由规则，路由规则未发布");
            return apiReturn(CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED);
        }
        routeRuleProxy.setExtension(serviceIds);
        ErrorCode checkResult = routeRuleProxyService.checkDeleteParam(routeRuleProxy);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        routeRuleProxyService.delete(routeRuleProxy);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdateRouteRuleEnableState", description = "更新发布路由规则状态")
    @RequestMapping(params = {"Action=UpdateRouteRuleEnableState"}, method = RequestMethod.GET)
    public Object updateRouteRuleEnableState(@Min(1) @RequestParam(value = "RouteRuleId") long routeRuleId,
                                             @Min(1) @RequestParam(value = "VirtualGwId") long virtualGwId,
                                             @RequestParam(value = "EnableState", defaultValue = "enable") String enableState) {
        logger.info("根据路由id：{},网关id：{}, 使能状态:{} 更新路由发布信息", routeRuleId, virtualGwId, enableState);
        //操作审计记录资源名称
        RouteRuleProxyDto routeRuleProxyDto = routeRuleProxyService.getRouteRuleProxy(virtualGwId, routeRuleId);
        routeRuleProxyDto.setEnableState(enableState);
        ErrorCode errorCode = routeRuleProxyService.checkUpdateParam(routeRuleProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        long id = routeRuleProxyService.update(routeRuleProxyDto);
        if (id == BaseConst.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @MethodReentrantLock
    @RequestMapping(params = {"Action=UpdateRouteRuleProxy"}, method = RequestMethod.POST)
    public String updateRouteRuleProxy(@Valid @RequestBody RouteRuleProxyDto routeRulePublishDto) {
        logger.info("更新路由规则, publishRouteRuleDto:{}", routeRulePublishDto);
        ErrorCode checkResult = routeRuleProxyService.checkUpdateParam(routeRulePublishDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }

        long routeRuleProxyId = routeRuleProxyService.update(routeRulePublishDto);
        if (BaseConst.ERROR_RESULT == routeRuleProxyId) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> result = Maps.newHashMap();
        result.put("Id", routeRuleProxyId);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @RequestMapping(params = {"Action=CopyRouteRuleProxy"}, method = RequestMethod.GET)
    public Object copyRouteRuleProxy(@Min(1) @RequestParam(value = "RouteRuleId") long routeRuleId,
                                     @Min(1) @RequestParam(value = "OriginGwId") long originGwId,
                                     @Min(1) @RequestParam(value = "DesGwId") long desGwId) {
        logger.info("一键发布已发布路由,路由id:{}, 源网关originGwId:{},目标网关desGwId:{}", new Object[]{routeRuleId, originGwId, desGwId});
        ErrorCode checkResult = copyRouteRuleProxy.checkCopyRouteRuleProxy(routeRuleId, originGwId, desGwId);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        boolean result = copyRouteRuleProxy.copyRouteRuleProxy(routeRuleId, originGwId, desGwId);
        if (!result) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @MethodReentrantLock
    @Audit(eventName = "SyncRouteProxy", description = "同步路由基本信息")
    @RequestMapping(params = {"Action=SyncRouteProxy"}, method = RequestMethod.POST)
    public String SyncRouteProxy(@Valid @RequestBody RouteProxySyncDto routeProxySyncDto) {
        logger.info("同步路由基本信息, routeRulePublishDto:{}", routeProxySyncDto);
        ErrorCode checkResult = syncRouteProxyService.checkSyncRouteProxy(routeProxySyncDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        List<String> errorGwName = syncRouteProxyService.syncRouteProxy(routeProxySyncDto);
        if (CollectionUtils.isEmpty(errorGwName)) {
            return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
        }
        return apiReturn(CommonErrorCode.batchPublishRouteError(errorGwName.toString()));
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
        return apiReturn(new Result(syncRouteRuleGwDtos));
    }


    @RequestMapping(params = {"Action=CopyRouteRule"}, method = RequestMethod.POST)
    public Object copyRouteRule(@Validated @RequestBody CopyRuleDto copyRuleDto) {
        logger.info("复制路由规则,copyRuleDto:{}", copyRuleDto);
        ErrorCode errorCode = routeRuleInfoService.checkCopyParam(copyRuleDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        long id = routeRuleInfoService.copyRouteRule(copyRuleDto);
        Map<String, Object> result = Maps.newHashMap();
        result.put("RouteRuleId", id);
        return apiReturn(HttpStatus.SC_CREATED, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }


}
