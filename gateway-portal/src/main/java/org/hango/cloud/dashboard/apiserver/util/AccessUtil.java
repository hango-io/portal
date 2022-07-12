package org.hango.cloud.dashboard.apiserver.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class AccessUtil {
    private static final Logger logger = LoggerFactory.getLogger(AccessUtil.class);

    /**
     * 从其他系统获取相关信息公共请求
     *
     * @param url
     * @param params
     * @param body
     * @param headers
     * @param methodType
     * @return
     */
    public static HttpClientResponse accessFromOtherPlat(String url, Map<String, String> params, String body, Map<String, String> headers, String methodType) {
        headers = headers == null ? new HashMap<>() : headers;
        headers.put("Content-Type", HttpClientUtil.DEFAULT_CONTENT_TYPE);
        headers.put("X-163-AcceptLanguage", "zh");
        StringBuilder paramsBuilder = new StringBuilder();

        HttpClientResponse httpClientResponse;
        try {
            if (!CollectionUtils.isEmpty(params)) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    paramsBuilder.append("&").append(entry.getKey()).append("=").append(
                            StringUtils.isBlank(entry.getValue()) ? StringUtils.EMPTY : URLEncoder.encode(entry.getValue(),
                                    Const.DEFAULT_ENCODING));
                }
            }
            httpClientResponse = HttpClientUtil.httpRequest(methodType, url + (paramsBuilder.length() == 0 ? paramsBuilder : ("?" + paramsBuilder.substring(1))), body, headers);
        } catch (Exception e) {
            logger.error("接口调用异常!", e);
            return null;
        }
        return httpClientResponse;
    }

    public static ResultActionWithMessage convertResponse(HttpClientResponse httpClientResponse) {
        if (httpClientResponse == null) {
            ErrorCode errorCode = CommonErrorCode.InternalServerError;
            return new ResultActionWithMessage(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), null);
        }
        int statusCode = httpClientResponse.getStatusCode();
        String message = "";
        Object result = null;
        String code = "";
        if (statusCode >= 500) {
            logger.error("请求返回状态码为：{}", statusCode);
            return new ResultActionWithMessage(CommonErrorCode.InternalServerError);
        }
        // 封装返回的Code,message，result以及statusCode
        String responseBody = httpClientResponse.getResponseBody();
        if (StringUtils.isNotEmpty(responseBody)) {
            try {
                if (responseBody.startsWith("data:")) {
                    responseBody = StringUtils.stripStart(responseBody, "data:");
                }
                JSONObject responseBodyObject = JSONObject.parseObject(responseBody);
                statusCode = httpClientResponse.getStatusCode();
                if (HttpStatus.SC_OK != statusCode) {
                    message = responseBodyObject.getString("Message");
                }
                code = responseBodyObject.getString("Code");
                result = responseBodyObject;
                logger.info("Resp Body : {}", JSON.toJSONString(result));
            } catch (Exception e) {
                logger.error("解析responseBody出现异常，异常信息：e:{}", e);
                return new ResultActionWithMessage(CommonErrorCode.InternalServerError);
            }
        }
        return new ResultActionWithMessage(statusCode, code, message, result);
    }

    public static void sendWithError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.setContentType(MappingJackson2JsonView.DEFAULT_CONTENT_TYPE);
        response.setStatus(errorCode.getStatusCode());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Code", errorCode.getCode());
        map.put("Message", errorCode.getMessage());
        map.put("RequestId", LogTraceUUIDHolder.getUUIDId());

        String responseBody = JSON.toJSONString(map);

        response.getWriter().write(responseBody);
    }
}
