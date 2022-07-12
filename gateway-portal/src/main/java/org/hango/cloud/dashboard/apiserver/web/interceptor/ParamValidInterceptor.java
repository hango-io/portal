package org.hango.cloud.dashboard.apiserver.web.interceptor;

import com.netease.libs.holder.WebContextHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.hango.cloud.dashboard.apiserver.exception.ParamValidException;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangbaojun
 * @version $Id: ParamValidInterceptor.java, v 1.0 2018年07月26日 16:32
 */
//@Aspect
//@Order(1)
//@Component
public class ParamValidInterceptor {

    @Pointcut(value = "execution(*.org.hango.cloud.dashboard.apiserver.web.controller.*(..))")
    public void validPointCut() {
    }

    @Around("validPointCut()")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] params = joinPoint.getArgs();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        WebContextHolder.setRequest(attributes.getRequest());
        WebContextHolder.setResponse(attributes.getResponse());

        //如果不存在参数，直接进行下一步
        if (ArrayUtils.isEmpty(params)) {
            return handlerProcess(joinPoint);
        }
        //声明参数类型
        BindingResult result = null;
        //参数赋值
        for (Object param : params) {
            if (param instanceof BindingResult) {
                result = (BindingResult) param;
            }

        }
        //CASE 1 ：不存在BindingResult对象
        //CASE 2 ：BindingResult对象不存在错误
        //RETURN NEXT
        if (null == result || !result.hasErrors()) {
            return handlerProcess(joinPoint);
        }
        AbstractController.apiReturn(getErrorCode(result.getAllErrors().get(0)));
        return null;
    }


    private ErrorCode getErrorCode(ObjectError error) {
        String field = ((FieldError) error).getField();
        switch (error.getCode()) {
            case "NotNull":
                return CommonErrorCode.MissingParameter(field);
            case "NotEmpty":
                return CommonErrorCode.MissingParameter(field);
//            case "Length":
//                return CommonApiErrorCode.OutOfBounds(field);
            case "Pattern":
                return CommonErrorCode.InvalidJSONFormat(field);
        }
        return CommonErrorCode.InvalidJSONFormat(field);
    }

    private Object handlerProcess(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            if (throwable instanceof ParamValidException) {
                String[] param = throwable.getMessage().split(",");
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("Code", param[0]);
                errorMap.put("Message", param[1]);
                AbstractController.apiReturn(Integer.valueOf(param[2]), param[0], param[1], errorMap);
                return null;
            } else {
                throw throwable;
            }
        }
    }
}
