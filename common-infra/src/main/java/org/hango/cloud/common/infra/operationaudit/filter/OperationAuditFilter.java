package org.hango.cloud.common.infra.operationaudit.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.holder.LogTraceUUIDHolder;
import org.hango.cloud.common.infra.base.holder.ResponseBodyHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpElement;
import org.hango.cloud.common.infra.base.meta.Pair;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.operationaudit.meta.OperationAudit;
import org.hango.cloud.common.infra.operationaudit.meta.OperationAuditKind;
import org.hango.cloud.common.infra.operationaudit.meta.OperationAuditRule;
import org.hango.cloud.common.infra.operationaudit.meta.ResourceDataDto;
import org.hango.cloud.common.infra.operationaudit.meta.ResourceInfoLocation;
import org.hango.cloud.common.infra.operationaudit.recorder.AbstractRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/6
 */
public class OperationAuditFilter extends OncePerRequestFilter {

    private static Logger logger = LoggerFactory.getLogger(OperationAuditFilter.class);

    @Autowired(required = false)
    private AbstractRecorder recorder;

    private ExecutorService executorService = new ThreadPoolExecutor(5, 10, 5,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("audit-record-%d-thread").build());


    @Override
    protected void initFilterBean() throws ServletException {
        FilterConfig filterConfig = getFilterConfig();
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, filterConfig.getServletContext());
        super.initFilterBean();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (recorder == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String action = request.getParameter(BaseConst.ACTION);
        OperationAuditRule operationAuditRule = OperationAuditKind.get(action);
        if (operationAuditRule == null) {
            filterChain.doFilter(request, response);
            return;
        }
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);
        OperationAudit requestMap = getRequestMap(request, responseWrapper, operationAuditRule);
        executorService.execute(() -> {
            try {
                recorder.doRecord(requestMap,request);
            } catch (Throwable throwable) {
                logger.error("audit error , errMsg = {}", throwable.getStackTrace());
            }
        });
        responseWrapper.copyBodyToResponse();
    }


    public OperationAudit getRequestMap(HttpServletRequest request, ContentCachingResponseWrapper response, OperationAuditRule rule) {
        OperationAudit oa = new OperationAudit();
        oa.setEventTime(Instant.now().toEpochMilli());
        oa.setEventVersion(rule.getEventVersion());
        oa.setEventSource("API 网关");
        oa.setEventName(rule.getEventName());
        oa.setDescription(rule.getDescription());
        oa.setSourceIpAddress(CommonUtil.getIp(request));
        oa.setUserAgent(rule.getUserAgent());
        oa.setRequestId(LogTraceUUIDHolder.getUUIDId());
        oa.setRequestMethod(request.getMethod());
        oa.setRequestParameters(packageRequestParameter(request));
        oa.setResponseStatus(response.getStatus());
        oa.setEventType(rule.getEnvType());
        oa.setResources(Lists.newArrayList(buildResourceData(request, response, rule)));
        Pair<String, String> responsePair = ResponseBodyHolder.getAndRemove();
        if (null != responsePair) {
            oa.setResponseElements(responsePair.getValue());
            if (HttpServletResponse.SC_OK != response.getStatus()) {
                Map<String, String> map = (Map<String, String>) JSON.parse(responsePair.getValue());
                if (CollectionUtils.isEmpty(map)) {
                    oa.setErrorCode("UNKNOWN ERROR");
                    oa.setErrorMessage("UNKNOWN ERROR");
                } else {
                    oa.setErrorCode(map.get("Code"));
                    oa.setErrorMessage(map.get("Message"));
                }
            }
        }
        oa.setApiAction(request.getParameter(BaseConst.ACTION));
        oa.setApiVersion(request.getParameter(BaseConst.VERSION));
        oa.setUrl(request.getRequestURL().toString());
        return oa;
    }

    private ResourceDataDto buildResourceData(HttpServletRequest request, ContentCachingResponseWrapper response, OperationAuditRule rule) {
        try {
            ResourceInfoLocation idLocation = rule.getJsonPathForResourceId();
            ResourceDataDto resource = new ResourceDataDto();
            resource.setResourceType(rule.getResourceType());
            if (idLocation != null) {
                idLocation.getLocation();
                resource.setResourceId(readResource(idLocation.getLocation(), idLocation.getJsonPath(), request, response));
            }
            ResourceInfoLocation nameLocation = rule.getJsonPathForResourceName();
            if (nameLocation != null) {
                resource.setResourceName(readResource(nameLocation.getLocation(), nameLocation.getJsonPath(), request, response));
            }
            return resource;
        } catch (IOException e) {
            logger.info("解析 requestBody 时出现异常", e);
        }
        return null;

    }

    private String readResource(HttpElement location, String path, HttpServletRequest request, ContentCachingResponseWrapper response) throws IOException {
        switch (location) {
            case REQUEST:
                return readFromInputStream(request.getInputStream(), path);
            case RESPONSE:
                return readFromInputStream(response.getContentInputStream(), path);
            case QUERY_STRING:
                return readFromQueryString(request, path);
            case REQUEST_HEADER:
                return readFromRequestHeader(request, path);
            default:
                return StringUtils.EMPTY;
        }
    }

    private String readFromInputStream(InputStream inputStream, String path) throws IOException {
        String content = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        Object obj = JSONPath.read(content, path);
        return TypeUtils.cast(obj, String.class, null);
    }

    private String readFromQueryString(HttpServletRequest request, String path) {
        return request.getParameter(path);
    }


    private String readFromRequestHeader(HttpServletRequest request, String path) {
        return request.getHeader(path);
    }

    private String packageRequestParameter(HttpServletRequest req) {
        JSONObject json = new JSONObject();
        Map<String, Object> headers = Maps.newHashMap();
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
            requestBody = StreamUtils.copyToString(req.getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            logger.info("解析 requestBody 时出现异常", e);
        }
        json.put("body", requestBody);
        return JSON.toJSONString(json);
    }
}
