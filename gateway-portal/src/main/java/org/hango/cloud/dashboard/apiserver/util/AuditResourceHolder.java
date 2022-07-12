package org.hango.cloud.dashboard.apiserver.util;

import com.alibaba.fastjson.JSON;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangbaojun
 * @version $Id: AuditResourceHolder.java, v 1.0 2018年09月03日 11:23
 */
public class AuditResourceHolder {

    public static final Logger logger = LoggerFactory.getLogger(AuditResourceHolder.class);

    private static ThreadLocal<List<ResourceDataDto>> auditThreadLocal = new ThreadLocal<>();

    public static void set(List<ResourceDataDto> resources) {
        auditThreadLocal.set(resources);
    }

    public static void set(ResourceDataDto resource) {
        if (resource != null) {
            List<ResourceDataDto> resources = new ArrayList<>();
            resources.add(resource);
            auditThreadLocal.set(resources);

        }
    }

    public static List<ResourceDataDto> get() {
        return auditThreadLocal.get();
    }

    public static List<ResourceDataDto> getAndRemove() {
        List<ResourceDataDto> resources = auditThreadLocal.get();
        logger.info("Method Invoke getAndRemove() , ThreadLocal Value {} Would Be Remove", JSON.toJSONString(resources));
        auditThreadLocal.remove();
        return resources;
    }
}
