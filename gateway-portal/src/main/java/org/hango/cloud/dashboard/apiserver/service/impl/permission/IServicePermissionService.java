package org.hango.cloud.dashboard.apiserver.service.impl.permission;

import org.hango.cloud.dashboard.apiserver.meta.enums.permission.ActionPermissionEnum;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/6/11
 */
public interface IServicePermissionService {

    /**
     * 判断用户是否有访问接口的权限
     *
     * @param token   账号jwt
     * @param request 请求request
     * @return
     */
    boolean hasRole(String token, HttpServletRequest request);

    /**
     * 判断用户是否有访问接口的权限
     *
     * @param token                账号jwt
     * @param permissionScopeId    权限作用域
     * @param actionPermissionEnum 鉴权Action枚举
     * @return
     */
    boolean hasRoleWithToken(String token, String permissionScopeId, ActionPermissionEnum actionPermissionEnum);

    /**
     * 判断用户是否有访问接口的权限
     *
     * @param account              账号ID
     * @param permissionScopeId    权限作用域
     * @param actionPermissionEnum 鉴权Action枚举
     * @return
     */
    boolean hasRoleWithAccount(String account, String permissionScopeId, ActionPermissionEnum actionPermissionEnum);


    /**
     * 判断是否有权限访问该项目
     *
     * @param account
     * @param tenantId
     * @param projectId
     * @param offset
     * @return
     */
    boolean hasAccessAuthority(String account, long tenantId, long projectId, long offset);


}
