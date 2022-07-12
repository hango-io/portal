package org.hango.cloud.dashboard.apiserver.service.apigateway;

import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;

/**
 * 发布和下线相关接口
 * 发布服务，下线服务等相关，gportal与api网关进行交互
 */
public interface IPublishOfflineService {

    /**
     * 下线网关api
     *
     * @param gwAddr
     * @param apiId
     * @param type
     * @return
     */
    boolean offlineApiFromGateway(String gwAddr, String apiId, String type);

    /**
     * 下线网关服务
     *
     * @param gwAddr
     * @param serviceEnName
     * @return
     */
    boolean offlineServiceFromGateway(String gwAddr, String serviceEnName);

    /**
     * 下线API，从网关侧下线，从gporta侧删除数据库记录
     *
     * @param gatewayInfo
     * @param apiInfo
     * @return
     */
    boolean offlineApi(GatewayInfo gatewayInfo, ApiInfo apiInfo);

    /**
     * 下线服务，从网关侧下线，从gportal侧删除数据库记录
     *
     * @param gatewayInfo
     * @param serviceInfo
     * @return
     */
    boolean offlineService(GatewayInfo gatewayInfo, ServiceInfo serviceInfo);
}
