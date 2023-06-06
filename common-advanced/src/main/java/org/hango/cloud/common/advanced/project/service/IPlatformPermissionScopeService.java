package org.hango.cloud.common.advanced.project.service;

import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;

import java.util.Collection;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/10/28
 */
public interface IPlatformPermissionScopeService {

    /**
     * 获取平台的租户、项目信息
     *
     * @param projectIds
     * @return
     */
    List<PermissionScopeDto> getPermissionScope(Collection<Long> projectIds);


    /**
     * 根据项目code获取项目的详细描述
     *
     * @param projectCode 项目code
     * @return 项目信息对象
     */
    PermissionScopeDto getPermissionScope(String projectCode);
}
