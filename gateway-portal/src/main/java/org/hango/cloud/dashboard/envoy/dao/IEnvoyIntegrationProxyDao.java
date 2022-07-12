package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationProxyInfo;

public interface IEnvoyIntegrationProxyDao extends IBaseDao<EnvoyIntegrationProxyInfo> {

    /**
     * 根据集成id查找
     *
     * @param integrationId
     * @return
     */
    public EnvoyIntegrationProxyInfo getByIntegrationId(long integrationId);

}
