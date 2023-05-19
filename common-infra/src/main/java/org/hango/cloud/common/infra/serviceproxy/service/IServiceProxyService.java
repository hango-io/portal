package org.hango.cloud.common.infra.serviceproxy.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyUpdateDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;

import java.util.List;
import java.util.Set;

public interface IServiceProxyService extends CommonService<ServiceProxyInfo, ServiceProxyDto> {

    /**
     * 获取数据面对应集群中的服务
     *
     * @param name               模糊查询服务名
     * @param virtualGwId        网关id（根据id查询网关所属的数据面地址）
     * @param registryCenterType 注册中心类型
     * @return 服务list
     */
    List<BackendServiceWithPortDto> getBackendServicesFromDataPlane(long virtualGwId, String name, String registryCenterType);


    /**
     * 根据条件分页查询已发布（关联）相关服务信息
     * 不支持分页,不支持模糊查询
     *
     * @param query 查询条件
     * @return serviceProxyInfo，网关已发布服务
     */
    List<ServiceProxyDto> getServiceProxy(ServiceProxyQuery query);

    /**
     * 基于域名查询服务
     */
    List<ServiceProxyDto> getServiceProxyByHost(String host);

    /**
     * 根据条件分页查询已发布（关联）服务数量
     * 不支持分页,不支持模糊查询
     *
     * @param query 查询条件
     * @return serviceProxyInfo，网关已发布服务
     */
    long countServiceProxy(ServiceProxyQuery query);


    /**
     * 根据条件分页查询已发布（关联）相关服务信息
     *
     * @param query 查询条件
     * @return serviceProxyInfo，网关已发布服务
     */
    Page<ServiceProxyDto> getServiceProxyLimited(ServiceProxyQuery query);

    /**
     * 根据条件分页查询已发布（关联）相关服务信息
     *
     * @param query 查询条件
     * @return serviceProxyInfo，网关已发布服务
     */
    Page<ServiceProxyDto> getServiceProxyWithPort(ServiceProxyQuery query);



    /**
     * 根据网关id查询该网关中所有的已发布服务信息
     *
     * @param virtualGwId 网关id
     * @return {@link List< ServiceProxyInfo >} 指定网关中的所有已发布服务信息
     */
    List<ServiceProxyDto> getServiceProxyListByVirtualGwId(long virtualGwId);


    /**
     * 获取所有服务标识
     *
     * @param virtualGwId
     * @return
     */
    List<String> getAllServiceTag(long virtualGwId);

    /**
     * 将服务的域名整合去重
     *
     * @param serviceDtoList 服务集合
     * @return 去重域名集合
     */
    Set<String> getUniqueHostListFromServiceList(List<ServiceProxyDto> serviceDtoList);

    /**
     * 将服务的域名整合去重
     *
     * @param serviceIdList 服务ID集合
     * @return 去重域名集合
     */
    Set<String> getUniqueHostListFromServiceIdList(List<Long> serviceIdList);

    /**
     * 填充端口信息
     *
     * @param serviceProxyDto
     */
    void fillServicePort(ServiceProxyDto serviceProxyDto);

    /**
     * 通过id列表获取对应的已发布服务信息
     *
     * @param ids
     * @return
     */
    List<ServiceProxyDto> getServiceByIds(List<Long> ids);



    Boolean updateServiceHost(Long id, String host);

    ServiceProxyDto fillServiceProxy(ServiceProxyUpdateDto serviceProxyUpdateDto);

}
