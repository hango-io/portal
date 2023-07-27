package org.hango.cloud.envoy.advanced.bakup.envoy.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.hango.cloud.common.infra.base.convert.RouteRuleConvert;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.hango.cloud.common.infra.route.dao.IRouteDao;
import org.hango.cloud.common.infra.route.dao.RouteMapper;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.pojo.RoutePO;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.advanced.bakup.apiserver.util.Const;
import org.hango.cloud.envoy.advanced.bakup.envoy.service.IEnvoyDateFixService;
import org.hango.cloud.envoy.infra.plugin.service.IEnvoyPluginInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IRouteService routeService;

    @Autowired
    private IRouteDao routeRuleProxyDao;

    @Autowired
    private IPluginTemplateService pluginTemplateService;

    @Autowired
    private IPluginBindingInfoDao envoyPluginBindingInfoDao;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @Autowired
    private RouteMapper routeMapper;

    @Override
    public ErrorCode checkRePublishServiceParam(long virtualGwId, boolean rePublishAllService, List<Long> serviceIdList) {
         VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        if (null == virtualGateway) {
            logger.info("指定的网关不存在! virtualGwId:{}", virtualGwId);
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }

        if (rePublishAllService) {
            return CommonErrorCode.SUCCESS;
        }

        if (CollectionUtils.isEmpty(serviceIdList)) {
            logger.info("参数ServiceIdList指定的服务列表为空！");
            return CommonErrorCode.MissingParameter("ServiceIdList");
        }

        return checkServiceIdList(virtualGwId, serviceIdList);
    }

    private ErrorCode checkServiceIdList(long virtualGwId, List<Long> serviceProxyIdList) {
        List<Long> errorServiceIdList = serviceProxyIdList.stream().filter(item -> {
            if (null == item) {
                return false;
            }
            ServiceProxyDto serviceProxyInfo = serviceProxyService.get(item);
            return null == serviceProxyInfo;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorServiceIdList)) {
            logger.info("服务尚未发布到指定网关！ errorServiceIdList:{}", errorServiceIdList);
            return CommonErrorCode.invalidParameterValue(errorServiceIdList, "ServiceIdList");
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<Long> rePublishService(long virtualGwId, boolean rePublishAllService, List<Long> serviceProxyIdList) {
        if (!rePublishAllService && CollectionUtils.isEmpty(serviceProxyIdList)) {
            return newArrayList();
        }

        List<ServiceProxyDto> serviceProxyInfoList;
        if (rePublishAllService) {
            serviceProxyInfoList = serviceProxyService.getServiceProxyListByVirtualGwId(virtualGwId);
        } else {
            serviceProxyInfoList = serviceProxyService.getServiceByIds(serviceProxyIdList);
        }

        if (CollectionUtils.isEmpty(serviceProxyInfoList)) {
            return Lists.newArrayList();
        }

        return serviceProxyInfoList.stream().filter(item -> {
            long updateResult = serviceProxyService.update(item);
            return BaseConst.ERROR_RESULT == updateResult;
        }).map(ServiceProxyDto::getId).collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkRePublishRouteRuleParam(long virtualGwId, boolean rePublishAllRouteRule, List<Long> serviceIdList, List<Long> routeRuleIdList) {
         VirtualGatewayDto virtualGateway = ((VirtualGatewayDto) virtualGatewayInfoService.get(virtualGwId));
        if (null == virtualGateway) {
            logger.info("指定的网关不存在! virtualGwId:{}", virtualGwId);
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }

        if (rePublishAllRouteRule) {
            return CommonErrorCode.SUCCESS;
        }

        if (!CollectionUtils.isEmpty(serviceIdList)) {
            return checkServiceIdList(virtualGwId, serviceIdList);
        }

        if (CollectionUtils.isEmpty(routeRuleIdList)) {
            logger.info("参数RouteRuleIdList为空！");
            return CommonErrorCode.MissingParameter("RouteRuleIdList");
        }
        return checkRouteRuleList(virtualGwId, routeRuleIdList);
    }

    private ErrorCode checkRouteRuleList(long virtualGwId, List<Long> routeRuleIdList) {
        List<Long> errorRouteRuleList = routeRuleIdList.stream().filter(item -> {
            if (null == item) {
                return false;
            }
            RouteDto routeRuleProxyInfo = routeService.getRoute(virtualGwId, item);
            if (null == routeRuleProxyInfo) {
                return true;
            }
            return Const.ROUTE_RULE_DISABLE_STATE.equals(routeRuleProxyInfo.getEnableState());
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorRouteRuleList)) {
            logger.info("指定的路由规则尚未发布至该网关！ errorRouteRuleList:{}", errorRouteRuleList);
            return CommonErrorCode.invalidParameterValue(errorRouteRuleList, "RouteRuleIdList");
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<Long> rePublishRouteRule(long virtualGwId, boolean rePublishAllRouteRule, List<Long> serviceIdList, List<Long> routeRuleIdList) {

        if (rePublishAllRouteRule) {
            serviceIdList = serviceProxyService.getServiceProxyListByVirtualGwId(virtualGwId).stream().map(ServiceProxyDto::getId).collect(Collectors.toList());
        }

        if (!CollectionUtils.isEmpty(serviceIdList)) {
            return serviceIdList.stream().map(item -> {
                        if (null == item) {
                            return new ArrayList<Long>();
                        }
                        logger.info("重发布服务{}下的路由规则", item);
                        return rePublishRouteRule(virtualGwId, item);
                    }).parallel()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        return routeRuleIdList.stream().filter(item -> {
            logger.info("重发布路由规则:{}", item);
            RouteDto routeRuleProxyInfo = routeService.getRoute(virtualGwId, item);
            long updateResult = routeService.update(routeRuleProxyInfo);
            return BaseConst.ERROR_RESULT == updateResult;
        }).parallel().collect(Collectors.toList());
    }

    private List<Long> rePublishRouteRule(long virtualGwId, long serviceId) {
        RouteQuery query = RouteQuery.builder().virtualGwId(virtualGwId).serviceId(serviceId).build();
        List<RouteDto> routeRuleProxyList = routeService.getRouteList(query);
        return routeRuleProxyList.stream().filter(item -> {
            logger.info("重发布路由规则:{}", item.getId());
            long updateResult = routeService.update(item);
            return BaseConst.ERROR_RESULT == updateResult;
        }).map(RouteDto::getId).parallel().collect(Collectors.toList());
    }

    @Override
    public List<Long> reFixPublishedRouteDao(long virtualGwId) {
        RouteQuery query = RouteQuery.builder().virtualGwId(virtualGwId).build();
        List<RouteDto> routeRuleProxyList = routeService.getRouteList(query);
        if (CollectionUtils.isEmpty(routeRuleProxyList)) {
            return Lists.newArrayList();
        }
        return routeRuleProxyList.stream().filter(item -> {
            RoutePO routePO = routeMapper.selectById(item.getId());
            RouteRuleConvert.fillMatchView(item, routePO);
            return routeMapper.updateById(routeService.toMeta(item)) <= 0;
        }).map(RouteDto::getId).parallel().collect(Collectors.toList());
    }

    @Override
    public Boolean fixAuthPluginConfig(long virtualGwId) {
        logger.info("Starting: fix super-auth plugin-type, kind");
        try {
            PluginBindingInfoQuery query = PluginBindingInfoQuery.builder().virtualGwId(virtualGwId).pluginType(Collections.singletonList("super-auth")).build();
            List<PluginBindingInfo> pluginBindingList = envoyPluginBindingInfoDao.getPluginBindingInfoList(query);
            pluginBindingList.forEach(pluginBindingInfo -> envoyPluginBindingInfoDao.update(convertAuthType(pluginBindingInfo)));
            logger.info("Ending: envoy plugin binding fix end");
            List<PluginTemplateInfo> pluginTemplateList = getPluginTemplateByType("super-auth");
            pluginTemplateList.stream().map(pluginTemplateInfo -> pluginTemplateService.toView(convertAuthType(pluginTemplateInfo)))
                    .forEach(pluginTemplateInfo -> pluginTemplateService.update(pluginTemplateInfo));
        } catch (Exception e) {
            logger.error("fix auth plugin error", e);
            return false;
        }
        logger.info("Ending: envoy plugin template fix end");
        return true;
    }


    private PluginBindingInfo convertAuthType(PluginBindingInfo pluginBindingInfo) {
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

    private List<PluginTemplateInfo> getPluginTemplateByType(String pluginType) {
        return pluginTemplateService.getPluginTemplateByType(pluginType);
    }

    private PluginTemplateInfo convertAuthType(PluginTemplateInfo pluginTemplateInfo) {
        //fix kind
        JSONObject jsonObject = JSONObject.parseObject(pluginTemplateInfo.getPluginConfiguration());
        String kind = authnToKind(jsonObject.getString("authnType"));
        if (null == kind) {
            return pluginTemplateInfo;
        }
        jsonObject.put("kind", kind);
        pluginTemplateInfo.setPluginConfiguration(JSONObject.toJSONString(jsonObject));
        pluginTemplateInfo.setPluginType(kind);
        return pluginTemplateInfo;
    }
}
