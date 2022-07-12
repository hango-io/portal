package org.hango.cloud.dashboard.apiserver.service.apigateway;

import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayAddrConfigInfo;
import org.hango.cloud.dashboard.apiserver.meta.ResultEntity;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;

import java.util.Map;

public interface IGetInfoFromGatewayService {


    /**
     * 获取网关所属环境的审计数据源地址
     *
     * @param gwAddr
     * @return
     */
    Map<String, String> getDataSourceAddr(String gwAddr);


    /**
     * 获取网关所属环境的认证中心地址以及环境信息
     *
     * @param gwAddr
     * @return
     */
    Map<String, String> getAuthEnv(String gwAddr);

    int getHealthFromGateway(String gwAddr, String healthAddr);

    /**
     * 检查网关的配置，同时同步网关mongo地址
     * 认证中心地址以及环境信息
     *
     * @param gwAddr
     * @param healthAddr
     * @return
     */
    ResultEntity<ErrorCode, GatewayAddrConfigInfo> checkAndUpdateFromGateway(long gwId, String gwAddr, String healthAddr);

    /**
     * 检查配置auth信息的有效性，检查认证中心是否配置有效
     *
     * @param gwAddr
     * @return
     */
    boolean checkAuthConfig(String gwAddr);
}
