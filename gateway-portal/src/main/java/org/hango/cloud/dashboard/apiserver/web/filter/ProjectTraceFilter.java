package org.hango.cloud.dashboard.apiserver.web.filter;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.enums.ActionIgnoreProject;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.util.AccessUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author hanjiahao
 * 项目Id过滤器，如果没有项目id，则使用默认项目id，方便后续程序进行处理
 */
public class ProjectTraceFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ProjectTraceFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

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
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            String projectIdHeader = req.getHeader(ProjectTraceHolder.PROJECT_TRACE_ID);
            String tenantIdHeader = req.getHeader(ProjectTraceHolder.TENANT_TRACE_ID);
            if (StringUtils.isBlank(projectIdHeader)) {
               projectIdHeader = "1";
            }
             if (StringUtils.isBlank(tenantIdHeader)) {
               tenantIdHeader = "1";
            }
            String uri = req.getRequestURI();
            //非api开头的接口不进行判断
            if (Const.NO_USERPERMISSION.contains(uri) || StringUtils.isNotBlank(ActionIgnoreProject.getActionIgnoreProject(request.getParameter("Action")))) {
                //默认的projectId，如果请求头不填，则为默认的projectId
                ProjectTraceHolder.setProId(ProjectTraceHolder.DEFAULT_PROJECT_ID);
                chain.doFilter(request, response);
                return;
            }
            //如果projectId为空，默认为defaultProId
            if (StringUtils.isBlank(projectIdHeader)) {
                logger.error("请求action:{},请求projectId：{}", request.getParameter("Action"), projectIdHeader);
                AccessUtil.sendWithError(res, CommonErrorCode.EmptyProjectId);
                return;
            }
            if (StringUtils.isBlank(tenantIdHeader)) {
                AccessUtil.sendWithError(res, CommonErrorCode.EmptyTenantId);
                return;
            }
            ProjectTraceHolder.setProId(Long.parseLong(projectIdHeader));
            ProjectTraceHolder.setTenantId(Long.parseLong(tenantIdHeader));
        } catch (Exception e) {
            logger.info("projectHeader头填写异常");
            e.printStackTrace();
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        ProjectTraceHolder.removeProId();
        ProjectTraceHolder.removeTenantId();
    }
}
