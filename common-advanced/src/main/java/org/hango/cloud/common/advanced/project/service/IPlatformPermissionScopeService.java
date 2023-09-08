package org.hango.cloud.common.advanced.project.service;

import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    /**
     * 根据项目id获取项目的详细描述
     *
     * @param projectId 项目ID
     * @return 项目信息对象
     */
    PermissionScopeDto getProjectScopeDto(long projectId);

    /**
     * 创建服务过滤条件Map
     * 服务的过滤条件在本方法中扩展
     * 过滤条件的格式在gportal和api-plane两侧统一，过滤条件的key必须为xxx_的前缀开头，参考"Const.PREFIX_LABEL"
     * 需要对endpoint的什么字段过滤就加上什么前缀，当前共5种前缀，详见"Const.PREFIX_XXX"，过滤Map结构如下
     * {
     * "label_projectCode": "project1", // 过滤label为"projectCode=project1"的endpoint
     * "label_application": "app1",     // 过滤label为"application=app1"的endpoint
     * "action": "function",            // 无效标签，可填写但不使用
     * "host_xxx": "qz.com"             // host值为"qz.com"的endpoint
     * "port_xxx": "8080"               // port值为"8080"的endpoint
     * }
     *
     * @param registry 注册中心
     * @return 服务过滤条件Map
     */
    Map<String, String> createServiceFilters(String registry);
}
