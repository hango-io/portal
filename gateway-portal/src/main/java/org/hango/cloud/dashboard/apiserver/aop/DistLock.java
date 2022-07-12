package org.hango.cloud.dashboard.apiserver.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注意：当Redis不可用时，所有节点都得不到执行。
 *
 * @author Feng Changjian (hzfengchj@corp.netease.com)
 * @version $Id: DistLock.java, v 1.0 2015年7月21日 下午8:44:06
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistLock {
    String value() default "";

    String expire() default "30";
}
