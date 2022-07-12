package org.hango.cloud.dashboard.apiserver.web.filter;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.web.holder.GatewayTraceHolder;
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
 * 审计相关获取网关id，审计参数头中均填入网关id
 * 包括dashboard页面以及审计管理页面
 */
public class AuditGatewayIdFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuditGatewayIdFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {

    }

    /**
     * 从请求头获取X-Gw-Id, 并存储至GatewayTraceHolder中
     *
     * @param request  请求request
     * @param response 请求response
     * @param chain    filter调用chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            //获取网关id
            String gwId = req.getHeader(GatewayTraceHolder.GATEWAY_ID);
            if (StringUtils.isBlank(gwId)) {
                gwId = GatewayTraceHolder.DEFAULT_GATEWAY_ID;
            }
            GatewayTraceHolder.setGatewayId(gwId);
        } catch (Exception e) {
            logger.info("gwId头填写异常");
            e.printStackTrace();
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        GatewayTraceHolder.removeGatewayId();
    }
}
