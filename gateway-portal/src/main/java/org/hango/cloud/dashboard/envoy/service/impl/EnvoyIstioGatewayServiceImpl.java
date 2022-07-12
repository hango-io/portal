package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIstioGatewayService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyGatewaySettingDto;
import org.hango.cloud.dashboard.envoy.web.util.HttpCommonUtil;
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
 * @date 2020/1/9
 */
@Service
public class EnvoyIstioGatewayServiceImpl implements IEnvoyIstioGatewayService {

    public static final Logger logger = LoggerFactory.getLogger(EnvoyIstioGatewayServiceImpl.class);

    public static final String RESULT = "Result";

    @Autowired
    private ApiServerConfig apiServerConfig;

    @Override
    public boolean updateGatewaySetting(EnvoyGatewaySettingDto setting, GatewayInfo gatewayInfo) {
        String body = genGatewaySetting(setting, gatewayInfo);

        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "UpdateIstioGateway");
        params.put("Version", "2019-07-25");

        Map<String, String> headers = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        headers.put("Content-type", Const.DEFAULT_CONTENT_TYPE);

        try {
            String apiPlaneUrl = gatewayInfo.getApiPlaneAddr() + "/api/portal";
            HttpClientResponse response = HttpCommonUtil.getFromApiPlane(apiPlaneUrl, params, body, headers, HttpMethod.POST.name());
            if (response == null) {
                return false;
            }
            if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
                logger.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return false;
            }
            if (StringUtils.isNotBlank(apiServerConfig.getBakApiPlaneAddr()) && !apiPlaneUrl.equals(apiServerConfig.getBakApiPlaneAddr())) {
                response = HttpCommonUtil.getFromApiPlane(apiServerConfig.getBakApiPlaneAddr() + "/api/portal", params, body, headers, HttpMethod.POST.name());
            }
            return response != null && HttpCommonUtil.isNormalCode(response.getStatusCode());
        } catch (Exception e) {
            logger.error("调用api-plane发布接口异常，e:{}", e);
            return false;
        }
    }

    @Override
    public EnvoyGatewaySettingDto getGatewaySetting(GatewayInfo gatewayInfo) {
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "GetIstioGateway");
        params.put("Version", "2019-07-25");
        params.put("GwClusterName", gatewayInfo.getGwClusterName());

        try {
            HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params, null, null, HttpMethod.GET.name());
            if (response == null) {
                return null;
            }
            if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
                logger.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return null;
            }
            JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
            if (jsonObject == null) {
                logger.info("未查询到有效数据， GwId = {}", gatewayInfo.getId());
                return null;
            }
            EnvoyGatewaySettingDto envoyGatewaySettingDto = JSON.parseObject(jsonObject.getString(RESULT), EnvoyGatewaySettingDto.class);
            if (envoyGatewaySettingDto == null) {
                return null;
            }
            envoyGatewaySettingDto.setGwId(gatewayInfo.getId());
            return envoyGatewaySettingDto;
        } catch (Exception e) {
            logger.error("调用api-plane发布接口异常，e:{}", e);
            return null;
        }
    }

    private String genGatewaySetting(EnvoyGatewaySettingDto setting, GatewayInfo gatewayInfo) {
        JSONObject jsonObject = (JSONObject) JSON.toJSON(setting);
        jsonObject.put("GwCluster", gatewayInfo.getGwClusterName());
        //默认Gateway 资源名称为GwClusterName
        String name = gatewayInfo.getGwClusterName();
        EnvoyGatewaySettingDto gatewaySetting = getGatewaySetting(gatewayInfo);
        if (gatewaySetting != null) {
            name = gatewaySetting.getName();
        }
        jsonObject.put("Name", name);
        return jsonObject.toJSONString();
    }
}
