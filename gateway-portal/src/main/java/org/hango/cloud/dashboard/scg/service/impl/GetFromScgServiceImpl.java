package org.hango.cloud.dashboard.scg.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.util.AccessUtil;
import org.hango.cloud.dashboard.apiserver.util.ResultActionWithMessage;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;
import org.hango.cloud.dashboard.scg.service.IGetFromScgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2021/11/18
 */
@Service
public class GetFromScgServiceImpl implements IGetFromScgService {

    private static final Logger logger = LoggerFactory.getLogger(GetFromScgServiceImpl.class);
    private static final String GW_SERVICE_PREFIX = "/service";
    private static final String GW_ROUTE_PREFIX = "/route";
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Override
    public boolean publishServiceToScgGw(ServiceProxyDto serviceProxyDto) {

        Map<String, String> params = new HashMap<>();
        params.put("Action", "PublishService");
        params.put("Version", "2021-12-30");
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(
                getGwAddr(serviceProxyDto.getGwId()) + GW_SERVICE_PREFIX,
                params,
                JSON.toJSONString(serviceProxyDto),
                null, HttpMethod.POST.name());
        ResultActionWithMessage resultActionWithMessage = AccessUtil.convertResponse(httpClientResponse);
        if (HttpStatus.SC_OK == resultActionWithMessage.getStatusCode()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean offlineServiceToScgGw(ServiceProxyDto serviceProxyDto) {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "OfflineService");
        params.put("Version", "2021-12-30");
        params.put("ServiceId", String.valueOf(serviceProxyDto.getServiceId()));
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(getGwAddr(serviceProxyDto.getGwId()) + GW_SERVICE_PREFIX,
                params,
                null,
                null,
                HttpMethod.GET.name());
        ResultActionWithMessage resultActionWithMessage = AccessUtil.convertResponse(httpClientResponse);
        if (HttpStatus.SC_OK == resultActionWithMessage.getStatusCode()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean publishRouteToScgGw(RouteRuleProxyInfo routeRuleProxyInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "PublishRoute");
        params.put("Version", "2021-12-30");
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(routeRuleProxyService.fromMeta(routeRuleProxyInfo));
        jsonObject.put("Orders", routeRuleProxyInfo.getOrders());
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(
                getGwAddr(routeRuleProxyInfo.getGwId()) + GW_ROUTE_PREFIX,
                params,
                JSON.toJSONString(jsonObject),
                null,
                HttpMethod.POST.name());
        ResultActionWithMessage resultActionWithMessage = AccessUtil.convertResponse(httpClientResponse);
        if (HttpStatus.SC_OK == resultActionWithMessage.getStatusCode()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean offlineRouteToScgGw(RouteRuleProxyInfo routeRuleProxyInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "OfflineRoute");
        params.put("Version", "2021-12-30");
        params.put("RouteRuleId", String.valueOf(routeRuleProxyInfo.getRouteRuleId()));
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(
                getGwAddr(routeRuleProxyInfo.getGwId()) + GW_ROUTE_PREFIX,
                params,
                null,
                null,
                HttpMethod.GET.name());
        ResultActionWithMessage resultActionWithMessage = AccessUtil.convertResponse(httpClientResponse);
        if (HttpStatus.SC_OK == resultActionWithMessage.getStatusCode()) {
            return true;
        }
        return false;
    }


    /**
     * 获取网关地址
     *
     * @param gwId
     * @return
     */
    private String getGwAddr(long gwId) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.info("未找到对应网关，网关ID {}", gwId);
            return StringUtils.EMPTY;
        }
        logger.info("执行发布操作，发布对应网关的信息为 {} ", JSON.toJSONString(gatewayInfo));
        return gatewayInfo.getApiPlaneAddr();
    }

}
