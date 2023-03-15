package org.hango.cloud.common.infra.base.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author hanjiahao
 * 项目Id过滤器，如果没有项目id，则使用默认项目id，方便后续程序进行处理
 */
public class ProjectTraceFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ProjectTraceFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 未使用，无需实现
    }

    /**
     * 从请求头获取X-Project-Id, 并存储至ProjectHolder中
     *
     * @param request  请求request
     * @param response 请求response
     * @param chain    filter调用chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ProjectTraceHolder.setProId(NumberUtils.LONG_ONE);
        ProjectTraceHolder.setTenantId(NumberUtils.LONG_ONE);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        ProjectTraceHolder.removeProId();
        ProjectTraceHolder.removeTenantId();
    }
}
