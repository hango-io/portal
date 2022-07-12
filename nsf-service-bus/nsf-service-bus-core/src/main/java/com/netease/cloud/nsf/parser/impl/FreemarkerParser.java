package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import org.apache.camel.component.freemarker.FreemarkerConstants;
import org.dom4j.Element;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/27
 **/
public class FreemarkerParser extends BaseParser implements Parser {
    /* 必填项 */
    private static final String TEMPLATE = "template";

    @Override
    public void parse(Element parent, StepNode step, ParserContext parserContext) {
        String template = step.get().getProperty().strictGet(TEMPLATE, String.class);
        setHeader(parent, step, FreemarkerConstants.FREEMARKER_TEMPLATE, template);

        // 自动生成template name
        String name = String.format("%s_template", step.get().getId());
        String uri = String.format("freemarker:%s?allowTemplateFromHeader=true", name);
        Element to = addElement(parent, step, "to");
        to.addAttribute("uri", uri);
        parseNext(parent, step, parserContext);
    }
}
