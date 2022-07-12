package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.web.dto.auth.AuthPermissionDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.AuthPermissionListDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.AuthPermissionObjectDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.RouteAuthPermissionDto;
import org.hango.cloud.dashboard.envoy.web.dto.auth.ServiceAuthPermissionDto;

import java.util.List;
import java.util.Map;

public interface IAuthPermissionService {
    /**
     * 添加授权参数校验
     *
     * @param authPermissionDto
     * @return
     */
    ErrorCode checkCreateAuthPermission(AuthPermissionDto authPermissionDto);

    /**
     * 调用service-auth添加授权
     *
     * @param authPermissionDto
     * @return
     */
    Map<String, String> createAuthPermission(AuthPermissionDto authPermissionDto);

    /**
     * 删除授权
     *
     * @param gwId             网关id
     * @param authPermissionId 授权信息id
     * @return 删除结果，true/false
     */
    boolean deleteAuthPermission(long gwId, long authPermissionId);

    AuthPermissionListDto describeAuthPermission(long gwId, long authAccountId, long authorizationObjectId,
                                                 String authorizationObjectType, long offset, long limit);

    List<ServiceAuthPermissionDto> describeServiceAuthList(long gwId, List<String> serviceTagList);

    List<RouteAuthPermissionDto> describeRouteAuthList(long gwId, List<Long> routeIdList);

    List<AuthPermissionObjectDto> describeAuthPermissionList(long gwId, List<String> authorizationObjectList,
                                                             String authorizationObjectType);

    /**
     * 通过网关id获取所有已授权服务id
     *
     * @param gwId 网关id
     * @return List<Long>
     */
    List<Long> getServiceAuthId(long gwId);

    /**
     * 通过路由id获取所有已授权路由id
     *
     * @param gwId 网关id
     * @return List<Long>
     */
    List<Long> getRouteAuthId(long gwId);


    /**
     * 删除授权信息
     *
     * @param gwId                    网关id
     * @param authAccountId           认证id
     * @param authorizationObjectId   授权对象id
     * @param authorizationObjectType 授权类型
     * @return 删除结果， true/false
     */
    boolean deleteAuthPermission(long gwId, long authAccountId, long authorizationObjectId, String authorizationObjectType);
}
