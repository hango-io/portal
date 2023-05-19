package org.hango.cloud.common.infra.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/20
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoadOrder {

    /**
     * 加载顺序，越小越先加载， 第一位的bean将被设置为{@link org.springframework.context.annotation.Primary}
     *
     * @return
     */
    int order() default Integer.MAX_VALUE;

    /**
     * 实现的接口，通过哪个接口进行调用该实现类
     *
     * @return
     */
    Class<?>[] implFor();
}
