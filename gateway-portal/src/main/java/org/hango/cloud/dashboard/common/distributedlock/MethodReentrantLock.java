package org.hango.cloud.dashboard.common.distributedlock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁注解（Redisson实现）
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
