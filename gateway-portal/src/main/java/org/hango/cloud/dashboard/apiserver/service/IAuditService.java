package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.AuditInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.AuditQuery;
import org.hango.cloud.dashboard.apiserver.meta.audit.CallStatisticsInfo;
import org.springframework.data.domain.Page;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/3/28
 */
public interface IAuditService {

    /**
     * 聚合查询调用统计数据
     *
     * @param auditQuery
     * @param gatewayInfo
     * @return
     */
    CallStatisticsInfo getAuditStatisticsInfo(AuditQuery auditQuery, GatewayInfo gatewayInfo);

    /**
     * 根据条件分页获取审计数据
     *
     * @param auditQuery
     * @param gatewayInfo
     * @return
     */
    Page<AuditInfo> getAuditInfoList(AuditQuery auditQuery, GatewayInfo gatewayInfo);

    /**
     * 获取审计详情
     *
     * @param id
     * @param auditIndex
     * @param gatewayInfo
     * @return
     */
    String getAuditDetail(String id, String auditIndex, GatewayInfo gatewayInfo);

    /**
     * 统计审计数据
     *
     * @param auditQuery
     * @param gatewayInfo
     * @return
     */
    long countAuditInfo(AuditQuery auditQuery, GatewayInfo gatewayInfo);
}
