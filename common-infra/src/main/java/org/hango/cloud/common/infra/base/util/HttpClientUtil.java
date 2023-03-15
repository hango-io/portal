package org.hango.cloud.common.infra.base.util;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.LogTraceUUIDHolder;
import org.hango.cloud.common.infra.base.meta.ActionPair;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * HTTP请求工具类
 *
 * @author Feng Changjian (hzfengchj@corp.netease.com)
 * @version $Id: HttpClientUtil.java, v 1.0 2013-8-2 下午03:36:54
 */
public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final CloseableHttpClient CLIENT = getHttpClient();


    /**
     * 自签名
     *
     * @return SSLContext
     */
    private static SSLContext createIgnoreVerifySsl() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLS");
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) {
                // 未使用，无需实现
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) {
                // 未使用，无需实现
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }


    public static Map<String, Object> defaultQuery(ActionPair actionPair) {
        Map<String, Object> query = Maps.newHashMap();
        query.put(BaseConst.ACTION, actionPair.getAction());
        query.put(BaseConst.VERSION, actionPair.getVersion());
        return query;
    }

    /**
     * 创建httpclient
     *
     * @return CloseableHttpClient
     */
    private static CloseableHttpClient getHttpClient() {
        CloseableHttpClient client = null;
        try {
            //采用绕过验证的方式处理https请求
            SSLContext sslcontext = createIgnoreVerifySsl();
            //设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(BaseConst.SCHEME_HTTP, PlainConnectionSocketFactory.INSTANCE)
                    .register(BaseConst.SCHEME_HTTPS, new SSLConnectionSocketFactory(sslcontext))
                    .build();
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(BaseConst.MAX_SO_TIME_OUT)
                    .setConnectTimeout(BaseConst.MAX_TIME_OUT)
                    .build();
            //创建自定义的httpclient对象
            client = HttpClients.custom().setConnectionManager(connManager)
                    .setMaxConnTotal(BaseConst.MAX_CONN)
                    .setMaxConnPerRoute(BaseConst.MAX_CONN_PER_HOST)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        } catch (Exception e) {
            logger.error("CloseableHttpClient create error, error message is {}", e.getMessage());
        }
        return client;
    }

    /**
     * 发送HTTP请求
     *
     * @param request 请求信息
     * @param module  访问模块
     * @return HttpClientResponse
     */
    public static HttpClientResponse httpRequest(HttpUriRequest request, String module) {
        String errMessage;
        try (CloseableHttpResponse execute = CLIENT.execute(request)) {
            String responseBody = EntityUtils.toString(execute.getEntity());
            Header contentLength = execute.getLastHeader(HttpHeaders.CONTENT_LENGTH);
            boolean respLogFlag = true;
            if (contentLength != null && NumberUtils.toInt(contentLength.getValue()) > BaseConst.MAX_LOG_INFO_LENGTH) {
                respLogFlag = false;
            }
            if (respLogFlag) {
                logger.info("Response Info is {}", responseBody);
            }
            return new HttpClientResponse(execute.getStatusLine().getStatusCode(), Arrays.asList(execute.getAllHeaders()), responseBody);
        } catch (IOException e) {
            errMessage = e.getMessage();
            logger.warn("Request Module {} Failed ! error message is {}", module, errMessage);
            e.printStackTrace();

        } catch (Throwable e) {
            errMessage = e.getMessage();
            logger.warn("Request Module {} Failed ! error message is {}", module, errMessage);
            e.printStackTrace();

        }
        return new HttpClientResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, Collections.emptyList(), errMessage);
    }


    /**
     * 接受String类型的请求体，向目标url发起Http请求。
     *
     * @param methodType 请求方法
     * @param url        目标地址
     * @param body       请求体
     * @param headers    请求头
     * @param module     请求方模块
     * @param queryMap   query string
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse httpRequest(String methodType, String url, String body, Map<String, Object> queryMap, HttpHeaders headers, String module) {
        Assert.notNull(methodType, "methodType must not be null");
        HttpRequestBase request = getRequestByMethod(methodType);
        try {
            if (!CollectionUtils.isEmpty(queryMap)) {
                String conStr = StringUtils.lastIndexOf(url, BaseConst.SYMBOL_QUESTION_MARK) == NumberUtils.INTEGER_MINUS_ONE ? BaseConst.SYMBOL_QUESTION_MARK : BaseConst.SYMBOL_AND;
                StringBuilder paramsBuilder = new StringBuilder(conStr);
                for (Entry<String, Object> entry : queryMap.entrySet()) {
                    String value = entry.getValue() == null ? StringUtils.EMPTY : String.valueOf(entry.getValue());
                    paramsBuilder.append(entry.getKey()).append(BaseConst.SYMBOL_EQUAL).append(URLEncoder.encode(value, Charset.defaultCharset().name())).append(BaseConst.SYMBOL_AND);
                }
                url += paramsBuilder.substring(0, paramsBuilder.length() - 1);
            }
            logger.info("Request Info is [{}] {} ", methodType, url);
            if (StringUtils.isNotBlank(body) && request instanceof HttpEntityEnclosingRequestBase) {
                logger.info("Request Body is {}",body);
                HttpEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
                ((HttpEntityEnclosingRequestBase) request).setEntity(entity);

            }
            Assert.notNull(request, "invalid request method");
            headers = CollectionUtils.isEmpty(headers) ? new HttpHeaders() : headers;
            MediaType contentType = headers.getContentType();
            if (contentType==null){
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
            Iterator<Entry<String, List<String>>> iterator = headers.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, List<String>> next = iterator.next();
                request.addHeader(next.getKey(), StringUtils.join(next.getValue(), BaseConst.SYMBOL_COMMA));
            }

            logger.info("Request Header : \n {}", headers);
            request.setURI(new URIBuilder(url).setCharset(Charset.defaultCharset()).build());
        } catch (Throwable e) {
            logger.warn("httpclient HttpException!", e);
        }
        return httpRequest(request, module);
    }

    /**
     * 获取请求方法对应Http Request 实现
     *
     * @param methodType 请求方法
     * @return HttpRequestBase
     */
    private static HttpRequestBase getRequestByMethod(String methodType) {
        HttpRequestBase request = null;
        switch (methodType) {
            case HttpGet.METHOD_NAME:
                request = new HttpGet();
                break;
            case HttpPost.METHOD_NAME:
                request = new HttpPost();
                break;
            case HttpPut.METHOD_NAME:
                request = new HttpPut();
                break;
            case HttpDelete.METHOD_NAME:
                request = new HttpDelete();
                break;
            case HttpTrace.METHOD_NAME:
                request = new HttpTrace();
                break;
            case HttpOptions.METHOD_NAME:
                request = new HttpOptions();
                break;
            case HttpHead.METHOD_NAME:
                request = new HttpHead();
                break;
            case HttpPatch.METHOD_NAME:
                request = new HttpPatch();
                break;
            default:
                break;
        }
        return request;
    }


    /**
     * 接受String类型的请求体，向目标url发起Get Http请求。
     *
     * @param url     目标地址
     * @param module  请求方模块
     * @param query   query string
     * @param headers 请求头
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse getRequest(String url, Map<String, Object> query, HttpHeaders headers, String module) {
        return httpRequest(HttpGet.METHOD_NAME, url, null, query, headers, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Get Http请求。
     *
     * @param url    目标地址
     * @param module 请求方模块
     * @param query  query string
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse getRequest(String url, Map<String, Object> query, String module) {
        return getRequest(url, query, null, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Get Http请求。
     *
     * @param url     目标地址
     * @param headers 请求头
     * @param module  请求方模块
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse getRequest(String url, HttpHeaders headers, String module) {
        return getRequest(url, null, headers, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Get Http请求。
     *
     * @param url    目标地址
     * @param module 请求方模块
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse getRequest(String url, String module) {
        return getRequest(url, null, null, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Post Http请求。
     *
     * @param url     目标地址
     * @param module  请求方模块
     * @param query   query string
     * @param body    请求体
     * @param headers 请求头
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse postRequest(String url, String body, Map<String, Object> query, HttpHeaders headers, String module) {
        return httpRequest(HttpPost.METHOD_NAME, url, body, query, headers, module);
    }


    /**
     * 接受String类型的请求体，向目标url发起Post Http请求。
     *
     * @param url    目标地址
     * @param body   请求体
     * @param module 请求方模块
     * @param query  query string
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse postRequest(String url, String body, Map<String, Object> query, String module) {
        return postRequest(url, body, query, null, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Post Http请求。
     *
     * @param url     目标地址
     * @param body    请求体
     * @param module  请求方模块
     * @param headers 请求头
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse postRequest(String url, String body, HttpHeaders headers, String module) {
        return postRequest(url, body, null, headers, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Post Http请求。
     *
     * @param url     目标地址
     * @param module  请求方模块
     * @param query   query string
     * @param headers 请求头
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse postRequest(String url, Map<String, Object> query, HttpHeaders headers, String module) {
        return postRequest(url, null, query, headers, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Post Http请求。
     *
     * @param url    目标地址
     * @param module 请求方模块
     * @param query  query string
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse postRequest(String url, Map<String, Object> query, String module) {
        return postRequest(url, null, query, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Post Http请求。
     *
     * @param url    目标地址
     * @param module 请求方模块
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse postRequest(String url, String module) {
        return postRequest(url, Collections.emptyMap(), module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Put Http请求。
     *
     * @param url     目标地址
     * @param module  请求方模块
     * @param body    请求体
     * @param query   query string
     * @param headers 请求头
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse putRequest(String url, String body, Map<String, Object> query, HttpHeaders headers, String module) {
        return httpRequest(HttpPut.METHOD_NAME, url, body, query, headers, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Put Http请求。
     *
     * @param url     目标地址
     * @param module  请求方模块
     * @param body    请求体
     * @param headers 请求头
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse putRequest(String url, String body, HttpHeaders headers, String module) {
        return putRequest(url, body, null, headers, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Put Http请求。
     *
     * @param url    目标地址
     * @param module 请求方模块
     * @param query  query string
     * @param body   请求体
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse putRequest(String url, String body, Map<String, Object> query, String module) {
        return putRequest(url, body, query, null, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Put Http请求。
     *
     * @param url     目标地址
     * @param module  请求方模块
     * @param query   query string
     * @param headers 请求头
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse putRequest(String url, Map<String, Object> query, HttpHeaders headers, String module) {
        return putRequest(url, null, query, headers, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Put Http请求。
     *
     * @param url    目标地址
     * @param module 请求方模块
     * @param body   请求体
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse putRequest(String url, String body, String module) {
        return putRequest(url, body, null, null, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Put Http请求。
     *
     * @param url    目标地址
     * @param module 请求方模块
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse putRequest(String url, String module) {
        return putRequest(url, null, module);
    }


    /**
     * 接受String类型的请求体，向目标url发起Delete Http请求。
     *
     * @param url     目标地址
     * @param module  请求方模块
     * @param query   query string
     * @param headers 请求头
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse deleteRequest(String url, Map<String, Object> query, HttpHeaders headers, String module) {
        return httpRequest(HttpDelete.METHOD_NAME, url, null, query, headers, module);
    }

    /**
     * 接受String类型的请求体，向目标url发起Delete Http请求。
     *
     * @param url     目标地址
     * @param module  请求方模块
     * @param headers 请求头
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse deleteRequest(String url, HttpHeaders headers, String module) {
        return deleteRequest(url, null, headers, module);
    }


    /**
     * 接受String类型的请求体，向目标url发起Delete Http请求。
     *
     * @param url    目标地址
     * @param module 请求方模块
     * @param query  query string
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse deleteRequest(String url, Map<String, Object> query, String module) {
        return deleteRequest(url, query, null, module);
    }


    /**
     * 接受String类型的请求体，向目标url发起Delete Http请求。
     *
     * @param url    目标地址
     * @param module 请求方模块
     * @return HttpClientResponseEntry
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse deleteRequest(String url, String module) {
        return deleteRequest(url, null, null, module);
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


    public static void sendWithError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MappingJackson2JsonView.DEFAULT_CONTENT_TYPE);
        response.setStatus(errorCode.getStatusCode());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Code", errorCode.getCode());
        map.put("Message", errorCode.getMessage());
        map.put("RequestId", LogTraceUUIDHolder.getUUIDId());

        String responseBody = JSON.toJSONString(map);

        response.getWriter().write(responseBody);
    }

    public static String parseRequestBody(HttpServletRequest request) {
        StringBuffer buffer = new StringBuffer();
        try {
            request.getReader().lines().forEach(buffer::append);
        } catch (Throwable e) {
            logger.warn("request parse failed");
        }
        return buffer.toString();
    }

}
