package org.hango.cloud.dashboard.envoy.web.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.controller.apimanage.ApiBasicInfoController;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IAuthPermissionService;
import org.hango.cloud.dashboard.envoy.web.dto.auth.AuthPermissionDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.AuthPermissionListDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.RouteAuthPermissionDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.ServiceAuthPermissionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
@Validated
public class AuthPermissionController extends AbstractController {
    private static Logger logger = LoggerFactory.getLogger(ApiBasicInfoController.class);

    @Autowired
    private IAuthPermissionService authPermissonService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateAuthPermission"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateAuthPermission", description = "添加授权")
    public Object createAuthPermission(@Validated @RequestBody AuthPermissionDto authPermissionDto) {
        logger.info("创建授权信息:{}", authPermissionDto);
        ErrorCode errorCode = authPermissonService.checkCreateAuthPermission(authPermissionDto);
        //参数校验
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        Map<String, String> authPermissionResult = authPermissonService.createAuthPermission(authPermissionDto);
        boolean flag = Boolean.parseBoolean(authPermissionResult.get("flag"));
        if (!flag) {
            if (StringUtils.isBlank(authPermissionResult.get("result"))) {
                return apiReturn(CommonErrorCode.InternalServerError);
            }
            JSONObject jsonObject = JSONObject.parseObject(authPermissionResult.get("result"));
            return apiReturn(Integer.parseInt(authPermissionResult.get("statusCode")), jsonObject.getString("Code"),
                    jsonObject.getString("Message"), null);
        }
        return apiReturn(CommonErrorCode.Success);
    }

    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteAuthPermission"}, method = RequestMethod.GET)
    @Audit(eventName = "DeleteAuthPermission", description = "删除授权")
    public Object deleteAuthPermission(@RequestParam(value = "AuthPermissionId") @Min(1) long authPermissionId,
                                       @RequestParam(value = "GwId") @Min(1) long gwId) {
        logger.info("删除授权信息 authPermissionId:{}", authPermissionId);
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.info("删除授权，网关不存在");
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        boolean result = authPermissonService.deleteAuthPermission(gwId, authPermissionId);
        if (!result) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(CommonErrorCode.Success);
    }

    @RequestMapping(params = {"Action=DescribeAuthPermission"}, method = RequestMethod.GET)
    @Audit(eventName = "DescribeAuthPermission", description = "查询授权")
    public Object describeAuthPermission(@RequestParam(value = "AuthAccountId", required = false, defaultValue = "0") long authAccountId,
                                         @RequestParam(value = "AuthorizationObjectId", required = false, defaultValue = "0") long authorizationObjectId,
                                         @RequestParam(value = "AuthorizationObjectType", required = false, defaultValue = "") String authorizationObjectType,
                                         @RequestParam(value = "GwId") @Min(1) long gwId,
                                         @RequestParam(value = "Offset", required = false, defaultValue = "0") @Min(0) long offset,
                                         @RequestParam(value = "Limit", required = false, defaultValue = "20") @Max(1000) long limit) {
        logger.info("查询授权信息，gwId:{},authAccountId:{}, authorizationObjectType:{}, authorizationObjectId:{}",
                new Object[]{gwId, authAccountId, authorizationObjectType, authorizationObjectId});
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.info("查询授权，网关不存在");
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        AuthPermissionListDto authPermissionListDto = authPermissonService.describeAuthPermission(gwId, authAccountId,
                authorizationObjectId, authorizationObjectType, offset, limit);
        if (authPermissionListDto == null) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("TotalCount", authPermissionListDto.getTotalCount());
        result.put("AuthPermissionList", authPermissionListDto.getAuthPermissionDtoList());
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @RequestMapping(params = {"Action=DescribeServiceAuthList"}, method = RequestMethod.GET)
    public Object describeServiceAuthList(@RequestParam(value = "GwId") @Min(1) long gwId,
                                          @RequestParam(value = "ServiceId", required = false,
                                                  defaultValue = "0") long serviceId,
                                          @RequestParam(value = "Offset", required = false,
                                                  defaultValue = "0") long offset,
                                          @RequestParam(value = "Limit", required = false,
                                                  defaultValue = "20") long limit,
                                          @RequestParam(value = "Authorization", required = false,
                                                  defaultValue = "false") boolean auth) {
        logger.info("查询服务授权相关信息，GwId:{}", gwId);
        //offset,limit校验
        ErrorCode errorCode = CommonUtil.checkOffsetAndLimit(offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        List<ServiceProxyInfo> serviceProxy;
        long projectId = ProjectTraceHolder.getProId();
        long count;
        if (!auth) {
            serviceProxy = serviceProxyService.getEnvoyServiceProxy(gwId, serviceId, projectId, offset,
                    limit);
            count = serviceProxyService.getServiceProxyCount(gwId, serviceId);
        } else {
            serviceProxy = serviceProxyService.getAuthServiceProxyByLimit(gwId, serviceId, projectId, offset,
                    limit);
            count = serviceProxyService.getAuthServiceProxyCount(gwId, serviceId);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        if (CollectionUtils.isEmpty(serviceProxy)) {
            return apiReturn(HttpStatus.SC_OK, null, null, result);
        }
        result.put(TOTAL_COUNT, count);
        List<String> serviceTags = serviceProxy.stream().map(
                item -> serviceInfoService.getServiceByServiceId(item.getServiceId()).getServiceName()).collect(
                Collectors.toList());
        List<ServiceAuthPermissionDto> serviceAuthList = authPermissonService.describeServiceAuthList(gwId,
                serviceTags);
        result.put("ServiceAuthList", serviceAuthList);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @RequestMapping(params = {"Action=DescribeRouteAuthList"}, method = RequestMethod.GET)
    public Object describeRouteAuthList(@RequestParam(value = "GwId") @Min(1) long gwId,
                                        @RequestParam(value = "ServiceId", required = false,
                                                defaultValue = "0") long serviceId,
                                        @RequestParam(value = "RouteRuleId", required = false,
                                                defaultValue = "0") long routeRuleId,
                                        @RequestParam(value = "Offset", required = false,
                                                defaultValue = "0") long offset,
                                        @RequestParam(value = "Limit", required = false,
                                                defaultValue = "20") long limit,
                                        @RequestParam(value = "Authorization", required = false,
                                                defaultValue = "false") boolean auth) {
        logger.info("查询路由授权相关信息，GwId:{}", gwId);
        //offset,limit校验
        ErrorCode errorCode = CommonUtil.checkOffsetAndLimit(offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        List<RouteRuleProxyInfo> routeRuleProxyList = routeRuleProxyService.getAuthRouteProxy(gwId, serviceId,
                routeRuleId, auth,
                offset, limit);
        long totalCount = routeRuleProxyService.getAuthRouteCount(gwId, serviceId, routeRuleId, auth);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        if (CollectionUtils.isEmpty(routeRuleProxyList)) {
            return apiReturn(HttpStatus.SC_OK, null, null, result);
        }
        result.put(TOTAL_COUNT, totalCount);
        List<Long> routeIdList = routeRuleProxyList.stream().map(item -> item.getRouteRuleId()).collect(
                Collectors.toList());
        List<RouteAuthPermissionDto> routeAuthList = authPermissonService.describeRouteAuthList(gwId, routeIdList);
        result.put("RouteAuthList", routeAuthList);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }
}
