package org.hango.cloud.dashboard.apiserver.util;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.web.holder.RequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LogTraceUUIDHolder {
    public static final String LOG_TRACE_PREFIX = "==== uuid:";
    public static final String LOG_TRACE_KEY = "logTraceUUID";
    private static final Logger logger = LoggerFactory.getLogger(LogTraceUUIDHolder.class);
    private static final String REQUEST_UUID_KEY = "request-uuid";

    public static String getUUIDId() {
        String uuid = (String) RequestContextHolder.getValue(REQUEST_UUID_KEY);
        if (StringUtils.isBlank(uuid)) {
            uuid = UUID.randomUUID().toString();
            logger.info("=============UUIDHolder获取uuid为空，请检查==============");
        }
        return uuid;
    }

    public static void setUUIDId(String uuid) {
        RequestContextHolder.setValue(REQUEST_UUID_KEY, uuid);
    }

//	/*
//	 * 获取taskId，生成的uuid格式为不带-的，新增代码不要调用该方法
//	 */
//	public static String getTaskId() {
//		String uuid = (String) RequestContextHolder.getValue(REQUEST_UUID_KEY);
//		if (StringUtils.isBlank(uuid)){
//			RequestContextHolder.setValue(REQUEST_UUID_KEY, UUIDGenerator.getUUID());
//			logger.info("=============UUIDHolder获取uuid为空，请检查==============");
//		}else if (uuid.contains("-")) {
//			RequestContextHolder.setValue(REQUEST_UUID_KEY, UUIDGenerator.getUUID());
//		} 
//		return (String) RequestContextHolder.getValue(REQUEST_UUID_KEY);
//	}

}
