package org.hango.cloud.envoy.infra.base.aop;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hango.cloud.common.infra.base.dto.ResourceDTO;
import org.hango.cloud.common.infra.base.mapper.RouteRuleProxyMapper;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyPO;
import org.hango.cloud.common.infra.serviceproxy.dao.IServiceProxyDao;
import org.hango.cloud.envoy.infra.plugin.dto.GatewayPluginDto;
import org.hango.cloud.envoy.infra.serviceproxy.dto.DpServiceProxyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/22 15:21
 **/
@Aspect
@Component
public class VersionManagerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(VersionManagerAdvice.class);


    @Autowired
    private IServiceProxyDao serviceProxyDao;

    @Autowired
    private RouteRuleProxyMapper routeRuleProxyMapper;

    @Autowired
    private IPluginBindingInfoDao pluginBindingInfoDao;

    private static final List<String> VERSION_INCREMENT_BLACKLIST = Arrays.asList("RePublishService", "RePublishRouteRule", "RePublishPlugin", "RePublishRouteDto", "RePublishPlugin");


    @Around("@annotation(versionManager)")
    public Object publishProxyToApiPlane(ProceedingJoinPoint pjp, VersionManager versionManager) throws Throwable {
        Object[] args = pjp.getArgs();
        logger.info("start handle version args:{}", JSONObject.toJSONString(args));
        //设置版本号
        try {
            if (needHandle(args)){
                preHandle((ResourceDTO)args[args.length-1], args[args.length-2]);
            }
        } catch (Exception e) {
            logger.error("prehandle version error, args:{}", args, e);
        }
        //执行方法
        Object proceed;
        try {
            proceed = pjp.proceed();
        } catch (Throwable e) {
            logger.error("handle version error args:{}", JSONObject.toJSONString(args), e);
            throw e;
        }
        //更新DB版本号
        try {
            if (needHandle(args) && proceed instanceof Boolean){
                postHandle((Boolean) proceed, (ResourceDTO)args[args.length-1]);
            }
        } catch (Exception e) {
            logger.error("posthandle update version error, args:{}", args, e);
        }
        return proceed;
    }


    private boolean needHandle(Object[] args){
        return args.length > 2 && args[args.length-1] instanceof ResourceDTO;
    }

    private void preHandle(ResourceDTO resourceDTO, Object body){
        Long version = resourceDTO.getResourceVersion();
        if (needUpdate()){
            version += 1;
        }
        resourceDTO.setResourceVersion(version);
        switch (resourceDTO.solveResourceType()) {
            case Route:
                JSONObject route = (JSONObject) body;
                route.put("Version", version);
                break;
            case Service:
                DpServiceProxyDto service = (DpServiceProxyDto) body;
                service.setVersion(version);
                break;
            case Plugin:
                GatewayPluginDto plugin =  (GatewayPluginDto) body;
                plugin.setVersion(version);
                break;
            default:
                logger.error("preHandle | resource type invalid, resourceType:{}", resourceDTO.getResourceType());
        }
    }

    private void postHandle(Boolean responseSuccess, ResourceDTO resourceDTO){
        //返回成功才更新版本号
        if (!responseSuccess || !needUpdate()) {
            return;
        }
        Long version = resourceDTO.getResourceVersion();
        switch (resourceDTO.solveResourceType()) {
            case Route:
                RouteRuleProxyPO proxyPO = RouteRuleProxyPO.builder().version(version).id(resourceDTO.getResourceId()).build();
                routeRuleProxyMapper.updateById(proxyPO);
                break;
            case Service:
                serviceProxyDao.updateVersion(resourceDTO.getResourceId(), version);
                break;
            case Plugin:
                pluginBindingInfoDao.updateVersion(resourceDTO.getResourceId(), version);
                break;
            default:
                logger.error("[postHandle]resource type invalid, resourceType:{}", resourceDTO.getResourceType());
        }
    }


    public boolean needUpdate(){
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)){
            return false;
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)attributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String action = request.getParameter("Action");
        if (StringUtils.isEmpty(action)){
            return false;
        }
        return !VERSION_INCREMENT_BLACKLIST.contains(action);
    }



}


