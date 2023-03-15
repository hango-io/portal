package org.hango.cloud.envoy.infra.virtualgateway.rpc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.virtualgateway.dto.KubernetesGatewayInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 封装对plane的远程调用
 * @Author xianyanglin
 * @Date 2022/12/14 19:49
 */
@Slf4j
@Service
public class VirtualGatewayRpcService {
    /**
     * 查询K8s Gateway列表
     *
     * @param confAddr    apiPlane服务地址
     * @param gatewayName 网关名字
     * @return K8s Gateway列表对象
     */
    public List<KubernetesGatewayInfo> getKubernetesGateway(String confAddr, String gatewayName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "GetKubernetesGateway");
        params.put("Version", "2022-12-31");
        if (StringUtils.isNotEmpty(gatewayName)) {
            params.put("GatewayName", gatewayName);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpClientResponse response = HttpClientUtil.getRequest(confAddr + "/api/gatewayapi", params, headers, EnvoyConst.MODULE_API_PLANE);
            if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
                log.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return new ArrayList<>();
            }
            JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
            if (jsonObject == null) {
                log.info("未查询到有效数据");
                return new ArrayList<>();
            }
            List<KubernetesGatewayInfo> gatewayDTOS = JSONArray.parseArray(jsonObject.getString(BaseConst.RESULT_LIST), KubernetesGatewayInfo.class);
            return gatewayDTOS.stream().filter(o -> o.getProjectId() != null).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("调用api-plane发布接口异常，e:", e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询K8s Gateway 路由列表
     *
     * @param confAddr    apiPlane服务地址
     * @param gatewayName 网关名字
     * @return K8s Gateway 路由列表
     */
    public List<HTTPRoute> getKubernetesGatewayHttpRoute(String confAddr, String gatewayName) {
        List<HTTPRoute> result = new ArrayList<>();
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "GetHTTPRoute");
        params.put("Version", "2022-12-31");
        if (StringUtils.isNotEmpty(gatewayName)) {
            params.put("GatewayName", gatewayName);
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpClientResponse response = HttpClientUtil.getRequest(confAddr + "/api/gatewayapi", params, headers, EnvoyConst.MODULE_API_PLANE);
            if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
                log.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
                return new ArrayList<>();
            }
            log.info("ResponseBody :{}", response.getResponseBody());
            JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
            if (jsonObject == null) {
                log.info("请求 plane 获取Kubernetes Gateway 路由列表为空,GatewayName 为:{}", gatewayName);
                return new ArrayList<>();
            }
            List<HTTPRoute> kubernetesGatewayHttpRouteList = JSON.parseArray(jsonObject.getString(BaseConst.RESULT_LIST), HTTPRoute.class);
            result.addAll(kubernetesGatewayHttpRouteList);
        } catch (Exception e) {
            log.error("调用api-plane发布接口异常，e:", e);
            return new ArrayList<>();
        }
        return result;
    }
}
