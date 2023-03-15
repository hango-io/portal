package org.hango.cloud.common.infra.serviceproxy.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;

import java.util.List;

public interface IServiceProxyService extends CommonService<ServiceProxyInfo, ServiceProxyDto> {

    /**
     * 获取数据面对应集群中的服务
     *
     * @param name                模糊查询服务名
     * @param virtualGwId                网关id（根据id查询网关所属的数据面地址）
     * @param registryCenterType  注册中心类型
     * @return 服务list
     */
    List<BackendServiceWithPortDto> getBackendServicesFromDataPlane(long virtualGwId, String name, String registryCenterType);


    /**
     * 根据条件分页查询已发布（关联）相关服务信息
     *
     * @param virtualGwId      网关id
     * @param serviceId 服务元数据服务id
     * @param projectId 分页查询项目id
     * @param offset    分页查询offset
     * @param limit     分页查询limit
     * @return serviceProxyInfo，网关已发布服务
     */
    List<ServiceProxyDto> getServiceProxy(long virtualGwId, long serviceId, long projectId, long offset, long limit);


    /**
     * 根据条件分页查询已发布（关联）相关服务信息
     *
     * @param virtualGwId      网关id
     * @param pattern   模糊查询条件
     * @param projectId 分页查询项目id
     * @param offset    分页查询offset
     * @param limit     分页查询limit
     * @return serviceProxyInfo，网关已发布服务
     */
    List<ServiceProxyDto> getServiceProxy(long virtualGwId, String pattern, long projectId, long offset, long limit);

    /**
     * 根据条件分页查询已发布（关联）相关服务信息
     */
    List<ServiceProxyDto> getServiceProxyWithPort(long virtualGwId, String pattern, long projectId, long offset, long limit);

    /**
     * 统计满足条件的服务数
     *
     * @param virtualGwId      网关ID
     * @param pattern   模糊查询条件
     * @param projectId 项目ID
     * @return
     */
    long getServiceProxyCount(long virtualGwId, String pattern, long projectId);



    /**
     * 根据服务id获取发布服务数量
     *
     * @param virtualGwId      网关id
     * @param serviceId 服务元数据服务id
     * @return 服务发布数量
     */
    long getServiceProxyCount(long virtualGwId, long serviceId);




    /**
     * 根据serviceId和gwId查询已发布服务信息
     *
     * @param virtualGwId      网关id
     * @param serviceId 服务id
     * @return {@link ServiceProxyInfo} 服务发布信息
     */
    ServiceProxyDto getServiceProxyByServiceIdAndGwId(long virtualGwId, long serviceId);

    /**
     * 根据serviceId和gwId查询已发布服务信息
     *
     * @param virtualGwId      网关id
     * @param serviceId 服务id
     * @return {@link ServiceProxyInfo} 服务发布信息
     */
    ServiceProxyInfo getServiceProxyInfo(long virtualGwId, long serviceId);


    /**
     * 查询某一服务的已发布服务相关信息，查询结果包括serviceName以及GatewayName
     *
     * @param serviceId
     * @return
     */
    List<ServiceProxyDto> getServiceProxyByServiceId(long serviceId);

    /**
     * 根据serviceId和gwId查询已发布服务信息
     *
     */
    List<ServiceProxyDto> getServiceProxy(Long virtualGwId, Long serviceId);



    /**
     * 查询服务subset是否被已发布路由规则引用，如果存在引用，则返回第一个引用的路由规则名称
     *
     * @param serviceProxyDto
     * @return
     */
    ErrorCode getRouteRuleNameWithServiceSubset(ServiceProxyDto serviceProxyDto);


    /**
     * 根据网关id查询该网关中所有的已发布服务信息
     *
     * @param virtualGwId 网关id
     * @return {@link List< ServiceProxyInfo >} 指定网关中的所有已发布服务信息
     */
    List<ServiceProxyDto> getServiceProxyListByVirtualGwId(long virtualGwId);

    /**
     * 根据网关id、服务id列表批量查询已发布服务信息
     *
     * @param virtualGwId          网关id
     * @param serviceIdList 服务id列表
     * @return {@link List< ServiceProxyInfo >} 指定网关中指定服务列表的所有已发布服务信息
     */
    List<ServiceProxyDto> batchGetServiceProxyList(long virtualGwId, List<Long> serviceIdList);

    /**
     * 获取subsetsName
     *
     * @param serviceProxyInfo
     * @return
     */
    List<String> getSubsetsName(ServiceProxyDto serviceProxyInfo);


    /**
     * 组装服务发布中的code字段
     *
     * @param serviceProxyDto
     * @return
     * @see ServiceProxyDto#getCode()
     */
    String getServiceCode(ServiceProxyDto serviceProxyDto);



    /**
     * 获取所有服务标识
     *
     * @param virtualGwId
     * @return
     */
    List<String> getAllServiceTag(long virtualGwId);

    void fillServicePort(ServiceProxyDto serviceProxyDto);
}
