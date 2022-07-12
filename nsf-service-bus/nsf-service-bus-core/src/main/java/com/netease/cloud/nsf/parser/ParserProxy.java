package com.netease.cloud.nsf.parser;

import org.dom4j.Element;

import java.util.Objects;

/**
 * Parser代理类
 *
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/28
 **/
public class ParserProxy implements Parser {

    private Parser instance;
    private ParserInterceptor[] interceptors;

    public ParserProxy(Parser instance, ParserInterceptor[] interceptors) {
        this.instance = instance;
        this.interceptors = interceptors;
    }

    @Override
    public void parse(Element parent, StepNode step, ParserContext parserContext) {
        InterceptorAdaptor adaptor = new InterceptorAdaptor(parent, step, parserContext, null, null) {
            @Override
            public void invoke() {
                instance.parse(parent, step, parserContext);
            }
        };
        if (Objects.nonNull(interceptors)) {
            for (ParserInterceptor interceptor : interceptors) {
                adaptor = new InterceptorAdaptor(parent, step, parserContext, interceptor, adaptor);
            }
        }
        adaptor.invoke();
    }


    private static class InterceptorAdaptor implements ParserInterceptor, Invoker {
        private Element parent;
        private StepNode step;
        private ParserContext parserContext;
        private ParserInterceptor interceptor;
        private InterceptorAdaptor adaptor;

        public InterceptorAdaptor(Element parent, StepNode step, ParserContext parserContext, ParserInterceptor interceptor, InterceptorAdaptor adaptor) {
            this.parent = parent;
            this.step = step;
            this.parserContext = parserContext;
            this.interceptor = interceptor;
            this.adaptor = adaptor;
        }

        @Override
        public void invoke() {
            if (Objects.nonNull(interceptor)) {
                interceptor.around(parent, step, parserContext, adaptor);
            } else {
                adaptor.invoke();
            }
        }

        @Override
        public void around(Element parent, StepNode step, ParserContext parserContext, Invoker invoker) {
            interceptor.around(parent, step, parserContext, invoker);
        }
    }

    public interface Invoker {
        void invoke();
    }
}
