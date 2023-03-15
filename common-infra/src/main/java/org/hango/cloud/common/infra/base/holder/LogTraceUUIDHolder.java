package org.hango.cloud.common.infra.base.holder;

import org.apache.commons.lang3.StringUtils;
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

}
