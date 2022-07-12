package org.hango.cloud.dashboard.envoy.web.util;

import org.apache.commons.httpclient.HttpStatus;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpCommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpCommonUtil.class);

    public static HttpClientResponse getFromApiPlane(String apiPlaneUrl, Map<String, String> params, String body, Map<String, String> headers, String methodType) {
        headers = headers == null ? new HashMap<>(Const.DEFAULT_MAP_SIZE) : headers;
        headers.put("Content-Type", HttpClientUtil.DEFAULT_CONTENT_TYPE);
        StringBuilder paramsBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        HttpClientResponse httpClientResponse;
        try {
            httpClientResponse = HttpClientUtil.httpRequest(methodType, apiPlaneUrl + "?" + paramsBuilder.substring(1), body, headers);
        } catch (Exception e) {
            logger.error("调用api-plane失败", e);
            return null;
        }
        return httpClientResponse;
    }

    public static HttpClientResponse getFromAuth(String authUrl, Map<String, String> params, String body, Map<String, String> headers, String methodType) {
        headers = headers == null ? new HashMap<>(Const.DEFAULT_MAP_SIZE) : headers;
        headers.put("Content-Type", HttpClientUtil.DEFAULT_CONTENT_TYPE);
        StringBuilder paramsBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        HttpClientResponse httpClientResponse;
        try {
            httpClientResponse = HttpClientUtil.httpRequest(methodType, authUrl + "?" + paramsBuilder.substring(1), body, headers);
        } catch (Exception e) {
            logger.error("调用auth服务失败", e);
            return null;
        }
        return httpClientResponse;
    }


    /**
     * 2xx响应码
     */
    public static boolean isNormalCode(int httpStatusCode) {
        return HttpStatus.SC_OK <= httpStatusCode && httpStatusCode < HttpStatus.SC_MULTIPLE_CHOICES;
    }

    /**
     * 4xx响应码
     */
    public static boolean is4XXCode(int httpStatusCode) {
        return HttpStatus.SC_BAD_REQUEST <= httpStatusCode && httpStatusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }
}
