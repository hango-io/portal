package org.hango.cloud.dashboard.apiserver.service;


import org.hango.cloud.dashboard.apiserver.dto.alertdto.AlertRuleDto;
import org.hango.cloud.dashboard.apiserver.util.ResultActionWithMessage;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/7/5
 */
public interface IAlertRuleService {

    /**
     * 添加告警规则
     *
     * @param dto
     * @param tenantId
     * @param projectId
     * @param accountId
     * @return
     */
    ResultActionWithMessage addRule(AlertRuleDto dto, long tenantId, long projectId, String accountId);

    /**
     * 删除告警规则
     *
     * @param name
     * @param tenantId
     * @param projectId
     * @param accountId
     * @return
     */
    ResultActionWithMessage deleteRule(String name, long tenantId, long projectId, String accountId);

    /**
     * 获取告警规则
     *
     * @param name
     * @param tenantId
     * @param projectId
     * @param accountId
     * @return
     */
    ResultActionWithMessage getRule(String name, long tenantId, long projectId, String accountId);

    /**
     * 更新告警规则
     *
     * @param dto
     * @param tenantId
     * @param projectId
     * @param accountId
     * @return
     */
    ResultActionWithMessage updateRule(AlertRuleDto dto, long tenantId, long projectId, String accountId);

    /**
     * 获取告警列表
     *
     * @param tenantId
     * @param projectId
     * @param accountId
     * @param sort
     * @param asc
     * @return
     */
    ResultActionWithMessage getRuleList(long tenantId, long projectId, String accountId, String sort, boolean asc, boolean showAll, int offset, int limit);

}
