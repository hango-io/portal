package com.netease.cloud.nsf.record;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.RouteNode;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.ProcessorDefinitionHelper;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.processor.interceptor.TraceFormatter;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.apache.camel.spi.TracedRouteNodes;
import org.apache.camel.util.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/21
 **/
public class CustomTraceFormatter implements TraceFormatter {
    private static final Logger logger = LoggerFactory.getLogger(CustomTraceFormatter.class);

    private int nodeLength = 0;
    private int breadCrumbLength = 0;
    private int propertiesValueLength = 0;
    private int headersValueLength = 0;
    private boolean showBreadCrumb = true;
    private boolean showPattern = true;
    private boolean showProperties = true;
    private boolean showHeaders = true;
    private boolean showBodyType = true;
    private boolean showBody = false;
    private boolean showException = true;
    private boolean allowStream = false;
    private boolean allowFile = false;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object format(TraceInterceptor interceptor, ProcessorDefinition<?> node, Exchange exchange) {
        TraceRecord record = new TraceRecord();
        String integrationId = exchange.getUnitOfWork().getRouteContext().getRoute().getId();
        if (Objects.nonNull(exchange.getUnitOfWork()) && Objects.nonNull(exchange.getUnitOfWork().getTracedRouteNodes()) && Objects.nonNull(exchange.getUnitOfWork().getTracedRouteNodes().getLastNode())) {
            String stepId = exchange.getUnitOfWork().getTracedRouteNodes().getLastNode().getProcessorDefinition().getId();
            record.setStepId(stepId);
        }
        record.setRecordId(exchange.getExchangeId());
        record.setTimestamp(String.valueOf(System.currentTimeMillis()));
        record.setIntegrationId(integrationId);

        if (showBreadCrumb) {
            record.setBreadCrumb(extractBreadCrumb(interceptor, node, exchange));
        }
        if (showPattern && Objects.nonNull(exchange.getPattern())) {
            record.setPattern(exchange.getPattern().toString());
        }
        if (showProperties && Objects.nonNull(exchange.getProperties())) {
            record.setProperties(extractMapForLogging(exchange.getProperties(), propertiesValueLength));
        }
        if (showHeaders && Objects.nonNull(exchange.getMessage()) && Objects.nonNull(exchange.getMessage().getHeaders())) {
            record.setHeaders(extractMapForLogging(exchange.getMessage().getHeaders(), headersValueLength));
        }
        if (showBodyType && Objects.nonNull(exchange.getMessage())) {
            record.setBodyType(MessageHelper.getBodyTypeName(exchange.getMessage()));
        }
        if (showBody && Objects.nonNull(exchange.getMessage())) {
            record.setBody(extractBodyForLogging(exchange.getMessage(), "", allowStream, allowFile));
        }
        if (showException && Objects.nonNull(exchange.getException())) {
            record.setException(exchange.getException().toString());
        }
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setNodeLength(int nodeLength) {
        this.nodeLength = nodeLength;
    }

    public void setBreadCrumbLength(int breadCrumbLength) {
        this.breadCrumbLength = breadCrumbLength;
    }

    public void setPropertiesValueLength(int propertiesValueLength) {
        this.propertiesValueLength = propertiesValueLength;
    }

    public void setHeadersValueLength(int headersValueLength) {
        this.headersValueLength = headersValueLength;
    }

    public void setShowBreadCrumb(boolean showBreadCrumb) {
        this.showBreadCrumb = showBreadCrumb;
    }

    public void setShowPattern(boolean showPattern) {
        this.showPattern = showPattern;
    }

    public void setShowProperties(boolean showProperties) {
        this.showProperties = showProperties;
    }

    public void setShowHeaders(boolean showHeaders) {
        this.showHeaders = showHeaders;
    }

    public void setShowBodyType(boolean showBodyType) {
        this.showBodyType = showBodyType;
    }

    public void setShowBody(boolean showBody) {
        this.showBody = showBody;
    }

    public void setShowException(boolean showException) {
        this.showException = showException;
    }

    public void setAllowStream(boolean allowStream) {
        this.allowStream = allowStream;
    }

    public void setAllowFile(boolean allowFile) {
        this.allowFile = allowFile;
    }

    protected String extractRoute(ProcessorDefinition<?> node) {
        RouteDefinition route = ProcessorDefinitionHelper.getRoute(node);
        if (route != null) {
            return route.getId();
        } else {
            return null;
        }
    }

    protected Object getBreadCrumbID(Exchange exchange) {
        return exchange.getExchangeId();
    }

    protected String getNodeMessage(RouteNode entry, Exchange exchange) {
        String message = entry.getLabel(exchange);
        if (nodeLength > 0) {
            return String.format("%1$-" + nodeLength + "." + nodeLength + "s", message);
        } else {
            return message;
        }
    }

    /**
     * Creates the breadcrumb based on whether this was a trace of
     * an exchange coming out of or into a processing step. For example,
     * <br/><tt>transform(body) -> ID-mojo/39713-1225468755256/2-0</tt>
     * <br/>or
     * <br/><tt>ID-mojo/39713-1225468755256/2-0 -> transform(body)</tt>
     */
    protected String extractBreadCrumb(TraceInterceptor interceptor, ProcessorDefinition<?> currentNode, Exchange exchange) {
        String result;

        // compute from, to and route
        String from = "";
        String to = "";
        if (exchange.getUnitOfWork() != null) {
            TracedRouteNodes traced = exchange.getUnitOfWork().getTracedRouteNodes();
            if (traced != null) {
                RouteNode traceFrom = traced.getSecondLastNode();
                if (traceFrom != null) {
                    from = getNodeMessage(traceFrom, exchange);
                } else if (exchange.getFromEndpoint() != null) {
                    from = "from(" + exchange.getFromEndpoint().getEndpointUri() + ")";
                }

                RouteNode traceTo = traced.getLastNode();
                if (traceTo != null) {
                    to = getNodeMessage(traceTo, exchange);
                }
            }
        }

        // assemble result with and without the to/from

        result = " >>> " + from + " --> " + to.trim() + " <<< ";

        if (interceptor.shouldTraceOutExchanges() && exchange.hasOut()) {
            result += " (OUT) ";
        }

        if (breadCrumbLength > 0 && result.trim().length() > breadCrumbLength) {
            return result.trim().substring(0, breadCrumbLength) + "... [Value clipped after " + breadCrumbLength + " chars, total length is " + result.trim().length() + "]";
        } else {
            return result.trim();
        }
    }

    protected static String extractBodyForLogging(Message message, String prepend, boolean allowStreams, boolean allowFiles) {
        // default to 1000 chars
        int maxChars = 1000;

        if (message.getExchange() != null) {
            String globalOption = message.getExchange().getContext().getGlobalOption(Exchange.LOG_DEBUG_BODY_MAX_CHARS);
            if (globalOption != null) {
                maxChars = message.getExchange().getContext().getTypeConverter().convertTo(Integer.class, globalOption);
            }

            String exchangeOption = message.getExchange().getProperty(Exchange.LOG_DEBUG_BODY_MAX_CHARS, String.class);
            if (exchangeOption != null) {
                maxChars = message.getExchange().getContext().getTypeConverter().convertTo(Integer.class, exchangeOption);
            }
        }

        return MessageHelper.extractBodyForLogging(message, prepend, allowStreams, allowFiles, maxChars);
    }

    protected static String extractMapForLogging(Map<String, Object> map, int maxChars) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        Iterator<Map.Entry<String, Object>> mapIterator = map.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<String, Object> entry = mapIterator.next();
            String key = entry.getKey();
            String value = Objects.toString(entry.getValue());
            // clip value if length enabled and the value is too big
            if (maxChars > 0 && value.length() > maxChars) {
                value = value.substring(0, maxChars) + "... [Value clipped after " + maxChars + " chars, total length is " + value.length() + "]";
            }

            sb.append(key);
            sb.append('=');
            sb.append(value);
            if (mapIterator.hasNext()) {
                sb.append(',').append(' ');
            }
        }
        return sb.append('}').toString();
    }
}