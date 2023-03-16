package org.hango.cloud.envoy.infra;

import org.hango.cloud.common.infra.base.filter.ProjectTraceFilter;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2017/12/4 上午10:33.
 */
@SpringBootApplication(exclude = {GsonAutoConfiguration.class, RedisAutoConfiguration.class, RedissonAutoConfiguration.class})
@EnableScheduling
@ComponentScan(value = {"org.hango.cloud"})
@MapperScan("org.hango.cloud.*.infra.base.mapper")
public class EnvoyApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(EnvoyApplication.class, args);
    }

    DispatcherServlet dispatcherServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
        return dispatcherServlet;
    }
    @Bean
    public FilterRegistrationBean projectTraceFilterReg() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ProjectTraceFilter());
        registration.addUrlPatterns("/*");
        registration.setName(ProjectTraceFilter.class.getSimpleName());
        registration.setOrder(20000);
        return registration;
    }
}