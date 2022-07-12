package com.netease.cloud.nsf.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.cloud.nsf.record.ExceptionRecord;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.DefaultExchangeFormatter;
import org.apache.camel.spi.ExchangeFormatter;
import org.apache.camel.util.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

public class ExceptionProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionProcessor.class);
    private final ExchangeFormatter exchangeFormatter = new DefaultExchangeFormatter();
    private final Boolean markErrorHandlerHandled = Boolean.FALSE;
    private final ObjectMapper objectMapper;

    public ExceptionProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if(!markErrorHandlerHandled){
            // the false value mean the caught exception will be kept on the exchange, causing the
            // exception to be propagated back to the caller, and to break out routing
            exchange.setProperty(Exchange.ERRORHANDLER_HANDLED, false);
        }

        ExceptionRecord record = new ExceptionRecord();
        String integrationId = exchange.getUnitOfWork().getRouteContext().getRoute().getId();
        if (Objects.nonNull(exchange.getUnitOfWork()) && Objects.nonNull(exchange.getUnitOfWork().getTracedRouteNodes()) && Objects.nonNull(exchange.getUnitOfWork().getTracedRouteNodes().getLastNode())) {
            String stepId = exchange.getUnitOfWork().getTracedRouteNodes().getLastNode().getProcessorDefinition().getId();
            record.setStepId(stepId);
        }
        record.setRecordId(exchange.getExchangeId());
        record.setIntegrationId(integrationId);

        record.setMessageHistoryStacktrace(MessageHelper.dumpMessageHistoryStacktrace(exchange, exchangeFormatter, false));

        if (Objects.nonNull(exchange.getProperty(Exchange.FAILURE_ENDPOINT))) {
            record.setFailureEndpoint(exchange.getProperty(Exchange.FAILURE_ENDPOINT, String.class));
        }
        if (Objects.nonNull(exchange.getProperty(Exchange.EXCEPTION_CAUGHT))) {
            Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            record.setException(e.toString());
            record.setExceptionClass(e.getClass().toString());
            record.setExceptionMessage(e.getMessage());
            record.setExceptionStacktrace(getExceptionStacktrace(e));
        }

        logger.info(objectMapper.writeValueAsString(record));
    }

    private String getExceptionStacktrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
