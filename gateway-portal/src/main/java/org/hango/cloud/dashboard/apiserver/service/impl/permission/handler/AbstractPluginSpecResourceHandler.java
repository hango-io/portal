package org.hango.cloud.dashboard.apiserver.service.impl.permission.handler;

import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/8/10
 */
public abstract class AbstractPluginSpecResourceHandler extends BaseSpecResourceHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPluginSpecResourceHandler.class);

    public IEnvoyPluginInfoService getPluginService(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        try {
            return context.getBean(IEnvoyPluginInfoService.class);
        } catch (BeansException e) {
            logger.error("Can Not Find Bean ... Bean Name is {}", IEnvoyPluginInfoService.class.getCanonicalName());
        }
        return null;
    }
}
