package org.hango.cloud.envoy.infra.plugin.rpc;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.util.LogUtil;
import org.hango.cloud.envoy.infra.plugin.dto.CustomPluginDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.hango.cloud.common.infra.base.meta.BaseConst.VERSION_19_07_25;
import static org.hango.cloud.gdashboard.api.util.Const.ACTION;
import static org.hango.cloud.gdashboard.api.util.Const.VERSION;

/**
 * @ClassName CustomPluginRpcService
 * @Description 封装对plane的远程调用
 * @Author xianyanglin
 * @Date 2023/7/5 14:02
 */
@Slf4j
@Service
public class CustomPluginRpcService {
    private static final String PATH = "/api/plugin";
    /**
     * 发布自定义插件
     */
    public Boolean publishCustomPlugin(String pluginName, String pluginContent, GatewayDto gateway) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(VERSION, VERSION_19_07_25);
        params.put(ACTION, "PublishCustomPlugin");
        CustomPluginDTO customPluginDTO = CustomPluginDTO.builder().pluginName(pluginName).gwCluster(gateway.getGwClusterName()).pluginContent(pluginContent).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(gateway.getConfAddr()+PATH, JSON.toJSONString(customPluginDTO), params, headers, EnvoyConst.MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            log.error("获取网关插件配置失败，返回http status code非2xx，httpStatusCode:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        log.info("发布自定义插件成功,返回结果:{}",response.getResponseBody());
        return true;
    }

    /**
     * 删除自定义插件
     *
     * @param pluginName   插件名称
     * @return 返回发布结果
     */
    public Boolean deleteCustomPlugin(String pluginName, GatewayDto gateway) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(VERSION, VERSION_19_07_25);
        params.put(ACTION, "DeleteCustomPlugin");
        CustomPluginDTO customPluginDTO = CustomPluginDTO.builder().pluginName(pluginName).gwCluster(gateway.getGwClusterName()).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(gateway.getConfAddr()+PATH, JSON.toJSONString(customPluginDTO), params, headers, EnvoyConst.MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            log.error(LogUtil.buildPlaneErrorLog(response));
            return false;
        }
        log.info("删除自定义插件,返回结果:{}",response.getResponseBody());
        return true;
    }
}
