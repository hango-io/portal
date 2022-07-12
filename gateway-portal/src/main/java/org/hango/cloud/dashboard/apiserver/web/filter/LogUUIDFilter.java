package org.hango.cloud.dashboard.apiserver.web.filter;

import org.hango.cloud.dashboard.apiserver.util.LogTraceUUIDHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 *
 */
@Order(1)
public class LogUUIDFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LogUUIDFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String uniqueId = UUID.randomUUID().toString();

            StringBuilder uuidBuilder = new StringBuilder(LogTraceUUIDHolder.LOG_TRACE_PREFIX);
            uuidBuilder.append(uniqueId);

            MDC.put(LogTraceUUIDHolder.LOG_TRACE_KEY, uuidBuilder.toString());
            LogTraceUUIDHolder.setUUIDId(uniqueId);

            // 使用 MultiReadRequestWrapper 替换 ServletResponse
            HttpServletRequest req = (HttpServletRequest) request;
            MultiReadRequestWrapper requestWrapper = new MultiReadRequestWrapper(req);
            chain.doFilter(requestWrapper, response);
        } catch (Exception e) {
            logger.info("", e);

        }
    }

    @Override
    public void destroy() {
        MDC.remove(LogTraceUUIDHolder.LOG_TRACE_KEY);
    }

}
