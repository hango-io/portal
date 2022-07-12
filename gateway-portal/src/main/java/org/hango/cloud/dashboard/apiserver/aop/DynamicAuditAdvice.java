package org.hango.cloud.dashboard.apiserver.aop;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.service.IAuditService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.service.impl.EnvoyAuditServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/1
 */
@Aspect
@Component
public class DynamicAuditAdvice {

    public static final Logger logger = LoggerFactory.getLogger(DynamicAuditAdvice.class);
    private static final String PARAM_NAME_GW_ID = "gwId";
    private static final String PARAM_NAME_GW_INFO = "gatewayInfo";
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private ApplicationContext applicationContext;
    private IAuditService auditService;

    @Around("@annotation(dynamicAuditBean)")
    public Object before(ProceedingJoinPoint joinPoint, DynamicAuditBean dynamicAuditBean) throws Throwable {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = ((MethodSignature) signature);
        String[] parameterNames = methodSignature.getParameterNames();
        boolean gwIdExitTag = false;
        String gwId = StringUtils.EMPTY;
        for (int i = 0; i < parameterNames.length; i++) {
            if (PARAM_NAME_GW_ID.equals(parameterNames[i])) {
                gwIdExitTag = true;
                gwId = String.valueOf(joinPoint.getArgs()[i]);
                break;
            }
            if (PARAM_NAME_GW_INFO.equals(parameterNames[i])) {
                gwIdExitTag = true;
                gwId = String.valueOf(((GatewayInfo) joinPoint.getArgs()[i]).getId());
                break;
            }
        }
        if (!gwIdExitTag) {
            return null;
        }
        Method method = methodSignature.getMethod();


        auditService = getBean(gwId);
        Object object = method.invoke(auditService, joinPoint.getArgs());
        return object;
    }


    /**
     * 动态获取实现bean
     *
     * @param gwId
     * @return
     */
    private IAuditService getBean(String gwId) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(NumberUtils.toLong(gwId));
        try {
            if (gatewayInfo != null) {
                switch (gatewayInfo.getAuditDatasourceSwitch()) {
                    case Const.AUDIT_DATASOURCE_ELASTICSEARCH:
                        return applicationContext.getBean(EnvoyAuditServiceImpl.class);
                    default:
                }
            }
        } catch (Exception e) {
            logger.error("GetBean Failed ! Error is {}", e);
        }
        throw new RuntimeException("获取网关数据源失败");
    }

}
