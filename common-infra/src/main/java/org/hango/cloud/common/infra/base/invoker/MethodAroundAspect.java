package org.hango.cloud.common.infra.base.invoker;

import com.google.common.collect.Maps;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/8/11
 */
@Component
@Aspect
public class MethodAroundAspect implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MethodAroundAspect.class);

    /**
     * 基类与对应增强Hooker的对应关系
     * Map<基类,增强Hooker>
     * <p>
     * eg.
     * Map<IGatewayInfoService.class,GatewayHooker>
     */
    private static final Map<Class, AbstractInvokeHooker> HOOKER_MAP = Maps.newHashMap();

    @Autowired
    private ApplicationContext context;

    @Override
    public void run(String... strings) throws Exception {
        Map<String, AbstractInvokeHooker> beans = context.getBeansOfType(AbstractInvokeHooker.class);
        if (CollectionUtils.isEmpty(beans)) {
            logger.info("not find any hooker");
            return;
        }
        HOOKER_MAP.putAll(beans.values().stream().collect(
                Collectors.groupingBy(AbstractInvokeHooker::aimAt,
                        Collectors.collectingAndThen(Collectors.minBy(
                                Comparator.comparing(AbstractInvokeHooker::getOrder)), Optional::get)
                )));
    }

    /**
     * 方法调用拦截
     * <p>
     * CASE 0: 前置方法调用
     * <p>
     * 前置方法通过方法名及入参类型匹配
     * 前置方法支持重载
     * <p>
     * CASE 1: 后置方法调用
     * <p>
     * 后置方法编写以被拦截方法的返回为入参
     * 如需被拦截方法的入参，请使用线程变量中存储的方法入参
     *
     * @param point
     * @return
     * @throws Throwable
     * @see MethodAroundHolder#getParam()   线程变量中存储的方法入参
     * @see MethodAroundHolder#getReturn()  线程变量中存储的方法返回
     */
    @Around("execution(* org.hango.cloud..service.impl..*(..))")
    @Transactional(rollbackFor = Exception.class)
    public Object aspect(ProceedingJoinPoint point) throws Throwable {

        logger.debug("aspect = {}", point.getSignature().getDeclaringTypeName());

        if (!(point.getSignature() instanceof MethodSignature)) {
            return point.proceed();
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class implementation = signature.getDeclaringType();
        AbstractInvokeHooker invokeHooker = HOOKER_MAP.get(implementation);
        if (invokeHooker == null) {
            return point.proceed();
        }
        String originMethod = signature.getName();
        Object proceed = null;
        try {

            Method preMethod = invokeHooker.getPreMethod(originMethod);
            if (preMethod != null) {
                preMethod.invoke(invokeHooker, point.getArgs());
            }
            proceed = point.proceed();
            MethodAroundHolder.setParams(point.getArgs());
            MethodAroundHolder.setReturn(proceed);
            Method postMethod = invokeHooker.getPostMethod(originMethod);
            if (postMethod != null) {
                proceed = postMethod.invoke(invokeHooker, proceed);
            }
            MethodAroundHolder.remove();
        } catch (Exception e) {
            logger.error("执行 {} 中对 {}() 的方法增强时出现异常", invokeHooker.getClass().getName(), originMethod);
            throw e.getCause() instanceof ErrorCodeException ? e.getCause() : e;
        }

        return proceed;
    }
}
