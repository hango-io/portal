package org.hango.cloud.dashboard.apiserver.service.impl.platform;

import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.HttpClientUtil;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ServiceAuthServiceImpl extends CommonServiceFromPlatform {

    private static String serivceAuthPath = "/service";
    @Autowired
    IGatewayInfoService gatewayInfoService;

    public AbstractController.ResultWithMessage getExterServiceFromAuth(String authUrl, String pattern) {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "DescirbeExterServiceByFuzzy");
        params.put("Version", "2018-08-09");
        params.put("Pattern", HttpClientUtil.encodeValue(pattern));
        HttpClientResponse httpClientResponse = accessFromAuthority(authUrl + serivceAuthPath, params, null, null, Const.GET_METHOD);
        return convertResponse(httpClientResponse);
    }

    public AbstractController.ResultWithMessage getExterServiceByLimit(String authUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "GetExterServiceList");
        params.put("Version", "2018-08-09");
        HttpClientResponse httpClientResponse = accessFromAuthority(authUrl + serivceAuthPath, params, null, null, Const.GET_METHOD);
        return convertResponse(httpClientResponse);
    }
}
