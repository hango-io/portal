package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/27
 **/
public class HttpParser extends BaseParser implements Parser {

    // 包含host、port、path
    private static final String URI = "uri";
    private static final String PROTOCOL = "protocol";
    private static final String METHOD = "method";
    private static final String HEADERS = "headers";
    private static final String QUERY_PARAMS = "queryParams";
    private static final String BODY = "body";

    // 是否将body返回结果自动转换为string类型（原生为stream类型），默认为true
    private static final String CONVERT_BODY_TO_STRING = "convertBodyToString";
    // 是否保留上下文中的headers，默认为true
    private static final String COPY_HEADERS = "copyHeaders";
    // 是否在返回码错误时抛出异常，默认为false
    private static final String THROW_EXCEPTION_ON_FAILURE = "throwExceptionOnFailure";
    // 是否清理与请求无关的headers，默认为true
    private static final String CLEAR_HEADERS = "clearHeaders";

    // timeout，单位为ms
    private static final String CONNECTION_REQUEST_TIMEOUT = "connectionRequestTimeout";
    private static final String CONNECTION_TIMEOUT = "connectTimeout";
    private static final String SOCKET_TIMEOUT = "socketTimeout";

    @Override
    public void parse(Element parent, StepNode step, ParserContext parserContext) {
        String uri = step.get().getProperty().strictGet(URI, String.class);
        String method = step.get().getProperty().strictGet(METHOD, String.class);
        String protocol = step.get().getProperty().getOrDefault(PROTOCOL, String.class, "http");

        List headers = step.get().getProperty().get(HEADERS, List.class);
        List params = step.get().getProperty().get(QUERY_PARAMS, List.class);
        String body = step.get().getProperty().get(BODY, String.class);

        Boolean convertBodyToString = step.get().getProperty().getOrDefault(CONVERT_BODY_TO_STRING, Boolean.class, Boolean.TRUE);
        Boolean copyHeaders = step.get().getProperty().getOrDefault(COPY_HEADERS, Boolean.class, Boolean.TRUE);
        Boolean throwExceptionOnFailure = step.get().getProperty().getOrDefault(THROW_EXCEPTION_ON_FAILURE, Boolean.class, Boolean.FALSE);
        Boolean clearHeaders = step.get().getProperty().getOrDefault(CLEAR_HEADERS, Boolean.class, Boolean.TRUE);

        // 连接请求超时默认5s
        Long connectionRequestTimeout = step.get().getProperty().getOrDefault(CONNECTION_REQUEST_TIMEOUT, Long.class, 5000L);
        // 连接超时默认30s
        Long connectTimeout = step.get().getProperty().getOrDefault(CONNECTION_TIMEOUT, Long.class, 30000L);
        // socket超时默认30s
        Long socketTimeout = step.get().getProperty().getOrDefault(SOCKET_TIMEOUT, Long.class, 30000L);

        Map<String, Object> uriOptions = new HashMap<>();
        uriOptions.put("copyHeaders", String.valueOf(copyHeaders));
        uriOptions.put("throwExceptionOnFailure", String.valueOf(throwExceptionOnFailure));

        uriOptions.put("connectionRequestTimeout", String.valueOf(connectionRequestTimeout));
        uriOptions.put("connectTimeout", String.valueOf(connectTimeout));
        uriOptions.put("socketTimeout", String.valueOf(socketTimeout));

        uri = uriPostProcess(uri);

        if (clearHeaders) {
            removeHeaders(parent, step, "CamelHttp*");
        }
        if (Objects.nonNull(headers) && headers.size() != 0) {
            Map<String, Object> headerMap = convertHeaderOrQuery(headers);
            for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                setSimpleHeader(parent, step, entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        if (Objects.nonNull(params) && params.size() != 0) {
            Map<String, Object> queryMap = convertHeaderOrQuery(params);
            String queryString = createQueryStringWithoutEscape(queryMap);
            setSimpleHeader(parent, step, Exchange.HTTP_QUERY, queryString);
        }
        if (StringUtils.isNotBlank(body)) {
            setSimpleBody(parent, step, body);
        }
        setHeader(parent, step, Exchange.HTTP_METHOD, method);

        uri = String.format("%s4://%s",protocol, uri);
        uri = appendParametersToURI(uri, uriOptions);

        Element to = addElement(parent, step, "to");
        to.addAttribute("uri", uri);

        if (convertBodyToString) {
            Element convertBodyTo = addElement(parent, step, "convertBodyTo");
            convertBodyTo.addAttribute("type", "java.lang.String");
        }

        parseNext(parent, step, parserContext);

        //todo: 调用外部服务失败时，返回异常stack
    }

    private String uriPostProcess(String uri) {
        if (StringUtils.startsWith(uri, "http://")) {
            uri = StringUtils.substringAfter(uri, "http://");
        }
        if (StringUtils.startsWith(uri, "https://")) {
            uri = StringUtils.substringAfter(uri, "https://");
        }
        if (StringUtils.contains(uri, "?")) {
            throw new RuntimeException(String.format("The URI parameter [%s] cannot contain querystring.", uri));
        }
        return uri;
    }

    private Map<String, Object> convertHeaderOrQuery(List<Map<String, Object>> objectList) {
        Map<String, Object> ret = new HashMap<>();
        if (Objects.nonNull(objectList)) {
            for (Map<String, Object> item : objectList) {
                Object key = item.get("key");
                Object value = item.get("value");
                ret.put(String.valueOf(key), value);
            }
        }
        return ret;
    }

    private String createQueryStringWithoutEscape(Map<String, Object> params) {
        if (Objects.isNull(params)) {
            return StringUtils.EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> item : params.entrySet()) {
            builder.append("&").append(item.getKey()).append("=").append(item.getValue());
        }
        return StringUtils.substringAfter(builder.toString(), "&");
    }
}
