package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.dto.RegistryCenterDto;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.PublishedDetailDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;

import java.util.List;
import java.util.Map;

public interface IServiceProxyService {


    /**
     * 发布服务，网关服务元数据和envoy网关服务产生关联
     *
     * @param serviceProxyDto envoy发布服务DTO
     * @return 服务发布id
     */
    long publishServiceToGw(ServiceProxyDto serviceProxyDto);

    /**
     * 更新已发布服务，EncoyServiceProxyInfo
     *
     * @param serviceProxyDto envoy更新服务dto
     * @return 更新受影响行数
     */
    long updateServiceToGw(ServiceProxyDto serviceProxyDto);

    /**
     * 发布服务，校验服务发布参数
     *
     * @param serviceProxyDto envoy发布服务dto
     * @return {@link ErrorCode} 参数校验结果
     */
    ErrorCode checkPublishParam(ServiceProxyDto serviceProxyDto);

    /**
     * 更新发布服务，校验更新参数
     *
     * @param serviceProxyDto envoy更新发布服务dto
     * @return {@link ErrorCode} 更新发布服务
     */
    ErrorCode checkUpdatePublishParam(ServiceProxyDto serviceProxyDto);

    /**
     * 根据服务标识分页查询已发布（关联）至envoy相关服务信息,查询结果包括serviceName以及GatewayName
     *
     * @param gwId      网关id
     * @param serviceId 服务元数据服务id
     * @param projectId 分页查询项目id
     * @param offset    分页查询offset
     * @param limit     分页查询limit
     * @return serviceProxyInfo，网关已发布服务
     */
    List<ServiceProxyInfo> getEnvoyServiceProxy(long gwId, long serviceId, long projectId, long offset,
                                                long limit);

    List<ServiceProxyInfo> getEnvoyServiceProxy(long gwId, String pattern, long project, long offset, long limit);

    List<ServiceProxyInfo> getAuthServiceProxyByLimit(long gwId, long serviceId, long projectId, long offset,
                                                      long limit);

    long getServiceProxyCount(long gwId, String pattern, long projectId);

    /**
     * 根据服务id获取发布服务数量
     *
     * @param gwId      网关id
     * @param serviceId 服务元数据服务id
     * @return 服务发布数量
     */
    long getServiceProxyCount(long gwId, long serviceId);

    long getAuthServiceProxyCount(long gwId, long serviceId);

    /**
     * 根据服务发布id删除服务发布信息
     *
     * @param id 服务发布id
     */
    void deleteServiceProxy(long id);

    /**
     * 下线已发布服务参数校验
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @return 参数校验结果，{@link ErrorCode}
     */
    ErrorCode checkDeleteServiceProxy(long gwId, long serviceId);

    /**
     * 根据服务发布信息删除服务，需要调用api-plane相关删除crd接口
     *
     * @param gwId      网关id
     * @param serviceId 元数据标识
     * @return 返回删除服务发布结果，true:下线成功，false:下线失败
     */
    boolean deleteServiceProxy(long gwId, long serviceId);

    /**
     * 根据serviceId和gwId查询已发布服务信息
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @return {@link ServiceProxyInfo} 服务发布信息
     */
    ServiceProxyInfo getServiceProxyByServiceIdAndGwId(long gwId, long serviceId);

    ServiceProxyInfo getServiceProxyInterByServiceIdAndGwIds(List<Long> gwIds, long serviceId);

    /**
     * 根据serviceId,gwId，backendService，publishType查询服务发布信息
     *
     * @param gwId           网关id
     * @param serviceId      服务od
     * @param backendService 服务发布信息
     * @param publishType    服务发布类型
     * @return {@link ServiceProxyInfo} 服务发布信息
     */
    ServiceProxyInfo getServiceProxyByServicePublishInfo(long gwId, long serviceId, String backendService, String publishType);

