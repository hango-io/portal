package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/21
 **/
public class OpenApiParser extends BaseParser implements Parser {
    // 包括hostname port uri
    private static final String URI = "uri";
    // 可选，强制要求method的类型
    private static final String METHOD = "method";

    // 是否将body返回结果自动转换为string类型（原生为stream类型），默认为true
    private static final String CONVERT_BODY_TO_STRING = "convertBodyToString";
    // 可选，是否将流程中的异常直接返回给调用方，默认为false
    private static final String TRANSFER_EXCEPTION = "transferException";
    // 可选，如果流程执行失败，responseBody是否包含异常堆栈信息，默认为true
    private static final String MUTE_EXCEPTION = "muteException";

    @Override
    public void parse(Element parent, StepNode node, ParserContext parserContext) {
        String uri = node.get().getProperty().strictGet(URI, String.class);
        String method = node.get().getProperty().get(METHOD, String.class);
        Boolean convertBodyToString = node.get().getProperty().getOrDefault(CONVERT_BODY_TO_STRING, Boolean.class, Boolean.TRUE);

        // from
        Map<String, Object> uriOptions = new HashMap<>();
        if (Objects.nonNull(method)) {
            uriOptions.put("httpMethodRestrict", method);
        }
        String host = parserContext.getParserOptions().getOpenApiHost();
        Integer port = parserContext.getParserOptions().getOpenApiPort();
        uri = String.format("jetty:http://%s:%s/%s", host, port, processUri(uri));

        Boolean transferException = node.get().getProperty().getOrDefault(TRANSFER_EXCEPTION, Boolean.class, Boolean.FALSE);
        Boolean muteException = node.get().getProperty().getOrDefault(MUTE_EXCEPTION, Boolean.class, Boolean.TRUE);

        uriOptions.put("transferException", transferException);
        uriOptions.put("muteException", muteException);
        uri = appendParametersToURI(uri, uriOptions);

        Element from = addElement(parent, node, "from");
        from.addAttribute("uri", uri);

        // convertBodyTo
        if (convertBodyToString) {
            Element convertBodyTo = addElement(parent, node, "convertBodyTo");
            convertBodyTo.addAttribute("type", "java.lang.String");
        }

        parseNext(parent, node, parserContext);
    }

    private String processUri(String uri) {
        // 删除掉/前缀
        if (StringUtils.startsWith(uri, "/")) {
            return StringUtils.substringAfter(uri, "/");
        } else {
            throw new RuntimeException(String.format("The URI parameter [%s] must start with /.", uri));
        }
    }
}
