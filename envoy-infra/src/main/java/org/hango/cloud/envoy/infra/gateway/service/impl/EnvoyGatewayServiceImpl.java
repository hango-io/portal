package org.hango.cloud.envoy.infra.gateway.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.gateway.dao.IGatewayDao;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.gateway.service.impl.GatewayServiceImpl;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboMetaDto;
import org.hango.cloud.envoy.infra.gateway.dto.EnvoyServiceDTO;
import org.hango.cloud.envoy.infra.gateway.service.IEnvoyGatewayService;
import org.hango.cloud.envoy.infra.virtualgateway.dto.IstioGatewayDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hango.cloud.common.infra.base.meta.BaseConst.PLANE_PORTAL_PATH;

/**
 * @Author zhufengwei
 * @Date 2023/3/21
 */
@Slf4j
@Service
public class EnvoyGatewayServiceImpl implements IEnvoyGatewayService {

    @Autowired
    IGatewayService gatewayService;

    @Override
    public List<EnvoyServiceDTO> getEnvoyService(long gwId) {
        GatewayDto gatewayDto = gatewayService.get(gwId);
        if (gatewayDto == null){
            return new ArrayList<>();
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "GetEnvoyService");
        params.put("Version", BaseConst.PLANE_VERSION);
        params.put("GwCluster", gatewayDto.getGwClusterName());
        try {
            HttpClientResponse response = HttpClientUtil.getRequest(gatewayDto.getConfAddr() + "/api", params, EnvoyConst.MODULE_API_PLANE);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                log.error("调用api-plane获取envoy service端口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return new ArrayList<>();
            }
            JSONObject jsonResult = JSONObject.parseObject(response.getResponseBody());
            JSONArray services = jsonResult.getJSONArray("Result");
            return JSONObject.parseArray(services.toJSONString(), EnvoyServiceDTO.class);
        } catch (Exception e) {
            log.error("调用api-plane获取envoy service端口异常，e", e);
            return new ArrayList<>();
        }
    }

}
