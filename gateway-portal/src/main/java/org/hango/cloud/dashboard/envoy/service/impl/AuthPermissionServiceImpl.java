package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.service.IAuthPermissionService;
import org.hango.cloud.dashboard.envoy.service.cache.AuthPermissionCacheService;
import org.hango.cloud.dashboard.envoy.web.dto.auth.AuthPermissionDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.AuthPermissionListDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.AuthPermissionObjectDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.RouteAuthPermissionDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.ServiceAuthPermissionDto;
import org.hango.cloud.dashboard.envoy.web.util.HttpCommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthPermissionServiceImpl implements IAuthPermissionService {
    public static final Logger logger = LoggerFactory.getLogger(AuthPermissionServiceImpl.class);

    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;
    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private AuthPermissionCacheService authCacheService;

    @Override
    public ErrorCode checkCreateAuthPermission(AuthPermissionDto authPermissionDto) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(authPermissionDto.getGwId());
        if (gatewayInfo == null) {
            logger.info("创建认证授权，网关不存在");
            return CommonErrorCode.NoSuchGateway;
        }
        if (!NumberUtils.isDigits(authPermissionDto.getAuthorizationObjectId())) {
            logger.info("创建认证授权，objectId不合法");
            return CommonErrorCode.InvalidParameter(authPermissionDto.getAuthorizationObjectId(), "AuthorizationObjectId");
        }
        long authorizationObjectId = Long.parseLong(authPermissionDto.getAuthorizationObjectId());
        //路由授权
        if (Const.AUTH_GATEWAY_ROUTE.equals(authPermissionDto.getAuthorizationObjectType())) {
            if (routeRuleProxyService.getRouteRuleProxyCount(gatewayInfo.getId(), authorizationObjectId) == 0) {
                logger.info("添加路由级别授权，路由未发布到对应的网关");
                return CommonErrorCode.RouteRuleNotPublished;
            }
        }
        //服务级别授权
        if (Const.AUTH_GATEWAY_SERVICE.equals(authPermissionDto.getAuthorizationObjectType())) {
            if (serviceProxyService.getServiceProxyCount(gatewayInfo.getId(), authorizationObjectId) == 0) {
                logger.info("添加服务级别授权，服务未发布到对应的网关");
                return CommonErrorCode.ServiceNotPublished;
            }
        }
        return CommonErrorCode.Success;
    }

    @Override
    public Map<String, String> createAuthPermission(AuthPermissionDto authPermissionDto) {
        Map<String, String> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        GatewayInfo gatewayInfo = gatewayInfoService.get(authPermissionDto.getGwId());
        String flag = "flag";
        result.put(flag, "true");
        if (gatewayInfo == null) {
            result.put(flag, "false");
            return result;
        }
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "CreateAuthPermission");
        params.put("Version", "2019-10-08");
        params.put("AuthAccountId", String.valueOf(authPermissionDto.getAuthAccountId()));
        params.put("AppId", gatewayInfo.getGwClusterName());
        params.put("AuthorizationObjectType", authPermissionDto.getAuthorizationObjectType());
        //service类型,授权服务标识
        if (Const.AUTH_GATEWAY_SERVICE.equals(authPermissionDto.getAuthorizationObjectType())) {
            params.put("AuthorizationObjectId", serviceInfoService.getServiceByServiceId(
                    Long.parseLong(authPermissionDto.getAuthorizationObjectId())).getServiceName());
            //service_auth_cache 缓存失效
            authCacheService.invalidAuthServiceCache(authPermissionDto.getGwId());
        } else {
            // route类型、项目类型
            params.put("AuthorizationObjectId", String.valueOf(authPermissionDto.getAuthorizationObjectId()));
            //route_auth_cache 缓存失效
            if (Const.AUTH_GATEWAY_ROUTE.equals(authPermissionDto.getAuthorizationObjectType())) {
                authCacheService.invalidAuthRouteCache(authPermissionDto.getGwId());
            }
        }
        HttpClientResponse response = HttpCommonUtil.getFromAuth(gatewayInfo.getAuthAddr() + "/auth", params, StringUtils.EMPTY, null, HttpMethod.GET.name());
        if (null == response) {
            result.put(flag, "false");
            return result;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用service-auth添加授权失败，返回http status code非2xx, httpStatuCode:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            if (HttpCommonUtil.is4XXCode(response.getStatusCode())) {
                result.put("result", response.getResponseBody());
                result.put("statusCode", String.valueOf(response.getStatusCode()));
            }
            result.put(flag, "false");
        }
        return result;
    }

    @Override
    public boolean deleteAuthPermission(long gwId, long authPermissionId) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return false;
        }
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DeleteAuthPermission");
        params.put("Version", "2019-10-08");
        params.put("AuthPermissionId", String.valueOf(authPermissionId));
        HttpClientResponse response = HttpCommonUtil.getFromAuth(gatewayInfo.getAuthAddr() + "/auth", params, StringUtils.EMPTY, null, HttpMethod.GET.name());
        if (null == response) {
            return false;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用service-auth删除授权失败，返回http status code非2xx, httpStatuCode:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        authCacheService.invalidAuthServiceCache(gwId);
        authCacheService.invalidAuthRouteCache(gwId);
        return true;
    }

    @Override
    public AuthPermissionListDto describeAuthPermission(long gwId, long authAccountId, long authorizationObjectId,
                                                        String authorizationObjectType, long offset, long limit) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return null;
        }
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DescribeAuthPermission");
        params.put("Version", "2019-10-08");
        params.put("AppId", gatewayInfo.getGwClusterName());
        if (authAccountId != 0) {
            params.put("AuthAccountId", String.valueOf(authAccountId));
        }
        //service类型,授权服务标识
        if (Const.AUTH_GATEWAY_SERVICE.equals(authorizationObjectType)) {
            params.put("AuthorizationObjectId", serviceInfoService.getServiceByServiceId(
                    authorizationObjectId).getServiceName());
            params.put("AuthorizationObjectType", authorizationObjectType);
        } else {
            params.put("AuthorizationObjectId", String.valueOf(authorizationObjectId));
            params.put("AuthorizationObjectType", authorizationObjectType);
        }
        params.put("Offset", String.valueOf(offset));
        params.put("Limit", String.valueOf(limit));
        HttpClientResponse response = HttpCommonUtil.getFromAuth(gatewayInfo.getAuthAddr() + "/auth", params, StringUtils.EMPTY, null, HttpMethod.GET.name());
        if (null == response) {
            return null;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用service-auth查询授权失败，返回http status code非2xx, httpStatusCode:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return null;
        }
        AuthPermissionListDto authPermissionListDto = JSONObject.parseObject(response.getResponseBody(), AuthPermissionListDto.class);
        //todo 如果需要全局返回，全局视角，这里需要扩展
        if (CollectionUtils.isNotEmpty(authPermissionListDto.getAuthPermissionDtoList())) {
            authPermissionListDto.getAuthPermissionDtoList().stream().forEach(authPermissionDto -> {
                authPermissionDto.setGwId(gwId);
            });
        }
        return authPermissionListDto;
    }

    @Override
    public List<ServiceAuthPermissionDto> describeServiceAuthList(long gwId,
                                                                  List<String> serviceTagList) {
        Map<String, AuthPermissionObjectDto> authServiceFromCache = authCacheService.getAuthServiceFromCache(gwId);
        List<AuthPermissionObjectDto> authPermissionObjectDtos;
        if (authServiceFromCache.isEmpty()) {
            logger.info("从缓存获取auth_route 为空");
            authPermissionObjectDtos = serviceTagList.stream().map(
                    item -> new AuthPermissionObjectDto(item, Lists.newLinkedList())).collect(Collectors.toList());
        } else {
            authPermissionObjectDtos = serviceTagList.stream().map(item -> getFromCache(item, authServiceFromCache))
                    .collect(Collectors.toList());
        }
        return authPermissionObjectDtos.stream().map(item -> getFromObjectDto(item)).collect(Collectors.toList());
    }

    @Override
    public List<RouteAuthPermissionDto> describeRouteAuthList(final long gwId, final List<Long> routeIdList) {
        List<String> routeIdStrList = Lists.transform(routeIdList, Functions.toStringFunction());
        //从缓存获取auth_route（所有已授权）
        Map<String, AuthPermissionObjectDto> authRouteFromCache = authCacheService.getAuthRouteFromCache(gwId);
        List<AuthPermissionObjectDto> authPermissionObjectDtos;
        if (authRouteFromCache.isEmpty()) {
            logger.info("从缓存获取auth_route 为空");
            authPermissionObjectDtos = routeIdStrList.stream().map(
                    item -> new AuthPermissionObjectDto(item, Lists.newLinkedList())).collect(Collectors.toList());
        } else {
            authPermissionObjectDtos = routeIdStrList.stream().map(item -> getFromCache(item, authRouteFromCache))
                    .collect(Collectors.toList());
        }
        return authPermissionObjectDtos.stream().map(item -> getRouteAuthFromObjectDto(item)).collect(
                Collectors.toList());
    }

    private AuthPermissionObjectDto getFromCache(String authorizationObjectId,
                                                 Map<String, AuthPermissionObjectDto> cache) {
        List<AuthPermissionDto> authPermissionDtos = cache.get(authorizationObjectId) == null ? Lists.newArrayList()
                :
                cache.get(authorizationObjectId).getAuthPermissionDtoList();
        return new AuthPermissionObjectDto(authorizationObjectId, authPermissionDtos);
    }

    private ServiceAuthPermissionDto getFromObjectDto(AuthPermissionObjectDto authPermissionObjectDto) {
        ServiceInfo serviceInfo =
                serviceInfoService.getServiceByServiceName(authPermissionObjectDto.getAuthorizationObjectId());
        if (serviceInfo == null) {
            logger.error("获取服务授权关系，存在脏数据，service_name:{}", authPermissionObjectDto.getAuthorizationObjectId());
            return null;
        }
        return new ServiceAuthPermissionDto(serviceInfo.getId(), serviceInfo.getDisplayName(),
                serviceInfo.getServiceName(),
                authPermissionObjectDto.getAuthPermissionDtoList());
    }

    private RouteAuthPermissionDto getRouteAuthFromObjectDto(AuthPermissionObjectDto authPermissionObjectDto) {
        RouteRuleInfo routeRuleInfo =
                routeRuleInfoService.getRouteRuleInfoById(Long.parseLong(authPermissionObjectDto.getAuthorizationObjectId()));
        if (routeRuleInfo == null) {
            logger.error("获取路由授权关系，存在脏数据，routeId:{}", authPermissionObjectDto.getAuthorizationObjectId());
            return null;
        }
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(routeRuleInfo.getServiceId());
        return new RouteAuthPermissionDto(routeRuleInfo.getId(), routeRuleInfo.getRouteRuleName(),
                routeRuleInfo.getServiceId(), serviceInfo.getDisplayName(),
                authPermissionObjectDto.getAuthPermissionDtoList());
    }

    @Override
    public List<AuthPermissionObjectDto> describeAuthPermissionList(long gwId,
                                                                    List<String> authorizationObjectList,
                                                                    String authorizationObjectType) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return null;
        }
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DescribeAuthPermissionList");
        params.put("Version", "2019-10-08");
        JSONObject body = new JSONObject();
        body.put("AppId", gatewayInfo.getGwClusterName());
        if (!CollectionUtils.isEmpty(authorizationObjectList)) {
            body.put("AuthorizationObjectList", authorizationObjectList);
        }
        body.put("AuthorizationObjectType", authorizationObjectType);

        Map<String, String> headers = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        headers.put("Content-type", Const.DEFAULT_CONTENT_TYPE);
        HttpClientResponse response = HttpCommonUtil.getFromAuth(gatewayInfo.getAuthAddr() + "/auth", params,
                body.toJSONString(), headers, HttpMethod.POST.name());
        if (null == response) {
            return null;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用service-auth查询授权失败，返回http status code非2xx, httpStatusCode:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return null;
        }
        JSONArray authPermissionArray = JSON.parseObject(response.getResponseBody()).getJSONArray(
                "AuthPermissionObjectList");
        List<AuthPermissionObjectDto> authPermissionObjectDtoList = JSON.parseArray(authPermissionArray.toString(),
                AuthPermissionObjectDto.class);
        if (CollectionUtils.isEmpty(authPermissionObjectDtoList)) {
            return Lists.newArrayList();
        }
        authPermissionObjectDtoList.stream().forEach(authPermissionObjectDto -> {
            authPermissionObjectDto.getAuthPermissionDtoList().stream().forEach(authPermissionDto -> {
                authPermissionDto.setGwId(gwId);
            });
        });
        return authPermissionObjectDtoList;
    }

    @Override
    public List<Long> getServiceAuthId(final long gwId) {
        Map<String, AuthPermissionObjectDto> authServiceFromCache = authCacheService.getAuthServiceFromCache(gwId);
        if (authServiceFromCache.isEmpty()) {
            return Lists.newArrayList();
        }
        List<Long> authServiceIdList = Lists.newArrayList();
        // todo 脏数据，serviceInfo get null
        authServiceFromCache.entrySet().forEach(
                entry -> authServiceIdList.add(serviceInfoService.getServiceByServiceName(entry.getKey()).getId()));
        return authServiceIdList;
    }

    @Override
    public List<Long> getRouteAuthId(final long gwId) {
        Map<String, AuthPermissionObjectDto> authServiceFromCache = authCacheService.getAuthRouteFromCache(gwId);
        if (authServiceFromCache.isEmpty()) {
            return Lists.newArrayList();
        }
        return authServiceFromCache.keySet().stream().map(item -> Long.parseLong(item)).collect(Collectors.toList());
    }

    @Override
    public boolean deleteAuthPermission(long gwId, long authAccountId, long authorizationObjectId, String authorizationObjectType) {
        AuthPermissionListDto authPermissionListDto = describeAuthPermission(gwId, authAccountId, authorizationObjectId, authorizationObjectType, 0, 1000);
        if (authPermissionListDto == null || CollectionUtils.isEmpty(authPermissionListDto.getAuthPermissionDtoList())) {
            logger.info("删除授权，当前资源不存在授权关系");
            return true;
        }
        List<AuthPermissionDto> authFailedList = authPermissionListDto.getAuthPermissionDtoList().stream().
                filter(item -> !deleteAuthPermission(item.getGwId(), item.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(authFailedList)) {
            logger.info("删除授权关系，删除异常，未删除的授权认证name:{}", authFailedList.stream().map(AuthPermissionDto::getAuthAccountName)
                    .collect(Collectors.toList()).toString());
            return false;
        }
        return true;
    }

}
