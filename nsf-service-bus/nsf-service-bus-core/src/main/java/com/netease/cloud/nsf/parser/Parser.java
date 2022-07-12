package com.netease.cloud.nsf.parser;

import org.dom4j.Element;

/**
 * Step Parser，负责将Step渲染为camel能够识别的route xml片段
 *
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/21
 **/
public interface Parser {
    /**
     * 将Step渲染为camel能够识别的route xml片段
     *
     * @param parent        当前xml node节点
     * @param node          当前StepNode
     * @param parserContext parser上下文
     */
    void parse(Element parent, StepNode node, ParserContext parserContext);
}
