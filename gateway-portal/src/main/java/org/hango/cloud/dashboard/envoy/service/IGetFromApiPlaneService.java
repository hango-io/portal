package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.dto.DubboMetaDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPublishServiceDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyServiceWithPortDto;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;

import java.util.List;
import java.util.Map;

public interface IGetFromApiPlaneService {

    /**
     * 获取api-plane对应集群中的服务
     *
     * @param name                模糊查询服务名
     * @param gwId                网关id（根据id查询网关所属的api-plane）
     * @param registryCenterType  注册中心类型
     * @param registryCenterAlias 注册中心别名
     * @param serviceFilters      服务过滤条件map
     * @return 服务list
     */
    List<EnvoyServiceWithPortDto> getServiceListFromApiPlane(long gwId, String name, String registryCenterType, String registryCenterAlias, Map<String, String> serviceFilters);

    /**
     * 发布服务，网关服务元数据和envoy网关服务产生关联
     *
     * @param serviceProxyDto envoy发布服务DTO
     * @return 服务发布id
     */
    boolean publishServiceByApiPlane(ServiceProxyDto serviceProxyDto, EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo);

    /**
     * 通过api-plane 下线服务
     *
     * @param apiPlaneAddr           api-plane地址
     * @param envoyPublishServiceDto
     * @return 下线结果
     */
    boolean offlineServiceByApiPlane(String apiPlaneAddr, EnvoyPublishServiceDto envoyPublishServiceDto);

    /**
     * 通过api-plane 发布服务
     *
     * @param routeRuleProxyInfo   路由ProxyInfo
     * @param pluginConfigurations 插件配置
     * @return 发布结果
     */
    boolean publishRouteRuleByApiPlane(RouteRuleProxyInfo routeRuleProxyInfo, List<String> pluginConfigurations);

    /**
     * 通过api-plane 下线路由
     *
     * @param routeRuleProxyInfo routeRuleInfo
     * @return 下线结果 true/fale
     */
    boolean deleteRouteRuleByApiPlane(RouteRuleProxyInfo routeRuleProxyInfo);

    /**
     * 通过api-plane 下获取Dubbo Meta元数据信息
     *
     * @param gwId            网关ID
     * @param igv             接口+版本+分组 {interface:group:version}
     * @param applicationName 应用名称
     * @param method          dubbo方法
     * @return
     */
    List<DubboMetaDto> getDubboMetaListByApIPlane(long gwId, String igv, String applicationName, String method);

}
