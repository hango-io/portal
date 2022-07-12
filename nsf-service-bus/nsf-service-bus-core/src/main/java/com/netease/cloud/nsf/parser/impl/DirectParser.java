package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import com.netease.cloud.nsf.step.Property;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

public class DirectParser extends BaseParser implements Parser {
    // 可选from、to，from类型即触发器类型，to类型即连接类型
    private static final String MODE = "mode";
    // 当mode=to时需要填写，目标direct的integration ID
    private static final String DIRECT_INTEGRATION_ID = "directIntegrationId";
    // 可选，If sending a message to a direct endpoint which has no active consumer,
    // then we can tell the producer to block and wait for the consumer to become active. 默认值为true
    private static final String BLOCK = "block";
    // 可选Whether the producer should fail by throwing an exception, when sending to a DIRECT endpoint with no active consumers.默认true
    private static final String FAIL_IF_NO_CONSUMERS = "failIfNoConsumers";

    @Override
    public void parse(Element parent, StepNode node, ParserContext parserContext) {
        Property property = node.get().getProperty();
        String mode = property.strictGet(MODE, String.class);
        Boolean block = property.getOrDefault(BLOCK, Boolean.class, Boolean.FALSE);
        Boolean failIfNoConsumers = property.getOrDefault(FAIL_IF_NO_CONSUMERS, Boolean.class, Boolean.TRUE);

        Map<String, Object> uriOptions = new HashMap<>();
        uriOptions.put("block", block);
        uriOptions.put("failIfNoConsumers", failIfNoConsumers);

        if ("from".equals(mode)) {
            // 当mode为from类型时，endpoint的integrationId为自己当前id
            String integrationId = getIntegrationId(node);
            String uri = buildUri(integrationId);
            uri = appendParametersToURI(uri, uriOptions);

            Element from = addElement(parent, node, "from");
            from.addAttribute("uri", uri);
        } else if ("to".equals(mode)) {
            String directIntegrationId = property.strictGet(DIRECT_INTEGRATION_ID, String.class);
            String uri = buildUri(directIntegrationId);
            uri = appendParametersToURI(uri, uriOptions);

            Element to = addElement(parent, node, "to");
            to.addAttribute("uri", uri);
        } else {
            throw new RuntimeException(String.format("Unsupported param[%s] value [%s]", MODE, mode));
        }

        parseNext(parent, node, parserContext);
    }

    private String buildUri(String directIntegrationId) {
        return String.format("direct:directEndpoint[integrationId=%s]", directIntegrationId);
    }
}
