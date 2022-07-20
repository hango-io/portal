package org.hango.cloud.dashboard.apiserver.web.holder;

import org.hango.cloud.dashboard.apiserver.web.filter.RequestContextHolderFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 请求上下文, 配合相关辅助类实现
 * 如果需要在Filter中使用该类中的变量,
 * 请注意与{@link RequestContextHolderFilter}
 * 之间的顺序问题
 * Created by Zhranklin zhangwu at 2017/8/18
 */
public class RequestContextHolder {

    public static final String GLANCE_HEADER = "glance_header";
    protected static final String REQUEST_KEY = "__request";
    protected static final String RESPONSE_KEY = "__response";
    protected static final ThreadLocal<Map<String, Object>> values = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(RequestContextHolder.class);

    public static Object getValue(String key) {
        Map<String, Object> map = values.get();
        if (map == null) {
            logger.warn("未在RequestContextFilter周期中调用getValue方法");
            return null;
//			throw new IllegalStateException("请在RequestContextFilter周期中调用getValue方法!");
        }
        return map.get(key);
    }

    public static void setValue(String key, Object value) {
        Map<String, Object> map = values.get();
        if (map == null) {
            throw new IllegalStateException("请在RequestContextFilter周期中调用setValue方法!");
        } else {
            map.put(key, value);
        }
    }

    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) getValue(REQUEST_KEY);
    }

    public static HttpServletResponse getResponse() {
        return (HttpServletResponse) getValue(RESPONSE_KEY);
    }

}