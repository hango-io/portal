package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import org.dom4j.Element;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/27
 **/
public class TimerParser extends BaseParser implements Parser {

    // timer的name
    private static final String NAME = "name";
    // timer执行间隔，输入例如10s
    private static final String PERIOD = "period";

    @Override
    public void parse(Element parent, StepNode step, ParserContext parserContext) {
        String name = step.get().getProperty().strictGet(NAME, String.class);
        String period = step.get().getProperty().strictGet(PERIOD, String.class);
        String uri = String.format("timer:%s?period=%s", name, period);
        Element from = addElement(parent, step, "from");
        from.addAttribute("uri", uri);
        parseNext(parent, step, parserContext);
    }
}
