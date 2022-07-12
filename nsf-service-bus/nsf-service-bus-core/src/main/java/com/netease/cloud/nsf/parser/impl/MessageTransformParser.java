package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import org.dom4j.Element;

import java.util.Objects;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/21
 **/
public class MessageTransformParser extends BaseParser implements Parser {

    // Property
    // 可选setHeader、setProperty、setBody
    private static final String METHOD = "method";
    // method为setHeader、setProperty时需要配置
    private static final String KEY = "key";
    // 可选simple、constant、jsonpath、xpath、xquery
    private static final String EXPRESSION_TYPE = "expressionType";
    // 具体的表达式
    private static final String EXPRESSION = "expression";

    @Override
    public void parse(Element parent, StepNode step, ParserContext parserContext) {
        String method = step.get().getProperty().strictGet(METHOD, String.class);
        String expressionType = step.get().getProperty().strictGet(EXPRESSION_TYPE, String.class);
        String name = step.get().getProperty().get(KEY, String.class);
        String value = step.get().getProperty().strictGet(EXPRESSION, String.class);
        Element methodElement = addElement(parent, step, method);
        if ("setHeader".equalsIgnoreCase(method) && Objects.nonNull(name)) {
            methodElement.addAttribute("headerName", name);
        } else if ("setProperty".equalsIgnoreCase(method) && Objects.nonNull(name)) {
            methodElement.addAttribute("propertyName", name);
        } else if (Objects.nonNull(name)) {
            methodElement.addAttribute("name", name);
        }
        Element expressionTypeElement = addElement(methodElement, step, expressionType);
        expressionTypeElement.setText(value);
        parseNext(parent, step, parserContext);
    }
}
