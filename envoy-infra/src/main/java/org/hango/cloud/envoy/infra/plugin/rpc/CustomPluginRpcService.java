package org.hango.cloud.envoy.infra.plugin.rpc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.util.LogUtil;
import org.hango.cloud.envoy.infra.plugin.dto.CustomPluginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(CustomPluginRpcService.class);
    private static final String PATH = "/api/plugin";
    /**
     * 发布自定义插件
     *
     * @param confAddr    apiPlane服务地址
     * @param customPluginDTO 自定义插件封装类
     * @return 返回发布结果
     */
    public Boolean publishCustomPlugin(CustomPluginDTO customPluginDTO, String confAddr) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(VERSION, VERSION_19_07_25);
        params.put(ACTION, "PublishCustomPlugin");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(confAddr+PATH, JSON.toJSONString(customPluginDTO), params, headers, EnvoyConst.MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("获取网关插件配置失败，返回http status code非2xx，httpStatusCode:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        logger.info("发布自定义插件成功,返回结果:{}",response.getResponseBody());
        return true;
    }

    /**
     * 删除自定义插件
     *
     * @param pluginName   插件名称
     * @param confAddr    apiPlane服务地址
     * @return 返回发布结果
     */
    public Boolean deleteCustomPlugin(String pluginName, String language, String confAddr) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(VERSION, VERSION_19_07_25);
        params.put(ACTION, "DeleteCustomPlugin");
        CustomPluginDTO customPluginDTO = CustomPluginDTO.builder().pluginName(pluginName).language(language).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.postRequest(confAddr+PATH, JSON.toJSONString(customPluginDTO), params, headers, EnvoyConst.MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            log.error(LogUtil.buildPlaneErrorLog(response));
            return false;
        }
        logger.info("删除自定义插件,返回结果:{}",response.getResponseBody());
        return true;
    }
    /**
     * 查看自定义插件
     *
     * @param pluginName   插件名称
     * @param language   插件实现语言
     * @param confAddr    apiPlane服务地址
     * @return 返回发布结果
     */
    public JSONObject getCustomPlugin(String pluginName,String language, String confAddr) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(VERSION, VERSION_19_07_25);
        params.put(ACTION, "GetCustomPlugin");
        params.put("PluginName", pluginName);
        params.put("Language", language);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response = HttpClientUtil.getRequest(confAddr+PATH, params, headers, EnvoyConst.MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            log.error(LogUtil.buildPlaneErrorLog(response));
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
        if (jsonObject == null) {
            log.info("未查询到有效数据");
            return null;
        }
        logger.info("查看自定义插件,返回结果:{}",jsonObject.toJSONString());
        return jsonObject;
    }
}
