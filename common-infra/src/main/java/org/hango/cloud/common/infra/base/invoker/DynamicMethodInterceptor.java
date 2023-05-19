package org.hango.cloud.common.infra.base.invoker;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/5/6
 */
public class DynamicMethodInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        System.out.println("----------------" + methodInvocation.getMethod().getName());
        return methodInvocation.proceed();
    }

}
