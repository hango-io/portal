package org.hango.cloud.dashboard.envoy.service;

import com.netease.cloud.nsf.step.Step;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationProxyInfo;

/**
 * 集成proxy对应Service层
 */
public interface IEnvoyIntegrationProxyService {

    /**
     * 根据id检验要发布的集成的参数是否符合要求
     *
     * @param integrationId 集成id
     * @param gwId          网关id
     * @return {@link ErrorCode} 当校验正确时返回Success，失败时返回相应的原因
     */
    public ErrorCode checkPublishParam(long integrationId, long gwId);

    /**
     * 发布集成
     *
     * @param integrationId 集成id
     * @param gwId          网关id
     * @return {@link ErrorCode} 当发布正确时返回Success，失败时返回相应的原因
     */
    public ErrorCode publishIntegration(long integrationId, long gwId);

    /**
     * 上下限集成规则时自动发布服务和路由
     *
     * @param integrationInfo      集成id
     * @param integrationProxyInfo 要发布的集成info
     * @param step                 集成step
     * @param gwId                 网关id
     * @return 发布成功返回true，不超过返回false
     */
    public boolean automaticallyPublishServicesAndRoutes(EnvoyIntegrationInfo integrationInfo, EnvoyIntegrationProxyInfo integrationProxyInfo, Step step, long gwId);

    /**
     * 根据id检验要下线的集成的参数是否符合要求
     *
     * @param integrationId 集成id
     * @return {@link ErrorCode} 当校验正确时返回Success，失败时返回相应的原因
     */
    public ErrorCode checkDeleteParam(long integrationId);

    /**
     * 下线集成
     *
     * @param integrationId 集成id
     * @return
     */
    public ErrorCode offlineIntegration(long integrationId);

    /**
     * 自动下线集成发布时使用的路由和服务
     *
     * @param integrationProxy 要下线的集成proxy
     * @return
     */
    public boolean automaticOfflineServiceAndRoutes(EnvoyIntegrationProxyInfo integrationProxy);

}
