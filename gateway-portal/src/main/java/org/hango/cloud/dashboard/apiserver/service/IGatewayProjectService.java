package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.meta.gateway.PermissionScopeDto;

import java.util.List;

public interface IGatewayProjectService {
    /**
     * 根据项目id获取项目的详细描述
     *
     * @param projectId
     * @return
     */
    PermissionScopeDto getProjectScopeDto(long projectId);

    /**
     * 根据租户id查询项目列表
     *
     * @param tenantId 租户id
     * @return {@link List<PermissionScopeDto>} 项目列表
     */
    List<PermissionScopeDto> getProjectScopeList(long tenantId);
}
