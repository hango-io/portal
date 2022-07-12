package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.*;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/14
 **/
public class AssertParser extends BaseParser implements Parser {
    private static final String LOG = "log";
    /* 必填项 */
    // 可选range、groovy
    private static final String CONDITION_TYPE = "conditionType";
    // 可选return
    private static final String ACTION = "action";

    /* 可选项 */
    private static final String RANGE = "range";
    private static final String BODY = "body";
    // 需要额外imports的package，多个以逗号分隔开
    private static final String GROOVY_IMPORTS = "groovyImports";
    private static final String SHELL = "shell";
    // 可选successReturn、failureReturn
    private static final String RETURN_TYPE = "returnType";

    @Override
    public void parse(Element parent, StepNode step, ParserContext parserContext) {
        String conditionType = step.get().getProperty().strictGet(CONDITION_TYPE, String.class);
        String action = step.get().getProperty().strictGet(ACTION, String.class);
        String groovyImports = step.get().getProperty().get(GROOVY_IMPORTS, String.class);
        // 如果有需要import的包，在choice之前渲染
        if("groovy".equals(conditionType) && StringUtils.isNotEmpty(groovyImports)){
            setHeader(parent, step, ParserConst.GROOVY_IMPORTS, groovyImports);
        }
        Element choice = addElement(parent, step, "choice");
        Element when = addElement(choice, step, "when");
        Element otherwise = addElement(choice, step, "otherwise");
        if ("range".equals(conditionType)) {
            String range = step.get().getProperty().strictGet(RANGE, String.class);
            String[] rangeBeginEnd = range.split("-");
            Element simple = addElement(when, step, "simple");
            simple.setText(String.format("${header[%s]} range '%s..%s'", Exchange.HTTP_RESPONSE_CODE, rangeBeginEnd[0], rangeBeginEnd[1]));
        }
        if ("groovy".equals(conditionType)) {
            String shell = step.get().getProperty().strictGet(SHELL, String.class);
            Element groovy = addElement(when, step, "groovy");
            groovy.setText(shell);
        }
        if ("return".equals(action)) {
            String body = step.get().getProperty().get(BODY, String.class);
            String returnType = step.get().getProperty().getOrDefault(RETURN_TYPE, String.class, "failureReturn");
            if (StringUtils.isNotBlank(body)) {
                setBody(otherwise, step, body);
            }
            if ("successReturn".equals(returnType)) {
                setHeader(otherwise, step, ParserConst.ON_SUCCESS_TAG, String.format("%s_assert", step.get().getId()));
                Element to = addElement(otherwise, step, "to");
                String onSuccessBean = parserContext.getParserOptions().getOnSuccessBean();
                String uri = String.format("bean:%s", onSuccessBean);
                to.addAttribute("uri", uri);
            }
        }
        parseNext(when, step, parserContext);
    }
}
