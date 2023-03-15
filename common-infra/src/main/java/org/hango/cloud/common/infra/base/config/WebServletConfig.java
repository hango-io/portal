package org.hango.cloud.common.infra.base.config;


import org.apache.commons.lang3.BooleanUtils;
import org.hango.cloud.common.infra.base.filter.LogUUIDFilter;
import org.hango.cloud.common.infra.base.filter.ProjectTraceFilter;
import org.hango.cloud.common.infra.base.filter.RequestContextHolderFilter;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.operationaudit.filter.OperationAuditFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
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
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean logUuidFilterReg() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new LogUUIDFilter());
        registration.addUrlPatterns("/*");
        registration.setName(LogUUIDFilter.class.getSimpleName());
        registration.setOrder(10000);
        return registration;
    }


    @Bean
    @ConditionalOnMissingBean(name="projectFilterReg")
    public FilterRegistrationBean projectFilterReg() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ProjectTraceFilter());
        registration.addUrlPatterns("/*");
        registration.setName(ProjectTraceFilter.class.getSimpleName());
        registration.setOrder(20000);
        return registration;
    }

    @Bean
    @ConditionalOnProperty(name = BaseConst.OPERATION_AUDIT_ENABLE, havingValue = BooleanUtils.TRUE, matchIfMissing = true)
    public FilterRegistrationBean operationAuditFilterReg() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new OperationAuditFilter());
        registration.addUrlPatterns("/*");
        registration.setName(OperationAuditFilter.class.getSimpleName());
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }


}
