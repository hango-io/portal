package com.netease.cloud.nsf.parser;

import java.util.List;

/**
 * parser上下文
 *
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/21
 **/
public interface ParserContext {
    /**
     * 根据step获取到对应的Parser
     * @param step
     * @return
     */
    Parser getParser(StepNode step);

    /**
     * 获取ParserInterceptor
     * @return
     */
    List<ParserInterceptor> getInterceptors();

    /**
     * 添加ParserInterceptor
     * @param interceptor
     */
    void addInterceptor(ParserInterceptor interceptor);

    /**
     * 获取ParserOptions
     * @return
     */
    ParserOptions getParserOptions();
}
