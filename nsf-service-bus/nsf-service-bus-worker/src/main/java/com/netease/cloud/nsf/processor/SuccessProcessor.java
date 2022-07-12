package com.netease.cloud.nsf.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.cloud.nsf.parser.ParserConst;
import com.netease.cloud.nsf.record.SuccessRecord;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/25
 **/
public class SuccessProcessor implements Processor {
    // todo: 使用RoutePolicy onExchangeBegin、onExchangeDone 标识流程状态{执行中、执行完成、超时}
    private static final Logger logger = LoggerFactory.getLogger(SuccessProcessor.class);
    private final ObjectMapper objectMapper;

    public SuccessProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        SuccessRecord record = new SuccessRecord();
        record.setIntegrationId(exchange.getFromRouteId());
        record.setRecordId(exchange.getExchangeId());
        record.setTimestamp(String.valueOf(System.currentTimeMillis()));
        Object tag = exchange.getMessage().getHeader(ParserConst.ON_SUCCESS_TAG);
        if (Objects.nonNull(tag)) {
            record.setTag(String.valueOf(tag));
        }
        logger.info(objectMapper.writeValueAsString(record));
    }

}
