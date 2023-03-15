package org.hango.cloud.common.infra.serviceproxy.dao;


import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/25
 */
public interface IServiceProxyDao extends IBaseDao<ServiceProxyInfo> {

    /**
     * 批量通过服务id查询已发布服务数量
     *
     * @param virtualGwId      网关id
     * @param serviceId 服务id
     * @param projectId 项目id
     * @return long已发布服务数量
     */
    long getCount(long virtualGwId, List<Long> serviceId, long projectId);

    /**
     * 批量通过服务id查询已发布服务
     *
     * @param virtualGwId      网关id
     * @param serviceId 服务id list
     * @param projectId 项目id
     * @param offset    查询offset
     * @param limit     查询limit
     * @return 查询已发布服务元数据list
     */
    List<ServiceProxyInfo> getServiceProxyByLimit(long virtualGwId, List<Long> serviceId, long projectId, long offset,
                                                  long limit);

    /**
     * 分页查询网关发布（关联）服务相关
     *
     * @param virtualGwId      网关id
     * @param serviceId 分页查询服务元数据id
     * @param projectId 项目id
     * @param offset    分页查询offset
     * @param limit     分页查询limit
     * @return 查询网关服务元数据list
     */
    List<ServiceProxyInfo> getServiceProxyByLimit(long virtualGwId, long serviceId, long projectId, long offset, long limit);


    /**
     * 根据网关id、服务id列表批量查询已发布服务信息
     *
     * @param virtualGwId          网关id
     * @param serviceIdList serviceIdList 服务id列表
     * @return {@link List< ServiceProxyInfo >} 指定网关中指定服务列表的所有已发布服务信息
     */
    List<ServiceProxyInfo> batchGetServiceProxyList(long virtualGwId, List<Long> serviceIdList);


    /**
     * 更新版本号
     */
    long updateVersion(long id, long version);
}
