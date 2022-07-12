package org.hango.cloud.dashboard.apiserver.util;


import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hango.cloud.dashboard.apiserver.exception.AbnormalStatusCodeException;
import org.hango.cloud.dashboard.apiserver.exception.HostUnReachableException;
import org.hango.cloud.dashboard.apiserver.exception.NetworkUnReachableException;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    public static final String HEAD_METHOD = "HEAD";
    public static final String GET_METHOD = "GET";
    public static final String PUT_METHOD = "PUT";
    public static final String POST_METHOD = "POST";
    public static final String DELETE_METHOD = "DELETE";
    public static final String OPTIONS_METHOD = "OPTIONS";
    public static final String DEFAULT_ENCODING = "utf-8";
    public static final String DEFAULT_CONTENT_TYPE = "application/json";
    public static final int MAX_LOG_INFO_LENGTH = 6144;
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final Integer MAX_TIME_OUT = 5000; // 连接超时时间。
    private static final Integer MAX_SO_TIME_OUT = 60000;// 读取的超时时间。
    private static final Integer MAX_IDLE_TIME_OUT = 10000;// 空闲连接关闭时间。
    private static final Integer MAX_CONN = 2000;// 整个连接管理器的最大连接数。
    private static final Integer MAX_CONN_PER_HOST = 100;// 每个目标主机的最大连接数。
    // HttpClient初始化设置。
    private static HttpClient httpClient = null;
    private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
    private static HttpClientParams httpClientParams = new HttpClientParams();

    static {
        Protocol easyhttps = new Protocol("https", new TLS12HttpsSocketFactory(), 443);
        Protocol.registerProtocol("https", easyhttps);
        HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
        httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(MAX_CONN_PER_HOST);
        httpConnectionManagerParams.setMaxTotalConnections(MAX_CONN);
        httpConnectionManagerParams.setConnectionTimeout(MAX_TIME_OUT);
        httpConnectionManagerParams.setSoTimeout(MAX_SO_TIME_OUT);
        connectionManager.setParams(httpConnectionManagerParams);
        httpClientParams.setParameter("http.connection-manager.timeout", MAX_TIME_OUT.longValue());
        httpClient = new HttpClient(httpClientParams, connectionManager);
    }

    public static HttpClientResponse httpRequestWithFormData(String methodType, String url, Map<String, Object> body, Map<String, String> headerMap)
            throws HttpException, HostUnReachableException {
        StringBuilder sbBody = new StringBuilder();
        if (body != null) {
            for (Entry<String, Object> entry : body.entrySet()) {
                sbBody.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        if (headerMap == null) {
            headerMap = new HashMap<>();
        }
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        return httpRequest(methodType, url, sbBody.toString(), headerMap, true);
    }

    public static HttpClientResponse httpRequest(String methodType, String url, String body, Map<String, String> headerMap) throws HttpException,
            HostUnReachableException {
        return httpRequest(methodType, url, body, headerMap, true);
    }


    public static HttpClientResponse httpRequestBytes(String methodType, String url, String body, Map<String, String> headerMap) throws HttpException,
            HostUnReachableException {
        return httpRequestBytes(methodType, url, body, headerMap, true);
    }

    /**
     * 接受String类型的请求体，向目标url发起Http请求。
     *
     * @param methodType 请求方法
     * @param url        目标地址
     * @param body       请求体
     * @param headerMap  请求头
     * @return HttpClientResponseEntry
     * @throws HttpException
     * @throws HostUnReachableException
     */
    @SuppressWarnings("rawtypes")
    public static HttpClientResponse httpRequest(String methodType, String url, String body, Map<String, String> headerMap, boolean logable)
            throws HttpException, HostUnReachableException {

        HttpMethodBase method = null;
        if (methodType.equals(HEAD_METHOD)) {
            method = new HeadMethod(url);
        } else if (methodType.equals(DELETE_METHOD)) {
            method = new DeleteMethod(url);
        } else if (methodType.equals(POST_METHOD)) {
            method = new PostMethod(url);
        } else if (methodType.equals(PUT_METHOD)) {
            method = new PutMethod(url);
        } else {
            method = new GetMethod(url);
        }

        try {
            // set http headers
            if (headerMap != null) {
                for (Object o : headerMap.entrySet()) {
                    Entry entry = (Entry) o;
                    String headerName = (String) entry.getKey();
                    String headerValue = (String) entry.getValue();
                    if (headerName.equalsIgnoreCase("host")) {
                        method.getParams().setVirtualHost(headerValue);
                        continue;
                    }
                    method.setRequestHeader(new Header(headerName, headerValue));
                }
            }

            // set http body
            if (!isNoRequestBodyMethod(methodType) && body != null) {
                ((EntityEnclosingMethod) method).setRequestEntity(new StringRequestEntity(body, null, DEFAULT_ENCODING));
            }

            if (logable) {
                logger.info(" ReqURL: " + hidePassword(url));
                logger.info(" ReqMethod: " + methodType);
                if (headerMap != null) {
                    for (String header : headerMap.keySet()) {
                        logger.info(" ReqHeader: " + header + " = " + headerMap.get(header));
                    }
                }
                logger.info(" ReqBody: " + hidePassword(body));
            }
            long startTime = System.currentTimeMillis();
            httpClient.executeMethod(method);
            long endTime = System.currentTimeMillis();
            int statusCode = method.getStatusCode();

            List<Header> headerList = new ArrayList<>();
            Header[] repsonseHeaders = method.getResponseHeaders();
            for (Header header : repsonseHeaders) {
                headerList.add(header);
            }
            String responseBody = parseResponseBody(method, DEFAULT_ENCODING);

            if (logable) {
                logger.info(" RespCode: " + statusCode);
                logger.info(" RespBody(startTime:" + startTime + ",endTime:" + endTime
                        + ",elapse:" + (endTime - startTime) + "ms) Body: " + hidePassword(responseBody));
            }

            return new HttpClientResponse(statusCode, headerList, responseBody);
        } catch (HttpException e) {
            logger.warn("httpclient HttpException!", e);
            throw new HttpException("URL" + url);
        } catch (IOException e) {
            logger.warn("httpclient IOException!", e);
            throw new HostUnReachableException("URL: " + url);
        } finally {
            method.releaseConnection();
            connectionManager.closeIdleConnections(MAX_IDLE_TIME_OUT);
        }
    }


    @SuppressWarnings("rawtypes")
    public static HttpClientResponse httpRequestBytes(String methodType, String url, String body, Map<String, String> headerMap, boolean logable)
            throws HttpException, HostUnReachableException {

        HttpMethodBase method = null;
        if (methodType.equals(HEAD_METHOD)) {
            method = new HeadMethod(url);
        } else if (methodType.equals(DELETE_METHOD)) {
            method = new DeleteMethod(url);
        } else if (methodType.equals(POST_METHOD)) {
            method = new PostMethod(url);
        } else if (methodType.equals(PUT_METHOD)) {
            method = new PutMethod(url);
        } else {
            method = new GetMethod(url);
        }

        try {

            // set http body
            boolean contentLenFlag = false;
            if (!isNoRequestBodyMethod(methodType) && body != null) {
                ((EntityEnclosingMethod) method).setRequestEntity(new StringRequestEntity(body, null, DEFAULT_ENCODING));
                contentLenFlag = true;
            }

            // set http headers
            if (headerMap != null) {
                for (Object o : headerMap.entrySet()) {
                    Entry entry = (Entry) o;
                    String headerName = (String) entry.getKey();
                    String headerValue = (String) entry.getValue();
                    if (headerName.equalsIgnoreCase("host")) {
                        method.getParams().setVirtualHost(headerValue);
                        continue;
                    }
                    if (!contentLenFlag && headerName.equalsIgnoreCase("content-length")) {
                        continue;
                    }
                    method.setRequestHeader(new Header(headerName, headerValue));
                }
            }

            if (logable) {
                logger.info(" ReqURL: " + hidePassword(url));
                logger.info(" ReqMethod: " + methodType);
                if (headerMap != null) {
                    for (String header : headerMap.keySet()) {
                        logger.info(" ReqHeader: " + header + " = " + headerMap.get(header));
                    }
                }
                logger.info(" ReqBody: " + hidePassword(body));
            }
            long startTime = System.currentTimeMillis();
            httpClient.executeMethod(method);
            long endTime = System.currentTimeMillis();
            int statusCode = method.getStatusCode();

            List<Header> headerList = new ArrayList<>();
            Header[] repsonseHeaders = method.getResponseHeaders();
            for (Header header : repsonseHeaders) {
                headerList.add(header);
            }
            byte[] bodyContent = method.getResponseBody();

            if (logable) {
                logger.info(" RespCode: " + statusCode);
                if (bodyContent != null) {
                    logger.info(" RespBody(startTime:" + startTime + ",endTime:" + endTime
                            + ",elapse:" + (endTime - startTime) + "ms) Body: " + Arrays.toString(bodyContent));
                } else {
                    logger.info(" RespBody(startTime:" + startTime + ",endTime:" + endTime
                            + ",elapse:" + (endTime - startTime) + "ms) Body: null");
                }
            }

            return new HttpClientResponse(statusCode, headerList, bodyContent);
        } catch (HttpException e) {
            logger.warn("httpclient HttpException!", e);
            throw new HttpException("URL" + url);
        } catch (IOException e) {
            logger.warn("httpclient IOException!", e);
            throw new HostUnReachableException("URL: " + url);
        } finally {
            method.releaseConnection();
            connectionManager.closeIdleConnections(MAX_IDLE_TIME_OUT);
        }
    }

    /**
     * 将后端服务器返回的值转换成String类型。
     *
     * @param method
     * @param encoding
     * @return
     */
    private static String parseResponseBody(HttpMethodBase method, String encoding) {
        InputStream input = null;
        try {
            input = method.getResponseBodyAsStream();
            if (input == null) {
                return null;
            }
            return IOUtils.toString(input, encoding);
        } catch (Exception e) {
            logger.info("get httpclient response error.", e);
        } finally {
            IOUtils.closeQuietly(input);
        }
        return null;
    }

    /**
     * 判断请求方法是否是GET\HEAD\DELETE\OPTIONS等不带请求体的方法。
     *
     * @param methodType
     * @return
     */
    public static boolean isNoRequestBodyMethod(String methodType) {
        return methodType.equalsIgnoreCase(GET_METHOD) || methodType.equalsIgnoreCase(HEAD_METHOD) || methodType.equalsIgnoreCase(DELETE_METHOD)
                || methodType.equalsIgnoreCase(OPTIONS_METHOD);
    }

    public static String composeQueryString(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            sb.append("&").append(key).append("=").append(params.get(key));
        }
        return sb.toString();
    }

    public static void download(String filePath, String url) {
        StopWatch watch = new StopWatch();
        watch.start();
        HttpMethodBase method = null;
        try {
            method = new GetMethod(url);
            httpClient.executeMethod(method);
            int statusCode = method.getStatusCode();
            InputStream input = method.getResponseBodyAsStream();
            int byteread = 0;
            FileOutputStream fs = null;
            try {
                fs = new FileOutputStream(filePath);
                byte[] buffer = new byte[input.available()];
                while ((byteread = input.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
            } catch (Exception e) {
                logger.error("download file error from NOS: {}", e);
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(fs);
            }
        } catch (Exception ex) {
            logger.error(" fetch request error", ex);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
            watch.stop();
            if (watch.getTime() >= 50000) {
                logger.error("fetch url " + url + ",consume: " + watch.getTime());
            }
        }
    }

    private static String hidePassword(String src) {
        if (StringUtils.isBlank(src)) {
            return src;
        }
        if (src.length() > MAX_LOG_INFO_LENGTH) {
            return "...(length = " + src.length() + ")";
        }
        return src.replaceAll("\"password\":\"([^\\s]+)\"", "\"password\":\"******\"").replaceAll("password=\\w{32}",
                "password=******");
    }

    /**
     * 接受byte[]类型的请求体，向目标url发起Http请求。
     *
     * @param methodType 请求方法
     * @param url        目标地址
     * @param body       请求体
     * @param headerMap  请求头
     * @return HttpClientResponseEntry
     * @throws HttpException
     * @throws NetworkUnReachableException
     */
    public static HttpClientResponse httpRequestBytes(String methodType, String url, byte[] body,
                                                      Map<String, String> headerMap) throws HttpException, NetworkUnReachableException {

        // logger.debug("httpRequest invoked " + url.hashCode());
        // HttpClient httpClient = new HttpClient();
        HttpClient httpClient = new HttpClient(httpClientParams, connectionManager);

        HttpMethodBase method = null;
        if (methodType.equals(Const.GET_METHOD)) {
            method = new GetMethod(url);
        } else if (methodType.equals(Const.HEAD_METHOD)) {
            method = new HeadMethod(url);
        } else if (methodType.equals(Const.DELETE_METHOD)) {
            method = new DeleteMethod(url);
        } else if (methodType.equals(Const.POST_METHOD)) {
            method = new PostMethod(url);
        } else if (methodType.equals(Const.PUT_METHOD)) {
            method = new PutMethod(url);
        }
        if (method != null) {
            try {
                // 设置HTTP Headers
                for (Object o : headerMap.entrySet()) {
                    Entry entry = (Entry) o;
                    String headerName = (String) entry.getKey();
                    String headerValue = (String) entry.getValue();
                    if (headerName.equalsIgnoreCase("content-length")) {
                        continue;
                    }
                    if (headerName.equalsIgnoreCase("host")) { // support nos
                        // virtual
                        // server
                        method.getParams().setVirtualHost(headerValue);
                        continue;
                    }
                    method.setRequestHeader(new Header(headerName, headerValue));
                }

                // 设置请求体
                if (!isNoRequestBodyMethod(methodType)) {
                    ((EntityEnclosingMethod) method).setRequestEntity(new ByteArrayRequestEntity(body));
                }

                // 非单例。
                // logger.debug("before executeMethod " + url.hashCode());
                // HostConfiguration hostConfig = new HostConfiguration();
                // hostConfig.setHost("www.xxx.com", 80,
                // Protocol.getProtocol("http"));
                long startTime = System.currentTimeMillis();
                httpClient.executeMethod(method);
                logger.debug("API调用耗时：" + (System.currentTimeMillis() - startTime));
                // logger.debug("after executeMethod " + url.hashCode());
                int statusCode = method.getStatusCode();

                // 分析后端返回的响应头。
                List<Header> headerList = new ArrayList<>();
                Header[] repsonseHeaders = method.getResponseHeaders();
                for (Header header : repsonseHeaders) {
                    String headerName = header.getName();
                    if (headerName.equalsIgnoreCase("Transfer-Encoding")
                            || headerName.equalsIgnoreCase("Content-Length")) { // 过滤Transfer-Encoding和Content-Length信息。
                        continue;
                    }
                    headerList.add(header);
                }
                // String responseBody = parseResponseBody(method,
                // Const.DEFAULT_ENCODING);
                byte[] responseByteBody = parseResponseByteBody(method);
                // logger.debug("after parse body " + url.hashCode());

                // DEBUG INFO
                String logRequestBody = ((body != null && body.length != 0) ? (body.length > 4096 ? "内容太大，不予显示。"
                        : new String(body, Const.DEFAULT_ENCODING)).replaceAll("\"password\":\"([^\\s]+)\"",
                        "\"password\":\"******\"").replaceAll("[\r|\n]", " ") : "null"); // 密码信息在日志中隐藏。
                String logResponseBody = (responseByteBody != null ? new String(responseByteBody,
                        Const.DEFAULT_ENCODING).replaceAll("[\r|\n]", " ") : "null");
                logger.info("====DEBUG INFO====HttpClient RequestURL: " + url);
                logger.info("====DEBUG INFO====HttpClient RequestBody: " + logRequestBody);
                logger.info("====DEBUG INFO====HttpClient RequestHeaderMap: " + headerMap);
                logger.info("====DEBUG INFO====HttpClient ResponseBody: " + logResponseBody);
                logger.info("====DEBUG INFO====HttpClient headerMap: " + headerList);
                return new HttpClientResponse(statusCode, headerList, responseByteBody);
            } catch (HttpException e) {
                e.printStackTrace();
                throw new HttpException("URL" + url);
            } catch (IOException e) {
                e.printStackTrace();
                throw new NetworkUnReachableException("URL: " + url);
            } finally {
                method.releaseConnection();
            }
        }
        return null;
    }

    /**
     * 将后端服务器返回的值转换成byte[]类型。
     *
     * @param method
     * @return
     */
    private static byte[] parseResponseByteBody(HttpMethodBase method) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] byteBody = null;
        try {
            InputStream in = method.getResponseBodyAsStream();
            if (in == null) {
                return null;
            }
            int ch;
            while ((ch = in.read()) != -1) {
                byteStream.write(ch);
            }
            byteBody = byteStream.toByteArray();
            byteStream.close();
        } catch (IOException e) {
            method.releaseConnection();
            logger.info("解析后端服务器返回值出错。" + e.toString());
        }
        return byteBody;
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

    public static void checkRespCode(HttpClientResponse httpResponseEntry) {
        int code = httpResponseEntry.getStatusCode();
        if (!HttpMisc.isNormalCode(code)) {
            String body = httpResponseEntry.getResponseBody();
            logger.error("非正常响应, body: {}, ", body);
            throw AbnormalStatusCodeException.createAbnormalStatusCodeException(body, code);
        }
    }

    public static void checkRespCodeSilently(HttpClientResponse httpResponseEntry) {
        int code = httpResponseEntry.getStatusCode();
        if (!HttpMisc.isNormalCode(code)) {
            String body = httpResponseEntry.getResponseBody();
            logger.warn("非正常响应, body: {}, ", body);
        }
    }

    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
