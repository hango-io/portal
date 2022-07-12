package org.hango.cloud.dashboard.apiserver.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 具体资源变更监控注解
 * @date 2020/7/21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpecResourceAlteration {

    /**
     * 资源变更时获取所有该资源类别的方法的所在类
     *
     * @return
     */
    Class[] clazz();
}
