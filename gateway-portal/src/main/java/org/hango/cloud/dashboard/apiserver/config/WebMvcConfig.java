package org.hango.cloud.dashboard.apiserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Weng Yanghui (wengyanghui)
 * @version $Id: Const.java, v 1.0 2018/1/24
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/reports/**")
                .addResourceLocations("classpath:/reports/");

        registry.addResourceHandler("/api-docs/**")
                .addResourceLocations("classpath:/api-docs/");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
