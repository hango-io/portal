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
 * @date 2022/4/13
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONPolymorphism {

    int order() default Integer.MAX_VALUE;

    /**
     * json key识别, 当存在该key， 指定使用该类
     * 使用any模式匹配该数组
     * 优先级高于{@link #order()}
     * PS: 该功能暂未实现
     */
    String[] keyDiscern() default {};
}
