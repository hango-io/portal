package org.hango.cloud.dashboard.apiserver.config;

import org.hango.cloud.dashboard.apiserver.web.filter.AuditGatewayIdFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.LogUUIDFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.ProjectTraceFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.RequestContextHolderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Zhu Jianfeng (hzzhujianfeng)
 * @version $Id: WebServletConfig.java, v 1.0 2017年3月24日 下午4:46:11
 */
@Configuration
public class WebServletConfig extends WebMvcConfigurerAdapter {

    @Bean
    public FilterRegistrationBean requestContextFilterReg() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new RequestContextHolderFilter());
        registration.addUrlPatterns("/*");
        registration.setName(RequestContextHolderFilter.class.getSimpleName());
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean logUuidFilterReg() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new LogUUIDFilter());
        registration.addUrlPatterns("/*");
        registration.setName(LogUUIDFilter.class.getSimpleName());
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean projectFilterReg() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ProjectTraceFilter());
        registration.addUrlPatterns("/*");
        registration.setName(ProjectTraceFilter.class.getSimpleName());
        registration.setOrder(3);
        return registration;
    }


    @Bean
    public FilterRegistrationBean auditGatewayIdFilterReg() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new AuditGatewayIdFilter());
        registration.addUrlPatterns("/*");
        registration.setName(AuditGatewayIdFilter.class.getSimpleName());
        registration.setOrder(5);
        return registration;
    }

}
