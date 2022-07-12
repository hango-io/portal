package com.netease.cloud.nsf.parser;

import com.netease.cloud.nsf.parser.impl.*;

import java.util.*;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/21
 **/
public class MapParserContext implements ParserContext {
    private Map<String, Parser> parserMap = new HashMap<String, Parser>();
    private List<ParserInterceptor> interceptors = new ArrayList<>();
    private final ParserOptions parserOptions;

    {
        parserMap.put("flow", new FlowParser());
        parserMap.put("assert", new AssertParser());
        parserMap.put("transform", new MessageTransformParser());
        parserMap.put("groovy", new GroovyParser());
        parserMap.put("timer", new QuartzParser());
        parserMap.put("http", new HttpParser());
        parserMap.put("template", new FreemarkerParser());
        parserMap.put("openApi", new OpenApiParser());
        parserMap.put("onSuccess", new OnSuccessParser());
        parserMap.put("loop", new LoopParser());
        parserMap.put("delay", new DelayParser());
        parserMap.put("direct", new DirectParser());
        parserMap.put("seda", new SedaParser());
        parserMap.put("directEndpoint", new DirectParser());
        parserMap.put("sedaEndpoint", new SedaParser());
    }

    public MapParserContext(ParserOptions parserOptions) {
        this.parserOptions = parserOptions;
    }


    @Override
    public Parser getParser(StepNode step) {
        String stepKind = step.get().getStepKind();
        List<ParserInterceptor> interceptors = getInterceptors();
        Parser parser = parserMap.get(stepKind);
        if (Objects.isNull(parser)) {
            throw new RuntimeException(String.format("parser with stepKind for %s cannot be found", stepKind));
        }
        return new ParserProxy(parser, interceptors.toArray(new ParserInterceptor[0]));
    }

    @Override
    public List<ParserInterceptor> getInterceptors() {
        return interceptors;
    }

    @Override
    public void addInterceptor(ParserInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    @Override
    public ParserOptions getParserOptions() {
        return this.parserOptions;
    }
}
