package com.netease.cloud.nsf.parser.impl;


import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import org.dom4j.Element;

import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/14
 **/
public class FlowParser extends BaseParser implements Parser {
    /* 必填项 */
    private static final String INTEGRATION_ID = "IntegrationId";

    /* 可选项 */
    // 是否记录trace，默认为true
    private static final String TRACE = "trace";

    @Override
    public void parse(Element parent, StepNode step, ParserContext parserContext) {
        Object id = step.get().getProperty().strictGet(INTEGRATION_ID, Object.class);
        Boolean trace = step.get().getProperty().get(TRACE, Boolean.class);
        setGlobalProperty(step, GLOBAL_INTEGRATION_ID, id);
        Element route = parent.addElement("route");
        route.addAttribute("id", String.valueOf(id));
        if (Objects.nonNull(trace)) {
            route.addAttribute("trace", String.valueOf(trace.booleanValue()));
        }
        parseChild(route, step, parserContext);
        parseNext(parent, step, parserContext);
    }
}
