package org.hango.cloud.dashboard.apiserver.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Optional;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.exception.AbnormalStatusCodeException;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.Pair;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.HttpClientUtil;
import org.hango.cloud.dashboard.apiserver.util.LogTraceUUIDHolder;
import org.hango.cloud.dashboard.apiserver.util.ResponseBodyHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.RequestContextHolder;
import org.hango.cloud.gdashboard.api.meta.errorcode.AbstractErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhu Jianfeng (hzzhujianfeng)
 * @version $Id: AbstractController.java, v 1.0 2017年3月24日 上午11:29:44
 */
@Component
public class AbstractController {

    public static final String RESULT = "Result";

    public static final String TOTAL_COUNT = "TotalCount";
    protected static final Logger logger = LoggerFactory.getLogger(AbstractController.class);
    private static final String UTF8_NAME = "utf-8";

    private static HttpHeaders getDefaultHeaders() {
        HttpHeaders headers;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return headers;
    }

    public static String apiReturn(int statusCode, String code, String message, Map<String, Object> params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("RequestId", LogTraceUUIDHolder.getUUIDId());
        if (StringUtils.isNotBlank(code)) {
            body.put("Code", code);
        }
        if (StringUtils.isNotBlank(message)) {
            body.put("Message", message);
        }
        if (!CollectionUtils.isEmpty(params)) {
            body.putAll(params);
        }
        HttpServletResponse response = RequestContextHolder.getResponse();
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.setContentType(MappingJackson2JsonView.DEFAULT_CONTENT_TYPE);
        response.setStatus(statusCode);
        try {
            //将ResponseBody 放入ThreadLocal ，供操作审计使用
            Pair<String, String> pair = new Pair<>(ResponseBodyHolder.ACTION, JSON.toJSONString(body));
            ResponseBodyHolder.set(pair);
            response.getWriter().write(JSON.toJSONString(body, SerializerFeature.WriteMapNullValue));
        } catch (IOException e) {
            logger.warn("io exception.", e);
        }
        return null;
    }

    public static String apiReturn(AbstractErrorCode errorCode) {
        return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * parse request's json body and return the JSONObject
     *
     * @param request
     * @param encoding body encode, utf-8 by default
     * @return
     * @throws IOException
     */
    public JSONObject parseJsonBody(HttpServletRequest request, String encoding) throws IOException {
        if (org.apache.commons.lang.StringUtils.isEmpty(encoding)) {
            encoding = "UTF-8";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), encoding));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        if (sb.length() != 0) {
            return JSONObject.parseObject(sb.toString());
        } else {
            return null;
        }
    }

    protected String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.isBlank(userAgent) ? StringUtils.EMPTY : userAgent;
    }

    /**
     * 将G1返回的code、message、result进行封装直接返回给前端
     *
     * @param responseBody
     * @return
     */
    public ResultWithMessage convertResponse(String responseBody) {
        //将G1返回的code、message、result进行封装直接返回给前端
        JSONObject responseBodyObject = JSONObject.parseObject(responseBody);
        int code = (int) responseBodyObject.get("code");
        String message = (String) responseBodyObject.get("message");
        Object result = responseBodyObject.get("result");

        return new ResultWithMessage(code, message, result);
    }

    /**
     * 将G0返回的code、message、result进行封装直接返回给前端
     *
     * @param httpClientResponse
     * @return
     */
    public ResultWithMessage convertResponseG0(HttpClientResponse httpClientResponse) {
        int code = httpClientResponse.getStatusCode();
        String message = "";
        Object result = null;
        // 将G0返回的code、message、result进行封装直接返回给前端
        if (StringUtils.isNotEmpty(httpClientResponse.getResponseBody())) {
            JSONObject responseBodyObject = JSONObject.parseObject(httpClientResponse.getResponseBody());
            code = httpClientResponse.getStatusCode();
            if (HttpStatus.SC_OK != code) {
                message = responseBodyObject.getString("Message");
            }
            result = responseBodyObject;
        }
        return new ResultWithMessage(code, message, result);
    }

    public ResultWithMessage accessG0InDifferentEnv(String g0Url, Map<String, String> params, String methodType, String body, Map<String, String> headers) throws Exception {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Content-Type", HttpClientUtil.DEFAULT_CONTENT_TYPE);
        headers.put("X-163-AcceptLanguage", "zh");

        StringBuilder paramsBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }

        HttpClientResponse httpClientResponse;
        try {
            httpClientResponse = HttpClientUtil.httpRequest(methodType, g0Url + "?" + paramsBuilder.substring(1), body, headers);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("请求G0网关失败,{}", e.getMessage());
            throw AbnormalStatusCodeException.createAbnormalStatusCodeException("请求网关失败，请稍后重试", 400);
        }

        return convertResponseG0(httpClientResponse);
    }

    public ResultWithMessage accessG0InDifferentEnv(String g0Url, String methodType, String body, Map<String, String> headers) throws Exception {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(HttpHeaders.CONTENT_TYPE, HttpClientUtil.DEFAULT_CONTENT_TYPE);
        headers.put("X-163-AcceptLanguage", "zh");

        HttpClientResponse httpClientResponse = HttpClientUtil.httpRequest(methodType, g0Url, body, headers);

        return convertResponseG0(httpClientResponse);
    }

    public String apiReturnSuccess(Map<String, Object> params) {
        ErrorCode errorCode = CommonErrorCode.Success;
        return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), params);
    }

    public String apiReturnSuccess(Object object) {
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, object);
        ErrorCode errorCode = CommonErrorCode.Success;
        return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), result);
    }

    public String apiReturn(AbstractErrorCode errorCode, Map<String, Object> params) {
        return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), params);
    }

    public static class ResultWithMessage {
        private final int code;
        private final String message;
        private final Object result;

        public ResultWithMessage(int code, String message) {
            this(code, message, null);
        }

        public ResultWithMessage(int code, String message, Object result) {
            this.message = message;
            this.code = code;
            this.result = result;
        }

        public String getMessage() {
            return message;
        }

        public int getCode() {
            return code;
        }

        public Object getResult() {
            return result;
        }
    }

    /**
     * 使用{@link ResultCall}时, 如果有直接指定返回字符串内容的需求, 使用该类, 响应body将与data内容完全一致
     */
    public static class RawStringResult {
        private static final String RAW_STRING_RESULT_KEY = "##raw_string";
        private String body;
        private int code;
        private HttpHeaders headers;

        public RawStringResult(int code, String body, HttpHeaders headers) {
            this.body = body;
            this.code = code;
            this.headers = Optional.fromNullable(headers).or(getDefaultHeaders());
        }

        public RawStringResult(int code, String body) {
            this(code, body, null);
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public HttpHeaders getHeaders() {
            return headers;
        }

        public void setHeaders(HttpHeaders headers) {
            this.headers = headers;
        }
    }

    /**
     * 可能抛出异常, 用于封装服务的整个调用过程, 如果get()返回以下类型的对象, 则会特殊处理:
     * 1. {@link RawStringResult}: 直接将{@link RawStringResult}.data中的内容写入输出流
     * 2. {@link ResultWithMessage}: 指定返回内容的"message"字段, 不设置结果
     */
    public abstract static class ResultCall {
        private boolean alarmed = false;

        public boolean isAlarmed() {
            return alarmed;
        }

        public void setAlarmed(boolean alarmed) {
            this.alarmed = alarmed;
        }

        abstract public Object get() throws Exception;
    }
}
