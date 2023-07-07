package org.hango.cloud.envoy.infra.serviceproxy.service;

import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/7
 */
public interface IEnvoyServiceProxyService extends CommonService<ServiceProxyInfo, ServiceProxyDto> {


    /**
     * 发布到网关数据面
     *
     * @param serviceProxyDto
     * @return
     */
    boolean publishToGateway(ServiceProxyDto serviceProxyDto);

    /**
     * 发布到网关数据面
     *
     * @param serviceProxyDto
     * @return
     */
    boolean updateToGateway(ServiceProxyDto serviceProxyDto);


    Boolean refreshRouteHost(Long vgId, Long serviceId, String hosts);


    /**
     * 从网关数据面下线
     *
     * @param serviceProxyDto
     * @return
     */
    boolean offlineToGateway(ServiceProxyDto serviceProxyDto);


    /**
     * 删除服务相关信息
     *
     * @param serviceProxyDto
     */
    void deleteService(ServiceProxyDto serviceProxyDto);
    /**
     * 获取查询服务接口的额外参数
     *
     * @param registry 注册中心类型
     * @return 额外的参数
     */
    Map<String, String> getExtraServiceParams(String registry);

    /**
     * 获取api-plane对应集群中的服务
     *
     * @param name               模糊查询服务名
     * @param virtualGwId        网关id（根据id查询网关所属的api-plane）
     * @param registryCenterType 注册中心类型
     * @return 服务list
     */
    List<BackendServiceWithPortDto> getServiceListFromApiPlane(long virtualGwId, String name, String registryCenterType);

    /**
     * 从数据面获取服务的端口
     *
     * @param serviceProxyDto
     * @return
     */
    List<Integer> getBackendServicePorts(ServiceProxyDto serviceProxyDto);


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
    List<String> getSubsetsName(ServiceProxyDto serviceProxyInfo);


}
