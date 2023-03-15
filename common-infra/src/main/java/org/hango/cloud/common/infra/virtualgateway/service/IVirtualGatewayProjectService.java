package org.hango.cloud.common.infra.virtualgateway.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.SingleVgBindDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayBindDto;

import java.util.List;

public interface IVirtualGatewayProjectService {
    /**
     * 根据项目id获取项目的详细描述
     *
     * @param projectId
     * @return
     */
    PermissionScopeDto getProjectScope(long projectId);

    /**
     * 根据租户id查询项目列表
     *
     * @param tenantId 租户id
     * @return {@link List<PermissionScopeDto>} 项目列表
     */
    List<PermissionScopeDto> getProjectScopeList(long tenantId);


    /**
     * 关联项目
     *
     * @param virtualGatewayBind
     * @return
     */
    ErrorCode checkBindProject(VirtualGatewayBindDto virtualGatewayBind);


    /**
     * 关联项目
     *
     * @param virtualGatewayBind
     * @return
     */
    long bindProject(VirtualGatewayBindDto virtualGatewayBind);

    /**
     * 取消关联项目检查
     *
     * @param virtualGwId
     * @param projectId
     * @return
     */
    ErrorCode checkUnBindProject(long virtualGwId, long projectId);

    /**
     * 取消关联项目
     *
     * @param virtualGwId
     * @param projectId
     * @return
     */
    long unbindProject(long virtualGwId, long projectId);

    /**
     * 获取关联项目列表
     *
     * @param query
     * @return
     */
    List<SingleVgBindDto> getBindList(QueryVirtualGatewayDto query);


    /**
     * 统计关联项目数
     *
     * @param query
     * @return
     */
    long countBindList(QueryVirtualGatewayDto query);


    /**
     * 更新域名状态（解绑）
     *
     * @param virtualGwId
     * @param projectId
     */
    void updateUnbindDomainStatus(long virtualGwId, long projectId);


    /**
     * 更新域名状态（绑定）
     *
     * @param virtualGatewayBind
     */
    void updateBindDomainStatus(VirtualGatewayBindDto virtualGatewayBind);

}
