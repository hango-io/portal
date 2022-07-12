package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.cloud.ncegdashboard.envoy.web.dto.PublishResultDto;
import com.netease.cloud.ncegdashboard.envoy.web.dto.RePublishPluginDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyPluginBindingInfoDao;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyPluginTemplateDao;
import org.hango.cloud.dashboard.envoy.dao.IRouteRuleProxyDao;
import org.hango.cloud.dashboard.envoy.meta.*;
import org.hango.cloud.dashboard.envoy.service.IEnvoyDateFixService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

/**
 * 网关数据修复相关接口Service层
 *
 * @author hzchenzhongyang 2020-01-19
 */
@Service
public class EnvoyDataFixServiceImpl implements IEnvoyDateFixService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyDataFixServiceImpl.class);

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;

    @Autowired
    private IRouteRuleProxyDao routeRuleProxyDao;

    @Autowired
    private IEnvoyPluginTemplateDao envoyPluginTemplateDao;

    @Autowired
    private IEnvoyPluginBindingInfoDao envoyPluginBindingInfoDao;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @Autowired
    private EnvoyPluginServiceImpl envoyPluginServiceImpl;

    @Override
    public ErrorCode checkRePublishServiceParam(long gwId, boolean rePublishAllService, List<Long> serviceIdList) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (null == gatewayInfo) {
            logger.info("指定的网关不存在! gwId:{}", gwId);
            return CommonErrorCode.NoSuchGateway;
        }

        if (rePublishAllService) {
            return CommonErrorCode.Success;
        }

        if (CollectionUtils.isEmpty(serviceIdList)) {
            logger.info("参数ServiceIdList指定的服务列表为空！");
            return CommonErrorCode.MissingParameter("ServiceIdList");
        }

        return checkServiceIdList(gwId, serviceIdList);
    }

    private ErrorCode checkServiceIdList(long gwId, List<Long> serviceIdList) {
        List<Long> errorServiceIdList = serviceIdList.stream().filter(item -> {
            if (null == item) {
                return false;
            }
            ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, item);
            return null == serviceProxyInfo;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorServiceIdList)) {
            logger.info("服务尚未发布到指定网关！ errorServiceIdList:{}", errorServiceIdList);
            return CommonErrorCode.InvalidParameterValue(errorServiceIdList, "ServiceIdList");
        }
        return CommonErrorCode.Success;
    }

    @Override
    public List<Long> rePublishService(long gwId, boolean rePublishAllService, List<Long> serviceIdList) {
        if (!rePublishAllService && CollectionUtils.isEmpty(serviceIdList)) {
            return newArrayList();
        }

        List<ServiceProxyInfo> serviceProxyInfoList;
        if (rePublishAllService) {
            serviceProxyInfoList = serviceProxyService.getServiceProxyListByGwId(gwId);
        } else {
            serviceProxyInfoList = serviceProxyService.batchGetServiceProxyList(gwId, serviceIdList);
        }

        if (CollectionUtils.isEmpty(serviceProxyInfoList)) {
            return Lists.newArrayList();
        }

        return serviceProxyInfoList.stream().filter(item -> {
            List<String> backendServices = serviceProxyService.getServiceProxyByServiceId(item.getServiceId()).stream().
                    map(ServiceProxyInfo::getBackendService).collect(Collectors.toList());
            if (!backendServices.contains(item.getBackendService())) {
                logger.info("重发布服务，服务和已发布其他网关的服务，backendService不同，建议下线服务");
                return false;
            }
            long updateResult = serviceProxyService.updateServiceToGw(ServiceProxyDto.toDto(item));
            return Const.ERROR_RESULT == updateResult;
        }).map(ServiceProxyInfo::getServiceId).collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkRePublishRouteRuleParam(long gwId, boolean rePublishAllRouteRule, List<Long> serviceIdList, List<Long> routeRuleIdList) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (null == gatewayInfo) {
            logger.info("指定的网关不存在! gwId:{}", gwId);
            return CommonErrorCode.NoSuchGateway;
        }

        if (rePublishAllRouteRule) {
            return CommonErrorCode.Success;
        }

        if (!CollectionUtils.isEmpty(serviceIdList)) {
            return checkServiceIdList(gwId, serviceIdList);
        }

        if (CollectionUtils.isEmpty(routeRuleIdList)) {
            logger.info("参数RouteRuleIdList为空！");
            return CommonErrorCode.MissingParameter("RouteRuleIdList");
        }
        return checkRouteRuleList(gwId, routeRuleIdList);
    }

    private ErrorCode checkRouteRuleList(long gwId, List<Long> routeRuleIdList) {
        List<Long> errorRouteRuleList = routeRuleIdList.stream().filter(item -> {
            if (null == item) {
                return false;
            }
            RouteRuleProxyInfo routeRuleProxyInfo = routeRuleProxyService.getRouteRuleProxy(gwId, item);
            if (null == routeRuleProxyInfo) {
                return true;
            }
            return Const.ROUTE_RULE_DISABLE_STATE.equals(routeRuleProxyInfo.getEnableState());
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorRouteRuleList)) {
            logger.info("指定的路由规则尚未发布至该网关！ errorRouteRuleList:{}", errorRouteRuleList);
            return CommonErrorCode.InvalidParameterValue(errorRouteRuleList, "RouteRuleIdList");
        }
        return CommonErrorCode.Success;
    }

    @Override
    public List<Long> rePublishRouteRule(long gwId, boolean rePublishAllRouteRule, List<Long> serviceIdList, List<Long> routeRuleIdList) {

        if (rePublishAllRouteRule) {
            serviceIdList = serviceProxyService.getServiceProxyListByGwId(gwId).stream().map(ServiceProxyInfo::getServiceId).collect(Collectors.toList());
        }

        if (!CollectionUtils.isEmpty(serviceIdList)) {
            return serviceIdList.stream().map(item -> {
                        if (null == item) {
                            return new ArrayList<Long>();
                        }
                        logger.info("重发布服务{}下的路由规则", item);
                        return rePublishRouteRule(gwId, item);
                    }).parallel()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        return routeRuleIdList.stream().filter(item -> {
            logger.info("重发布路由规则:{}", item);
            RouteRuleProxyInfo routeRuleProxyInfo = routeRuleProxyService.getRouteRuleProxy(gwId, item);
            long updateResult = routeRuleProxyService.updateEnvoyRouteRuleProxy(routeRuleProxyInfo);
            return Const.ERROR_RESULT == updateResult;
        }).parallel().collect(Collectors.toList());
    }

    private List<Long> rePublishRouteRule(long gwId, long serviceId) {
        List<RouteRuleProxyInfo> routeRuleProxyInfoList = routeRuleProxyService.getRouteRuleProxyListByServiceId(gwId, serviceId);
        return routeRuleProxyInfoList.stream().filter(item -> {
            logger.info("重发布路由规则:{}", item.getRouteRuleId());
            long updateResult = routeRuleProxyService.updateEnvoyRouteRuleProxy(item);
            return Const.ERROR_RESULT == updateResult;
        }).map(RouteRuleProxyInfo::getRouteRuleId).parallel().collect(Collectors.toList());
    }

    @Override
    public List<Long> reFixPublishedRouteDao(long gwId) {
        List<RouteRuleProxyInfo> routeRuleProxyListByGwId = routeRuleProxyService.getRouteRuleProxyListByGwId(gwId);
        if (CollectionUtils.isEmpty(routeRuleProxyListByGwId)) {
            return Lists.newArrayList();
        }
        return routeRuleProxyListByGwId.stream().filter(item -> {
            RouteRuleInfo routeRuleInfoById = routeRuleInfoService.getRouteRuleInfoById(item.getRouteRuleId());
            item.setUri(routeRuleInfoById.getUri());
            item.setMethod(routeRuleInfoById.getMethod());
            item.setHeader(routeRuleInfoById.getHeader());
            item.setQueryParam(routeRuleInfoById.getQueryParam());
            item.setHost(routeRuleInfoById.getHost());
            item.setOrders(routeRuleInfoById.getOrders());
            item.setPriority(routeRuleInfoById.getPriority());
            return routeRuleProxyDao.update(item) <= 0;
        }).map(RouteRuleProxyInfo::getRouteRuleId).parallel().collect(Collectors.toList());
    }

    @Override
    public Boolean fixAuthPluginConfig(long gwId) {
        logger.info("Starting: fix super-auth plugin-type, kind");
        try {
            List<EnvoyPluginBindingInfo> pluginBindingList = getPluginBindingList(gwId, "super-auth");
            pluginBindingList.stream().forEach(pluginBindingInfo -> envoyPluginBindingInfoDao.update(convertAuthType(pluginBindingInfo)));
            logger.info("Ending: envoy plugin binding fix end");
            List<EnvoyPluginTemplateInfo> pluignTemplateList = getPluignTemplate("super-auth");
            pluignTemplateList.stream().forEach(pluginTemplateInfo -> envoyPluginTemplateDao.update(convertAuthType(pluginTemplateInfo)));
        } catch (Exception e) {
            logger.error("fix auth plugin error: {}", e);
            return false;
        }
        logger.info("Ending: envoy plugin template fix end");
        return true;
    }


    @Override
    public PublishResultDto rePublishPlugin(RePublishPluginDto rePublishPluginDto) {
        List<EnvoyPluginBindingInfo> pluginBindingInfoList = envoyPluginBindingInfoDao.getBindingPluginList(ProjectTraceHolder.getProId(),
                rePublishPluginDto.getGwId(), rePublishPluginDto.getBindingObjectType(), rePublishPluginDto.getBindingObjectIdList());
        if (CollectionUtils.isEmpty(pluginBindingInfoList)){
            return PublishResultDto.ofNull();
        }
        PublishResultDto publishResultDto = PublishResultDto.ofInit(pluginBindingInfoList.size());
        for (EnvoyPluginBindingInfo envoyPluginBindingInfo : pluginBindingInfoList) {
            long id = envoyPluginBindingInfo.getId();
            //dto转换
            BindingPluginInfo bindingPluginInfo = BindingPluginInfo.createBindingPluginFromEnvoyPluginBindingInfo(envoyPluginBindingInfo);
            //获取需要绑定的插件
            List<String> toBePublishedPluginList = envoyPluginServiceImpl.createToBePublishedPluginList(bindingPluginInfo,
                    EnvoyPluginServiceImpl.Operation.UPDATE, Collections.singletonList(id));
            //更新插件
            boolean bindingResult = envoyPluginServiceImpl.createPluginAndMakeRequest(bindingPluginInfo,
                    EnvoyPluginServiceImpl.Operation.UPDATE, toBePublishedPluginList);
            if (bindingResult) {
                publishResultDto.addSuccessResult(id);
            }else {
                publishResultDto.addFailResult(id);
            }
        }
        return publishResultDto;
    }

    private List<EnvoyPluginBindingInfo> getPluginBindingList(long gwId, String pluginType) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("pluginType", pluginType);
        return envoyPluginBindingInfoDao.getRecordsByField(params);
    }

    private EnvoyPluginBindingInfo convertAuthType(EnvoyPluginBindingInfo pluginBindingInfo) {
        //fix kind
        JSONObject jsonObject = JSONObject.parseObject(pluginBindingInfo.getPluginConfiguration());
        String kind = authnToKind(jsonObject.getString("authnType"));
        if (null == kind) return pluginBindingInfo;
        jsonObject.put("kind", kind);
        pluginBindingInfo.setPluginConfiguration(JSONObject.toJSONString(jsonObject));
        pluginBindingInfo.setPluginType(kind);
        return pluginBindingInfo;
    }

    private String authnToKind(String authnType) {
        HashMap<String, String> authnTokind = new HashMap<String, String>() {
            {
                put("oauth2_authn_type", "oauth2-auth");
                put("jwt_authn_type", "jwt-auth");
                put("aksk_authn_type", "sign-auth");
            }
        };
        return authnTokind.get(authnType);
    }

    private List<EnvoyPluginTemplateInfo> getPluignTemplate(String pluginType) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("pluginType", pluginType);
        return envoyPluginTemplateDao.getRecordsByField(params);
    }

    private EnvoyPluginTemplateInfo convertAuthType(EnvoyPluginTemplateInfo pluginTemplateInfo) {
        //fix kind
        JSONObject jsonObject = JSONObject.parseObject(pluginTemplateInfo.getPluginConfiguration());
        String kind = authnToKind(jsonObject.getString("authnType"));
        if (null == kind) return pluginTemplateInfo;
        jsonObject.put("kind", kind);
        pluginTemplateInfo.setPluginConfiguration(JSONObject.toJSONString(jsonObject));
        pluginTemplateInfo.setPluginType(kind);
        return pluginTemplateInfo;
    }
}
