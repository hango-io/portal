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
 * @date 2022/8/11
 */
@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HookerAspect {
    /**
     * 需前置调用的Hooker中对应的method
     *
     * @return
     * @see org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker 及其子类
     */
    String preHookMethod() default "";


    /**
     * 需后置调用的Hooker中对应的method
     *
     * @return
     * @see org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker 及其子类
     */
    String postHookMethod() default "";
}
