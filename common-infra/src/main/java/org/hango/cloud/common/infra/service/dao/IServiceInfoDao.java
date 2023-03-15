package org.hango.cloud.common.infra.service.dao;


import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.common.infra.service.meta.ServiceInfo;

import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2017/12/26 16:47.
 */
public interface IServiceInfoDao extends IBaseDao<ServiceInfo> {

    /**
     * 通过条件获取服务元信息
     *
     * @param params
     * @param offset
     * @param limit
     * @return
     */
    List<ServiceInfo> getRecordsByField(Map<String, Object> params, long offset, long limit);


    /**
     * 通过项目id，按照时间降序获取projectId
     *
     * @param projectId
     * @return
     */
    List<ServiceInfo> getServiceByProjectId(long projectId);


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
     * 通过显示名称模糊查询该项目下的服务信息
     *
     * @param pattern
     * @param status
     * @param projectId
     * @return
     */
    List<ServiceInfo> findAllServiceByDisplayName(String pattern, int status, long projectId);


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

    /**
     * 更新服务状态
     *
     * @param serviceId
     * @return
     */
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
