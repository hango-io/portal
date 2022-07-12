package org.hango.cloud.dashboard.apiserver.aop;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.util.AccessUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.handler.AbstractSpecResourceHandler;
import org.hango.cloud.dashboard.envoy.innerdto.SpecResourceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/22
 */
@Aspect
@Component
public class SpecResourceAlterationAdvice {

    private static Logger logger = LoggerFactory.getLogger(AuditAdvice.class);
    @Autowired
    private ApiServerConfig apiServerConfig;

    @Autowired
    private ApplicationContext applicationContext;


    @Around("@annotation(alteration)")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint, SpecResourceAlteration alteration) {
        try {
            proceedingJoinPoint.proceed();
            logger.info("Spec Resource Report ...");
            Class[] clazzArray = alteration.clazz();
            if (ArrayUtils.isEmpty(clazzArray)) {
                return null;
            }
            for (Class clazz : clazzArray) {
                reportSpecResource(clazz);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private void reportSpecResource(Class clazz) {
        AbstractSpecResourceHandler handler = (AbstractSpecResourceHandler) applicationContext.getBean(clazz);
        List<SpecResourceDto> specResources = handler.toSpecResources(handler.getMetas());
        Map<String, String> queryString = new HashMap<>();
        queryString.put("Action", "RefreshSpecResource");
        queryString.put("Version", "2020-06-28");
        queryString.put("ServiceModule", Const.SERVICE_MODULE);
        queryString.put("ResourceType", handler.getResourceType());

        Map<String, String> headers = new HashMap<>();
        headers.put("x-auth-accountId", apiServerConfig.getPermissionScopeAccount());
        AccessUtil.accessFromOtherPlat(apiServerConfig.getSkiffAuthorityAddr(), queryString,
                JSON.toJSONString(specResources), headers, Const.POST_METHOD);
    }
}
