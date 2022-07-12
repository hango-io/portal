package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import com.netease.cloud.nsf.step.Property;
import org.dom4j.Element;

public class DelayParser extends BaseParser implements Parser {
    // 可选constant、simple
    private static final String DELAY_TYPE = "delayType";
    // 当delayType=constant时，填写delay时长
    private static final String DELAY_DURATION = "delayDuration";
    // 当delayType=simple时，填写simple表达式
    private static final String DELAY_EXPRESSION = "delayExpression";

    @Override
    public void parse(Element parent, StepNode node, ParserContext parserContext) {
        Property property = node.get().getProperty();
        String delayType = property.strictGet(DELAY_TYPE, String.class);
        Element delay = addElement(parent, node, "delay");
        if ("constant".equals(delayType)) {
            Long delayDuration = property.strictGet(DELAY_DURATION, Long.class);
            Element constant = addElement(delay, node, "constant");
            constant.setText(String.valueOf(delayDuration));
        } else if ("simple".equals(delayType)) {
            String delayExpression = property.strictGet(DELAY_EXPRESSION, String.class);
            Element simple = addElement(delay, node, "simple");
            simple.setText(String.valueOf(delayExpression));
        } else {
            throw new RuntimeException(String.format("Unsupported param[%s] value [%s]", DELAY_TYPE, delayType));
        }
        parseNext(parent, node, parserContext);
    }
}
