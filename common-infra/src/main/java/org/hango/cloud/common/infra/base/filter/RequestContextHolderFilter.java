package org.hango.cloud.common.infra.base.filter;


import org.hango.cloud.common.infra.base.holder.RequestContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * 请求过滤器
 */
public class RequestContextHolderFilter extends RequestContextHolder implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 未使用，无需实现
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        RequestContextHolder.values.set(new HashMap<String, Object>());
        RequestContextHolder.setValue(RequestContextHolder.REQUEST_KEY, servletRequest);
        RequestContextHolder.setValue(RequestContextHolder.RESPONSE_KEY, servletResponse);
        filterChain.doFilter(servletRequest, servletResponse);
        RequestContextHolder.values.set(null);
    }

    @Override
    public void destroy() {
        // 未使用，无需实现
    }

}
