package org.hango.cloud.dashboard.audit.service;

import org.hango.cloud.dashboard.audit.meta.AuditMetaData;


/**
 * 配置审计相关interface
 */
public interface IAuditConfigService {
    /**
     * 记录相关配置审计
     *
     * @param auditMetaData 审计相关MetaData
     */
    void record(AuditMetaData auditMetaData);
}
