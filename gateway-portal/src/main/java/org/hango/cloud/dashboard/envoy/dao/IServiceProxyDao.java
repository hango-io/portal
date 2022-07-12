package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;

import java.util.List;

public interface IServiceProxyDao extends IBaseDao<ServiceProxyInfo> {

    /**
     * 批量通过服务id查询已发布服务数量
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @param projectId 项目id
     * @return long已发布服务数量
     */
    long getCount(long gwId, List<Long> serviceId, long projectId);

    /**
     * 批量通过服务id查询已发布服务
     *
     * @param gwId      网关id
     * @param serviceId 服务id list
     * @param projectId 项目id
     * @param offset    查询offset
     * @param limit     查询limit
     * @return 查询已发布服务元数据list
     */
    List<ServiceProxyInfo> getServiceProxyByLimit(long gwId, List<Long> serviceId, long projectId, long offset,
                                                  long limit);

    /**
     * 分页查询网关发布（关联）服务相关
     *
     * @param gwId      网关id
     * @param serviceId 分页查询服务元数据id
     * @param projectId 项目id
     * @param offset    分页查询offset
     * @param limit     分页查询limit
     * @return 查询网关服务元数据list
     */
    List<ServiceProxyInfo> getServiceProxyByLimit(long gwId, long serviceId, long projectId, long offset, long limit);

    List<ServiceProxyInfo> getServiceProxyByLimit(long gwId, long serviceId, long projectId,
                                                  List<Long> authServiceId, long offset, long limit);

    long getAuthServiceProxyCount(long gwId, long serviceId, long projectId, List<Long> authServiceId);

    /**
     * 根据网关id、服务id列表批量查询已发布服务信息
     *
     * @param gwId          网关id
     * @param serviceIdList serviceIdList 服务id列表
     * @return {@link List< ServiceProxyInfo >} 指定网关中指定服务列表的所有已发布服务信息
     */
    List<ServiceProxyInfo> batchGetServiceProxyList(long gwId, List<Long> serviceIdList);
}
