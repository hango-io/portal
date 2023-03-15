package org.hango.cloud.common.infra.base.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.LogTraceUUIDHolder;
import org.hango.cloud.common.infra.base.holder.RequestContextHolder;
import org.hango.cloud.common.infra.base.holder.ResponseBodyHolder;
import org.hango.cloud.common.infra.base.meta.Pair;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.gdashboard.api.meta.errorcode.AbstractErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id: AbstractController.java, v 1.0 2017年3月24日 上午11:29:44
 */
@Component
public class AbstractController {

    public static final String RESULT = "Result";

    public static final String TOTAL_COUNT = "TotalCount";

    public static final String TOTAL = "Total";

    protected static final Logger logger = LoggerFactory.getLogger(AbstractController.class);

    public static String apiReturn(int statusCode, String code, String message, Map<String, Object> params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("RequestId", LogTraceUUIDHolder.getUUIDId());
        if (StringUtils.isNotBlank(code)) {
            body.put("Code", code);
        }
        if (StringUtils.isNotBlank(message)) {
            body.put("Message", message);
        }
        if (!CollectionUtils.isEmpty(params)) {
            body.putAll(params);
        }
        HttpServletResponse response = RequestContextHolder.getResponse();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MappingJackson2JsonView.DEFAULT_CONTENT_TYPE);
        response.setStatus(statusCode);
        try {
            //将ResponseBody 放入ThreadLocal ，供操作审计使用
            Pair<String, String> pair = new Pair<>(ResponseBodyHolder.ACTION, JSON.toJSONString(body));
            ResponseBodyHolder.set(pair);
            response.getWriter().write(JSON.toJSONString(body, SerializerFeature.WriteMapNullValue));
        } catch (IOException e) {
            logger.warn("io exception.", e);
        }
        return null;
    }

    public static String apiReturn(Result result) {
        HttpServletResponse response = RequestContextHolder.getResponse();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MappingJackson2JsonView.DEFAULT_CONTENT_TYPE);
        response.setStatus(result.getErrorCode().getStatusCode());
        try {
            //将ResponseBody 放入ThreadLocal ，供操作审计使用
            Pair<String, String> pair = new Pair<>(ResponseBodyHolder.ACTION, JSON.toJSONString(result));
            ResponseBodyHolder.set(pair);
            response.getWriter().write(JSON.toJSONString(result));
        } catch (IOException e) {
            logger.warn("io exception.", e);
        }
        return null;
    }

    public static String apiReturn(AbstractErrorCode errorCode) {
        return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), null);
    }

    public String apiReturnSuccess(Map<String, Object> params) {
        ErrorCode errorCode = CommonErrorCode.SUCCESS;
        return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), params);
    }

    public String apiReturnSuccess(Object object) {
        Map<String, Object> result = Maps.newHashMap();
        result.put(RESULT, object);
        return apiReturnSuccess(result);
    }

    public String apiReturn(AbstractErrorCode errorCode, Map<String, Object> params) {
        return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), params);
    }
}
