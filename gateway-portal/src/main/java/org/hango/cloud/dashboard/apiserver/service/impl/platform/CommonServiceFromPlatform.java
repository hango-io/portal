package org.hango.cloud.dashboard.apiserver.service.impl.platform;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.util.HttpClientUtil;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CommonServiceFromPlatform {

    private static final Logger logger = LoggerFactory.getLogger(CommonServiceFromPlatform.class);

    /**
     * 从平台获取相关数据
     *
     * @param authorityUrl 平台url
     * @param params       请求参数
     * @param body         请求体
     * @param headers      请求头
     * @param methodType   请求method
     * @return
     */
    public HttpClientResponse accessFromAuthority(String authorityUrl, Map<String, String> params, String body, Map<String, String> headers, String methodType) {
        headers = headers == null ? new HashMap<>() : headers;
        headers.put("Content-Type", HttpClientUtil.DEFAULT_CONTENT_TYPE);
        headers.put("X-163-AcceptLanguage", "zh");
        StringBuilder paramsBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        HttpClientResponse httpClientResponse;
        try {
            httpClientResponse = HttpClientUtil.httpRequest(methodType, authorityUrl + "?" + paramsBuilder.substring(1), body, headers);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("请求平台失败,{}", e.getMessage());
            return null;
        }
        return httpClientResponse;
    }

    /**
     * 将httpClientResonse转换为ResultWithMessage类型
     *
     * @param httpClientResponse
     * @return
     */
    public AbstractController.ResultWithMessage convertResponse(HttpClientResponse httpClientResponse) {
        if (httpClientResponse != null) {
            int code = httpClientResponse.getStatusCode();
            String message = "";
            Object result = null;
            // 返回的code、message、result进行封装直接返回给前端
            if (StringUtils.isNotEmpty(httpClientResponse.getResponseBody())) {
                JSONObject responseBodyObject = JSONObject.parseObject(httpClientResponse.getResponseBody());
                code = httpClientResponse.getStatusCode();
                if (HttpStatus.SC_OK != code) {
                    message = responseBodyObject.getString("Message");
                }
                result = responseBodyObject;
            }
            return new AbstractController.ResultWithMessage(code, message, result);
        }
        return null;
    }
}
