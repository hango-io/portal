package org.hango.cloud.envoy.infra.credential.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoDTO;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoViewDTO;
import org.hango.cloud.common.infra.credential.service.ICertificateInfoService;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.envoy.infra.base.config.EnvoyConfig;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.credential.service.IEnvoySecretProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.hango.cloud.common.infra.base.meta.BaseConst.PLANE_PORTAL_PATH;

@Slf4j
@Service
public class EnvoySecretProxyServiceImpl implements IEnvoySecretProxyService {
    @Autowired
    private ICertificateInfoService certificateInfoService;

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private EnvoyConfig envoyConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishToGateway(CertificateInfoDTO certificateInfoDTO) {
        List<? extends GatewayDto> allGateway = gatewayService.findAll();
        if (allGateway == null) {
            log.warn("网关信息为空");
            return false;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "PublishSecret");
        params.put("Version", "2019-07-25");

        Map<String, Object> bodys = Maps.newHashMap();
        bodys.put("Name", certificateInfoDTO.getName());
        Base64.Encoder encoder = Base64.getEncoder();
        bodys.put("ServerCrt", encoder.encodeToString(certificateInfoDTO.getContent().getBytes()));
        bodys.put("ServerKey", encoder.encodeToString(certificateInfoDTO.getPrivateKey().getBytes()));
        boolean result = true;
        for (GatewayDto gatewayDto : allGateway) {
            boolean publishRes = doPublish(gatewayDto.getConfAddr(), params, bodys);
            result &= publishRes;
        }
        return result;
    }

    @Override
    public boolean offlineToGateway(CertificateInfoDTO certificateInfoDTO) {
        CertificateInfoViewDTO dbInfo = certificateInfoService.getCertificateInfoById(certificateInfoDTO.getCertificateId());
        List<? extends GatewayDto> allGateway = gatewayService.findAll();
        if (allGateway == null) {
            log.warn("网关信息为空");
            return false;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "DeleteSecret");
        params.put("Version", "2019-07-25");

        Map<String, Object> bodys = Maps.newHashMap();
        bodys.put("Name", dbInfo.getName());
        boolean result = true;
        for (GatewayDto gatewayDto : allGateway) {
            boolean publishRes = doPublish(gatewayDto.getConfAddr(), params, bodys);
            result &= publishRes;
        }
        return result;
    }


    private boolean doPublish(String confAddr, Map<String, Object> params, Object body){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpClientResponse response = HttpClientUtil.postRequest(confAddr + PLANE_PORTAL_PATH, JSONObject.toJSONString(body), params, headers, EnvoyConst.MODULE_API_PLANE);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                log.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return false;
            }
        } catch (Exception e) {
            log.error("调用api-plane发布接口异常", e);
            return false;
        }
        return true;
    }

}

