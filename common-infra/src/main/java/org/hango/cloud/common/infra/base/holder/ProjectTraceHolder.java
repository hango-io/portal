package org.hango.cloud.common.infra.base.holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hanjiahao
 * project_id相关的配置项
 */
public class ProjectTraceHolder {

    private static final Logger logger = LoggerFactory.getLogger(ProjectTraceHolder.class);

    public static final String PROJECT_TRACE_ID = "x-auth-projectId";
    public static final String TENANT_TRACE_ID = "X-auth-tenantId";
    public static final long DEFAULT_PROJECT_ID = 0;
    private static ThreadLocal<Long> projectTraceId = new ThreadLocal<>();
    private static ThreadLocal<Long> tenantTraceId = new ThreadLocal<>();

    public static long getProId() {
        return projectTraceId.get() == null ? 0L : projectTraceId.get();
    }

    public static void setProId(long proId) {
        ProjectTraceHolder.projectTraceId.set(proId);
    }

    public static void removeProId() {
        ProjectTraceHolder.projectTraceId.remove();
    }

    public static long getTenantId() {
        return tenantTraceId.get();
    }

    public static void setTenantId(long tenantId) {
        ProjectTraceHolder.tenantTraceId.set(tenantId);
    }

    public static void removeTenantId() {
        ProjectTraceHolder.tenantTraceId.remove();
    }

}
