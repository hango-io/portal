package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import com.netease.cloud.nsf.step.Property;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SedaParser extends BaseParser implements Parser {
    // 可选from、to，from类型即触发器类型，to类型即连接类型
    private static final String MODE = "mode";
    // 当mode=to时需要填写，目标SEDA的integration ID
    private static final String SEDA_INTEGRATION_ID = "sedaIntegrationId";
    // default 1000.
    // 队列长度
    private static final String QUEUE_SIZE = "size";
    // (consumer param) default 1.
    // consumer并行处理的数量
    private static final String CONCURRENT_CONSUMERS = "concurrentConsumers";
    // (consumer param) default false.
    // 是否允许多consumer. 如果为true，可以使用SEDA实现消息订阅发布模式. 你可以发布一条消息而多个consumer都会接收到该消息的副本
    private static final String MULTIPLE_CONSUMERS = "multipleConsumers";
    // (consumer param) default 1000.
    // The timeout used when polling.
    // 当超时发生时，consumer可以检查是否继续运行. 设置较小的值可以使consumer更快关闭.单位ms.
    private static final String POLL_TIMEOUT = "pollTimeout";
    // (consumer param) default false.
    // 当路由停止时，是否清空队列. 该选项能加快路由关闭速度.
    private static final String PURGE_WHEN_STOPPING = "purgeWhenStopping";

    // (producer param) default false.
    // 当队列满时，是否block直到消息被消失. 默认false将抛出异常
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
    // (producer param) default false.
    // 当指定consumer不存在时，是否抛出异常. 和discardIfNoConsumers同时只能有一个选项生效.
    private static final String FAIL_IF_NO_CONSUMERS = "failIfNoConsumers";

    @Override
    public void parse(Element parent, StepNode node, ParserContext parserContext) {
        Property property = node.get().getProperty();
        String mode = property.strictGet(MODE, String.class);

        Map<String, Object> uriOptions = new HashMap<>();

        if ("from".equals(mode)) {
            setConsumerUriParam(node, uriOptions);
            // 如果未指定queue, endpoint的integrationId为自己当前id
            String integrationId = getIntegrationId(node);
            String uri = buildUri(integrationId);
            uri = appendParametersToURI(uri, uriOptions);

            Element from = addElement(parent, node, "from");
            from.addAttribute("uri", uri);
        } else if ("to".equals(mode)) {
            setProducerUriParam(node, uriOptions);
            String sedaIntegrationId = property.strictGet(SEDA_INTEGRATION_ID, String.class);
            String uri = buildUri(sedaIntegrationId);
            uri = appendParametersToURI(uri, uriOptions);

            Element to = addElement(parent, node, "to");
            to.addAttribute("uri", uri);
        } else {
            throw new RuntimeException(String.format("Unsupported param[%s] value [%s]", MODE, mode));
        }

        parseNext(parent, node, parserContext);
    }

    private void setConsumerUriParam(StepNode node, Map<String, Object> uriOptions) {
        Property property = node.get().getProperty();
        Integer queueSize = property.getOrDefault(QUEUE_SIZE, Integer.class, 1000);
        Integer concurrentConsumers = property.getOrDefault(CONCURRENT_CONSUMERS, Integer.class, 1);
        Boolean multipleConsumers = property.getOrDefault(MULTIPLE_CONSUMERS, Boolean.class, Boolean.FALSE);
        Long pollTimeout = property.getOrDefault(POLL_TIMEOUT, Long.class, 1000L);
        Boolean purgeWhenStopping = property.getOrDefault(PURGE_WHEN_STOPPING, Boolean.class, Boolean.FALSE);

        uriOptions.put("size", queueSize);
        uriOptions.put("concurrentConsumers", concurrentConsumers);
        uriOptions.put("multipleConsumers", multipleConsumers);
        uriOptions.put("pollTimeout", pollTimeout);
        uriOptions.put("purgeWhenStopping", purgeWhenStopping);
    }

    private void setProducerUriParam(StepNode node, Map<String, Object> uriOptions) {
        Property property = node.get().getProperty();
        Boolean blockWhenFull = property.getOrDefault(BLOCK_WHEN_FULL, Boolean.class, Boolean.FALSE);
        Long offerTimeout = property.get(OFFER_TIMEOUT, Long.class);
        String waitForTaskToComplete = property.getOrDefault(WAIT_FOR_TASK_TO_COMPLETE, String.class, "IfReplyExpected");
        Boolean discardIfNoConsumers = property.getOrDefault(DISCARD_IF_NO_CONSUMERS, Boolean.class, Boolean.FALSE);
        Boolean failIfNoConsumers = property.getOrDefault(FAIL_IF_NO_CONSUMERS, Boolean.class, Boolean.TRUE);
        Long timeout = property.getOrDefault(TIMEOUT, Long.class, 30000L);
        uriOptions.put("blockWhenFull", blockWhenFull);
        if (Boolean.TRUE.equals(blockWhenFull) && Objects.nonNull(offerTimeout)) {
            uriOptions.put("offerTimeout", offerTimeout);
        }
        uriOptions.put("waitForTaskToComplete", waitForTaskToComplete);
        uriOptions.put("discardIfNoConsumers", discardIfNoConsumers);
        uriOptions.put("failIfNoConsumers", failIfNoConsumers);
        uriOptions.put("timeout", timeout);
    }

    private String buildUriWithQueue(String queue) {
        return String.format("seda:sedaEndpoint[queue=%s]", queue);
    }

    private String buildUri(String integrationId) {
        return String.format("seda:sedaEndpoint[integrationId=%s]", integrationId);
    }
}
