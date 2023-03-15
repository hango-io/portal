package org.hango.cloud.common.infra.service.service;


import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.meta.ServiceInfo;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/5
 */
public interface IServiceInfoService extends CommonService<ServiceInfo, ServiceDto> {

    /**
     * 通过项目id获取所有服务
     *
     * @param projectId
     * @return
     */
    List<ServiceDto> findAllServiceByProjectId(long projectId);

    /**
     * 分页获取项目id下的所有服务
     *
     * @param pattern
     * @param offset
     * @param limit
     * @param projectId
     * @return
     */
    List<ServiceDto> findAllServiceByProjectIdLimit(String pattern, long offset, long limit, long projectId);


    /**
     * 通过服务显示名称获取服务列表信息
     *
     * @param pattern
     * @param status
     * @param projectId
     * @return
     */
    List<ServiceDto> findAllServiceByDisplayName(String pattern, int status, long projectId);


    /**
     * 获取项目id下的服务数量
     *
     * @param pattern   pattern支持模糊搜索
     * @param projectId
     * @return
     */
    long getServiceCountByProjectId(String pattern, long projectId);


    /**
     * 根据服务Id判断服务是否存在
     *
     * @param serviceId
     * @return
     */
    boolean isServiceExists(long serviceId);

    /**
     * 根据服务标识查询
     *
     * @param serviceName
     * @return
     */
    ServiceDto getServiceByServiceName(String serviceName);

    /**
     * 通过服务标识查询服务信息
     *
     * @param serviceName
     * @param projectId
     * @return
     */
    ServiceDto getServiceByServiceNameAndProject(String serviceName, long projectId);

    /**
     * 判断服务显示名称是否存在
     *
     * @param displayName
     * @return
     */
    boolean isDisplayNameExists(String displayName);

    /**
     * 根据服务显示名称，projectId信息获取所属projectId下的服务
     *
     * @param displayName
     * @param projectId
     * @return
     */
    ServiceDto describeDisplayName(String displayName, long projectId);

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
     * @return {@link List<ServiceDto>} 服务详情列表
     */
    List<ServiceDto> getServiceDtoList(List<Long> serviceIdList);
}
