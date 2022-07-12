package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import com.netease.cloud.nsf.step.Property;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoopParser extends BaseParser implements Parser {
    // 可选repeat、doWhile
    private static final String LOOP_MODE = "loopMode";
    // 如果为true,则每次循环时，都会使用循环exchange副本，每次循环的exchange都会相同；如果为false，则每次循环都使用同一份上下文，默认值为false
    private static final String COPY = "copy";
    // 当loopMode=repeat时，配置重复执行次数
    private static final String REPEAT_COUNT = "repeatCount";
    // 当loopMode=doWhile时，可选simple、groovy
    private static final String PREDICATE_TYPE = "predicateType";
    // 当predicateType=simple时，填写expression表达式
    private static final String SIMPLE_EXPRESSION = "simpleExpression";
    // 当predicateType=groovy时，填写groovy脚本
    private static final String GROOVY_SHELL = "groovyShell";
    // 可选，可选项有seda、direct. 当选择seda时，将选择sedaEndpoint;当选择direct时，将选择directEndpoint
    private static final String SUB_INTEGRATION_TYPE = "subIntegrationType";
    // 可选，当配置了subDirectIntegration时，将不会继续parse childSteps，而是将子direct integration作为循环体
    private static final String SUB_INTEGRATION = "subIntegration";
    // 可选，当配置了当配置了subDirectIntegration时，可配置该选项
    // 当对应directEndpoint consumer不存在时，是否报错. 当subIntegrationType=direct默认true; 当subIntegrationType=seda默认true；
    private static final String FAIL_IF_NO_CONSUMERS = "failIfNoConsumers";

    /**
     * subIntegrationType=direct
     */
    // 可选，当配置了当配置了subDirectIntegration时，可配置该选项，默认true
    // 当找不到对应的directEndpoint，会一直阻塞，直到对应directEndpoint流程启用。
    private static final String BLOCK = "block";

    /**
     * subIntegrationType=seda
     */
    // (producer param) default false.
    // 当队列满时，是否block直到消息被结束. 默认false将抛出异常
    private static final String BLOCK_WHEN_FULL = "blockWhenFull";
    // (producer param)
    // 当blockWhenFull = true时，可配置offer block timeout. 可以通过配0或负数，disable该选项
    private static final String OFFER_TIMEOUT = "offerTimeout";
    // (producer param) default IfReplyExpected.
    // 是否等待task结束，可选项有Never、IfReplyExpected、Always. 默认为IfReplyExpected，将根据触发器类型来决定是异步还是同步返回.
    private static final String WAIT_FOR_TASK_TO_COMPLETE = "waitForTaskToComplete";
    // (producer param) default 30000.
    // 等待异步任务完成的超时时间，可以设置0或负数，来disable该选项
    private static final String TIMEOUT = "timeout";
    // (producer param) default false.
    // 当指定consumer不存在时，是否忽略. 和failIfNoConsumers同时只能有一个选项生效.
    private static final String DISCARD_IF_NO_CONSUMERS = "discardIfNoConsumers";

    @Override
    public void parse(Element parent, StepNode node, ParserContext parserContext) {
        Property property = node.get().getProperty();
        Element loop = addElement(parent, node, "loop");
        Boolean copy = property.getOrDefault(COPY, Boolean.class, Boolean.FALSE);
        loop.addAttribute("copy", String.valueOf(copy));
        String loopMode = property.strictGet(LOOP_MODE, String.class);
        if ("repeat".equals(loopMode)) {
            Integer repeatCount = property.strictGet(REPEAT_COUNT, Integer.class);
            Element constant = addElement(loop, node, "constant");
            constant.setText(String.valueOf(repeatCount));
        } else if ("doWhile".equals(loopMode)) {
            loop.addAttribute("doWhile", String.valueOf(Boolean.TRUE));
            String predicateType = property.strictGet(PREDICATE_TYPE, String.class);
            if ("simple".equals(predicateType)) {
                String simpleExpression = property.strictGet(SIMPLE_EXPRESSION, String.class);
                Element simple = addElement(loop, node, "simple");
                simple.setText(simpleExpression);
            } else if ("groovy".equals(predicateType)) {
                String groovyShell = property.strictGet(GROOVY_SHELL, String.class);
                Element shell = addElement(loop, node, "groovy");
                shell.setText(groovyShell);
            } else {
                throw new RuntimeException(String.format("Unsupported param[%s] value [%s]", PREDICATE_TYPE, predicateType));
            }
        } else {
            throw new RuntimeException(String.format("Unsupported param[%s] value [%s]", LOOP_MODE, loopMode));
        }

        // 如果包含了subIntegration，则使用subIntegration，而不是parse childSteps
        String subIntegration = property.get(SUB_INTEGRATION, String.class);
        if (StringUtils.isNotEmpty(subIntegration)) {
            Map<String, Object> uriOptions = new HashMap<>();
            String uri;
            String subIntegrationType = property.get(SUB_INTEGRATION_TYPE, String.class);
            if ("direct".equalsIgnoreCase(subIntegrationType)) {
                setDirectSubIntegration(node, uriOptions);
                uri = buildDirectUri(subIntegration);
            } else if ("seda".equalsIgnoreCase(subIntegrationType)) {
                setSedaSubIntegration(node, uriOptions);
                uri = buildSedaUri(subIntegration);
            } else {
                throw new RuntimeException(String.format("Unsupported param[%s] value [%s]", SUB_INTEGRATION_TYPE, subIntegrationType));
            }
            uri = appendParametersToURI(uri, uriOptions);
            Element to = addElement(loop, node, "to");
            to.addAttribute("uri", uri);
        } else {
            parseChild(loop, node, parserContext);
        }
        parseNext(parent, node, parserContext);
    }

    private void setDirectSubIntegration(StepNode node, Map<String, Object> options) {
        Property property = node.get().getProperty();
        Boolean block = property.getOrDefault(BLOCK, Boolean.class, Boolean.TRUE);
        Boolean failIfNoConsumers = property.getOrDefault(FAIL_IF_NO_CONSUMERS, Boolean.class, Boolean.TRUE);
        options.put("block", block);
        options.put("failIfNoConsumers", failIfNoConsumers);
    }

    private void setSedaSubIntegration(StepNode node, Map<String, Object> options) {
        Property property = node.get().getProperty();
        Boolean blockWhenFull = property.getOrDefault(BLOCK_WHEN_FULL, Boolean.class, Boolean.FALSE);
        Long offerTimeout = property.get(OFFER_TIMEOUT, Long.class);
        String waitForTaskToComplete = property.getOrDefault(WAIT_FOR_TASK_TO_COMPLETE, String.class, "IfReplyExpected");
        Boolean discardIfNoConsumers = property.getOrDefault(DISCARD_IF_NO_CONSUMERS, Boolean.class, Boolean.FALSE);
        Boolean failIfNoConsumers = property.getOrDefault(FAIL_IF_NO_CONSUMERS, Boolean.class, Boolean.TRUE);
        Long timeout = property.getOrDefault(TIMEOUT, Long.class, 30000L);
        options.put("blockWhenFull", blockWhenFull);
        if (Boolean.TRUE.equals(blockWhenFull) && Objects.nonNull(offerTimeout)) {
            options.put("offerTimeout", offerTimeout);
        }
        options.put("waitForTaskToComplete", waitForTaskToComplete);
        options.put("discardIfNoConsumers", discardIfNoConsumers);
        options.put("failIfNoConsumers", failIfNoConsumers);
        options.put("timeout", timeout);
    }

    // 需要与DirectParser中的buildUri方法保持一致
    private String buildDirectUri(String integrationId) {
        return String.format("direct:directEndpoint[integrationId=%s]", integrationId);
    }

    // 需要与SedaParser中的buildUri方法保持一致
    private String buildSedaUri(String integrationId) {
        return String.format("seda:sedaEndpoint[integrationId=%s]", integrationId);
    }
}
