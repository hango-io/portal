package org.hango.cloud.dashboard.apiserver.dao;


import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2017/12/26 16:47.
 */
public interface ServiceInfoDao extends IBaseDao<ServiceInfo> {

    /**
     * 通过项目id，按照时间降序获取projectId
     *
     * @param projectId
     * @return
     */
    List<ServiceInfo> getServiceByProjectId(long projectId);

    /**
     * 根据服务标识获取服务
     *
     * @param serviceName
     * @return
     */
    List<ServiceInfo> getServiceByServiceName(String serviceName);

    /**
     * 支持模糊匹配的分页搜索
     *
     * @param pattern
     * @param offset
     * @param limit
     * @param projectId
     * @return
     */
    List<ServiceInfo> getServiceByProjectIdLimit(String pattern, long offset, long limit, long projectId);

    /**
     * 获取模糊匹配的service count
     *
     * @param pattern
     * @param projectId
     * @return
     */
    long getServiceCountByProjectId(String pattern, long projectId);

    /**
     * 根据id删除服务
     *
     * @param serviceId
     */
    void delete(long serviceId);

    long updateStatus(long serviceId);

    /**
     * 查询所有的服务方信息根据时间倒叙
     *
     * @return
     */
    List<ServiceInfo> findAllOrderByCreateDateDesc();

    /**
     * 根据服务名称模糊查询满足条件的id列表
     *
     * @param serviceName 服务名称，实际上对接数据库中display_name字段
     * @param projectId   项目id
     * @return {@link List<Long>} 满足条件的服务id列表
     */
    List<Long> getServiceIdListByDisplayNameFuzzy(String serviceName, long projectId);

    /**
     * 根据服务id列表查询服务详情列表
     *
     * @param serviceIdList 服务id列表
     * @return {@link List<ServiceInfo>} 服务详情列表
     */
    List<ServiceInfo> getServiceInfoList(List<Long> serviceIdList);
}
