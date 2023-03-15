package org.hango.cloud.common.infra.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yutao04
 * @version 1.0
 * @Type
 * @Desc
 * @date 2021/11/23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MethodReentrantLock {
    /**
     * <p>
     * Only working on type = Type.PARAM <br/> 默认使用方法的所有入参方案作为锁隔离资源的key值 设置纳入key值生成的入参下标列表<br/>
     * e.g.<br/>@MethodReentrantLock({0,1,2})<br/> void methodA(int a, String b, Object c)
     * </p>
     *
     * @return
     */
    int[] value() default {};


    /**
     * 获取锁超时时间
     * @return
     */
    int tryLockTimeout() default 0;

    /**
     * 重复执行时，是否中断
     * @return
     */
    boolean interrupt() default false;

    /**
     * 锁作用范围。<br/> METHOD：锁隔离作用于方法<br/> PARAM：锁隔离作用于方法和入参<br/>
     *
     * @return
     */
    Type type() default Type.PARAM;

    enum Type {
        METHOD,
        PARAM
    }
}
