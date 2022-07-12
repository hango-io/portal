package org.hango.cloud.dashboard.apiserver.web.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.enums.ActionIgnoreProject;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.service.impl.permission.IServicePermissionService;
import org.hango.cloud.dashboard.apiserver.util.AccessUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ActionInfoHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author hanjiahao
 * 平台权限判断非管理员权限
 * 判断服务管理员权限
 */
public class ServicePermissionFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ServicePermissionFilter.class);

    private IServicePermissionService servicePermissionService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        this.servicePermissionService = (IServicePermissionService) wac.getBean("servicePermissionService");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();
        //不需要进行鉴权的接口，同时不需要携带accountId头
        if (Const.NO_USERPERMISSION.contains(uri) || StringUtils.isNotBlank(ActionIgnoreProject.getActionIgnoreProject(request.getParameter("Action")))) {
            chain.doFilter(request, response);
            return;
        }
        String token = request.getHeader(UserPermissionHolder.USER_PERMISSION);
        if (token == null) {
            logger.info("请求token为空");
            AccessUtil.sendWithError(response, CommonErrorCode.NoPermission);
            return;
        }
        //兼容之前的逻辑，不对accountId进行强判断，前端适配。不使用accountId进行鉴权，使用JWT进行鉴权
        String accountId = request.getHeader(UserPermissionHolder.USER_ACCOUNTID);
        UserPermissionHolder.setAccountId(accountId);
        UserPermissionHolder.setJwt(token);
        //获取action
        ActionInfoHolder.setAction(request.getParameter("Action"));


        //判断是否具有访问权限
        if (servicePermissionService.hasAccessAuthority(accountId, ProjectTraceHolder.getTenantId(), ProjectTraceHolder.getProId(), NumberUtils.LONG_ZERO)
                && servicePermissionService.hasRole(token, request)) {
            chain.doFilter(request, response);
        } else {
            logger.info("用户鉴权不通过，没有访问对应接口的权限");
            AccessUtil.sendWithError(response, CommonErrorCode.NoPermission);
            return;
        }
    }

    @Override
    public void destroy() {
        UserPermissionHolder.removePermission();
        ActionInfoHolder.removeAction();
    }
}
