package org.hango.cloud.common.infra.base.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.apache.commons.lang3.BooleanUtils;
import org.hango.cloud.common.infra.base.invoker.DynamicMethodInterceptor;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.operationaudit.recorder.AbstractRecorder;
import org.hango.cloud.common.infra.operationaudit.recorder.FileRecorder;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayProjectImpl;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/13
 */
@Configuration
public class GlobalConfig {

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        // 1.定义一个converters转换消息的对象
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        fastConverter.setSupportedMediaTypes(supportedMediaTypes);
        // 2.添加fastjson的配置信息，比如: 是否需要格式化返回的json数据
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setParserConfig(new JSONParserConfig());
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat,
                SerializerFeature.UseSingleQuotes);
        // 3.在converter中添加配置信息
        fastConverter.setFastJsonConfig(fastJsonConfig);
        // 4.将converter赋值给HttpMessageConverter
        HttpMessageConverter<?> converter = fastConverter;
        // 5.返回HttpMessageConverters对象
        return new HttpMessageConverters(converter);
    }

    @Bean
    public DefaultPointcutAdvisor defaultPointcutAdvisor() {
        DynamicMethodInterceptor dynamicMethodInterceptor = new DynamicMethodInterceptor();
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* org.hango..*service.*(..))");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdvice(dynamicMethodInterceptor);
        return advisor;
    }

    @Bean
    @ConditionalOnMissingBean(IVirtualGatewayProjectService.class)
    public IVirtualGatewayProjectService gatewayProjectService() {
        return new VirtualGatewayProjectImpl();
    }


    @Bean
    @ConditionalOnProperty(name = BaseConst.OPERATION_AUDIT_ENABLE, havingValue = BooleanUtils.TRUE, matchIfMissing = true)
    @ConditionalOnMissingBean(value = {AbstractRecorder.class})
    public AbstractRecorder abstractRecorder() {
        return new FileRecorder();
    }
}
