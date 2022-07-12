package org.hango.cloud.dashboard.apiserver.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.AuditDto;
import org.hango.cloud.dashboard.apiserver.exception.HostUnReachableException;
import org.hango.cloud.dashboard.apiserver.meta.Pair;
import org.hango.cloud.dashboard.apiserver.meta.UserIdentityEntity;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.LogTraceUUIDHolder;
import org.hango.cloud.dashboard.apiserver.util.ResponseBodyHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: zhangzihao1@corp.netease.com
 * @create: 2018-08-24
 **/

@Aspect
@Component
public class AuditAdvice {

    private static Logger logger = LoggerFactory.getLogger(AuditAdvice.class);
    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private ApiServerConfig apiServerConfig;

    @Around("@annotation(audit)")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint, Audit audit) {
        try {
            proceedingJoinPoint.proceed();
            if (apiServerConfig.getAuditEnable()) {
                logger.info("Start Record Audit Information ... ");
                String url = apiServerConfig.getAuditUrl();
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = attributes.getRequest();
                HttpServletResponse response = attributes.getResponse();
                if (StringUtils.isBlank(UserPermissionHolder.getAccountId())) {
                    logger.info("请求AccountId为空，不进行审计，action为:{}", request.getParameter(Const.ACTION));
                    return null;
                }
                AuditDto auditEntityDto = getRequestMap(request, response, audit);
                String auditUrl = url + "/audit?Action=EventReport&Version=2018-07-20";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

                logger.info("The Audit Url is {}", auditUrl);
                restTemplate.postForObject(auditUrl, new HttpEntity<>(JSON.toJSONString(auditEntityDto), headers), String.class);
            }
        } catch (HttpClientErrorException e) {
            logger.info("The audit exception : HttpStatus = {}, ErrorMessage={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (HostUnReachableException e) {
            logger.info("The audit exception : {}", e.getMessage());
            return null;
        } catch (RestClientException e) {
            logger.info("The audit exception : {}", e.getMessage());
            return null;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return null;
    }

    public AuditDto getRequestMap(HttpServletRequest request, HttpServletResponse response, Audit audit) {

        AuditDto auditDto = new AuditDto();
        auditDto.setEventTime(Instant.now().toEpochMilli());
        auditDto.setEventVersion(audit.eventVersion());
        auditDto.setEventSource("API 网关");
        auditDto.setEventName(audit.eventName());
        auditDto.setDescription(audit.description());
        auditDto.setSourceIpAddress(CommonUtil.getIp(request));
        auditDto.setUserAgent("http");
        auditDto.setRequestId(LogTraceUUIDHolder.getUUIDId());
        auditDto.setRequestMethod(request.getMethod());
        auditDto.setRequestParameters(packageRequestParameter(request));
        auditDto.setResponseStatus(response.getStatus());
        auditDto.setEventType(audit.eventType());
        auditDto.setResources(AuditResourceHolder.getAndRemove());

        Pair<String, String> responsePair = ResponseBodyHolder.getAndRemove();
        if (null != responsePair) {
            auditDto.setResponseElements(responsePair.getValue());
            if (HttpServletResponse.SC_OK != response.getStatus()) {
                Map<String, String> map = (Map<String, String>) JSON.parse(responsePair.getValue());
                if (CollectionUtils.isEmpty(map)) {
                    auditDto.setErrorCode("UNKNOWN ERROR");
                    auditDto.setErrorMessage("UNKNOWN ERROR");
                } else {
                    auditDto.setErrorCode(map.get("Code"));
                    auditDto.setErrorMessage(map.get("Message"));
                }
            }
        }
        auditDto.setProjectId(String.valueOf(ProjectTraceHolder.getProId()));
        auditDto.setTenantId(String.valueOf(ProjectTraceHolder.getTenantId()));
        Object envId = request.getAttribute(Const.ENV_ID);
        if (envId != null) {
            auditDto.setEnvId(String.valueOf(envId));
        }
        auditDto.setApiAction(request.getParameter(Const.ACTION));
        auditDto.setApiVersion(request.getParameter(Const.VERSION));
        auditDto.setUrl(request.getRequestURL().toString());
        auditDto.setUserIdentity(new UserIdentityEntity(UserPermissionHolder.getAccountId()));
        return auditDto;
    }

    private String packageRequestParameter(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        Map<String, Object> headers = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            if (HttpHeaders.COOKIE.equalsIgnoreCase(key)) {
                continue;
            }
            headers.put(key, req.getHeader(key));
        }
        json.put("header", headers);

        json.put("queryString", req.getQueryString());

        String requestBody = StringUtils.EMPTY;
        try {
            requestBody = StreamUtils.copyToString(req.getInputStream(), Charset.forName(Const.DEFAULT_ENCODING));
        } catch (IOException e) {
            logger.info("解析 requestBody 时出现异常", e);
        }

        json.put("body", requestBody);
        return JSON.toJSONString(json);
    }


}
