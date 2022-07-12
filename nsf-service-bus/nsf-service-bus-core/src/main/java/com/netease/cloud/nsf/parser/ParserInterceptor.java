package com.netease.cloud.nsf.parser;

import org.dom4j.Element;

/**
 * Parser拦截器，可在每个Parser执行parser方法前后插入切面逻辑
 *
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/28
 **/
public interface ParserInterceptor {
    /**
     * 可在Parser执行parser方法前后插入切面逻辑
     * <p>
     * 需要调用代理Parser的parser方法时，调用invoker.invoke()
     *
     * @param parent        当前xml node
     * @param step          当前stepNode
     * @param parserContext parserContext
     * @param invoker       parser代理方法
     */
    void around(Element parent, StepNode step, ParserContext parserContext, ParserProxy.Invoker invoker);
}
