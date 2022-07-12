package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.*;
import org.dom4j.Element;

import java.util.Objects;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/28
 **/
public class GroovyParser extends BaseParser implements Parser {
    /* 必填项 */
    /**
     * 需要执行的groovy脚本
     * groovy内置的对象有：
     * context(org.apache.camel.CamelContext)
     * camelContext(org.apache.camel.CamelContext)
     * exchange(org.apache.camel.Exchange)
     * request(org.apache.camel.Message)
     * response(org.apache.camel.Message)
     * properties(org.apache.camel.builder.script.PropertiesFunction)
     */
    private static final String GROOVY_SHELL = "groovyShell";

    /* 可选项 */
    /**
     * 需要额外imports的package，多个以逗号分隔开
     */
    private static final String GROOVY_IMPORTS = "groovyImports";

    /**
     * 可选Predicate、Processor、不填
     * 不填默认为Processor
     */
    private static final String GROOVY_MODE = "groovyMode";

    @Override
    public void parse(Element parent, StepNode step, ParserContext parserContext) {
        String groovyShell = step.get().getProperty().strictGet(GROOVY_SHELL, String.class);
        String groovyImports = step.get().getProperty().get(GROOVY_IMPORTS, String.class);
        String groovyMode = step.get().getProperty().getOrDefault(GROOVY_MODE, String.class, "Processor");
        if (Objects.nonNull(groovyImports)) {
            setHeader(parent, step, ParserConst.GROOVY_IMPORTS, groovyImports);
        }
        if ("Predicate".equalsIgnoreCase(groovyMode)) {
            Element groovy = addElement(parent, step, "groovy");
            groovy.setText(groovyShell);
        } else {
            Element script = addElement(parent, step, "script");
            Element groovy = addElement(script, step, "groovy");
            groovy.setText(groovyShell);
        }
        parseNext(parent, step, parserContext);
    }
}