    /**
     * 查询某一服务的已发布服务相关信息，查询结果包括serviceName以及GatewayName
     *
     * @param serviceId
     * @return
     */
    List<ServiceProxyInfo> getServiceProxyByServiceId(long serviceId);

    /**
     * 通过envoyServiceProxyInfo构造EnvoyServiceProxyDto
     *
     * @param serviceProxyInfo EnvoyServiceProxyInfo信息
     * @return EnvoyServiceProxyDto
     */
    ServiceProxyDto fromMeta(ServiceProxyInfo serviceProxyInfo);

    /**
     * 返回包含服务健康状态的DTO
     *
     * @param serviceProxyInfo
     * @param querySource      默认为空，当querySource为NSF时，仅筛选出 以动态方式发布且为Eureka注册中心 的 NSF同步服务
     * @return
     */
    ServiceProxyDto fromMetaWithStatus(ServiceProxyInfo serviceProxyInfo, String querySource);

    /**
     * 包含返回Port的DTO
     *
     * @param serviceProxyInfo
     * @return
     */
    ServiceProxyDto fromMetaWithPort(ServiceProxyInfo serviceProxyInfo);

    /**
     * 根据服务id查询已发布服务所发布的网关
     *
     * @param serviceId 服务id
     * @return {@link GatewayDto}
     */
    List<GatewayDto> getPublishedServiceGateway(long serviceId);


    /**
     * 查询服务subset是否被已发布路由规则引用，如果存在引用，则返回第一个引用的路由规则名称
     *
     * @param serviceProxyDto
     * @return
     */
    ErrorCode getRouteRuleNameWithServiceSubset(ServiceProxyDto serviceProxyDto);

//    /**
//     * 为dto增加版本信息，因为db中存储的是字符串，dto中是list，不能直接用BeanUtil.copy来赋值
//     * 用于前端展示
//     *
//     * @param serviceProxyInfo
//     * @return
//     */
//    List<EnvoySubsetDto> setSubsetForDto(ServiceProxyInfo serviceProxyInfo);

    /**
     * 当需要将版本信息发送到APIPlane时，采用此方法生成subset，因为subset在DR中的名称和用户输入的不同，需要加上-{gwClusterName}
     *
     * @param serviceProxyDto
     * @param gwClusterName
     * @return
     */
    List<EnvoySubsetDto> setSubsetForDtoWhenSendToAPIPlane(ServiceProxyDto serviceProxyDto, String gwClusterName);

    /**
     * 根据网关id查询该网关中所有的已发布服务信息
     *
     * @param gwId 网关id
     * @return {@link List< ServiceProxyInfo >} 指定网关中的所有已发布服务信息
     */
    List<ServiceProxyInfo> getServiceProxyListByGwId(long gwId);

    /**
     * 根据网关id、服务id列表批量查询已发布服务信息
     *
     * @param gwId          网关id
     * @param serviceIdList 服务id列表
     * @return {@link List< ServiceProxyInfo >} 指定网关中指定服务列表的所有已发布服务信息
     */
    List<ServiceProxyInfo> batchGetServiceProxyList(long gwId, List<Long> serviceIdList);

    /**
     * 当需要发送给APIPlane时BackendService，要根据服务注册中心的类型进行调整
     *
     * @param serviceProxyDto
     * @return
     */
    String getBackendServiceSendToApiPlane(ServiceProxyDto serviceProxyDto);

    /**
     * 获取subsetsName
     *
     * @param serviceProxyInfo
     * @return
     */
    List<String> getSubsetsName(ServiceProxyInfo serviceProxyInfo);

    /**
     * 获取服务已发布相关信息，服务管理页列表展示
     *
     * @param serviceId 服务id
     * @return {@link PublishedDetailDto}
     */
    List<PublishedDetailDto> getPublishedDetailByService(long serviceId);

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
    Map<String, String> createServiceFilters(RegistryCenterDto registry);
}
