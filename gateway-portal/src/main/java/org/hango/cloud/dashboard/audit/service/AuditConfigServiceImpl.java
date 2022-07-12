package org.hango.cloud.dashboard.audit.service;

import com.alibaba.fastjson.JSON;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.audit.meta.AuditMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 配置变更相关审计
 * 记录日志相关audit
 *
 * @author hanjiahao
 */
@Service
public class AuditConfigServiceImpl implements IAuditConfigService {

    private static final Logger auditLogger = LoggerFactory.getLogger("audit");
    @Autowired
    private ApiServerConfig apiServerConfig;

    @Override
    public void record(AuditMetaData auditMetaData) {
        if (apiServerConfig.getConfigUpdateAudit()) {
            auditLogger.info("配置审计日志 auditInfo:{}", JSON.toJSONString(auditMetaData));
        }
    }
}
