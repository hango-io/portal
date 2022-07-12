package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.webservice.EnvoyRouteWsParamInfo;

public interface EnvoyRouteWsParamDao extends IBaseDao<EnvoyRouteWsParamInfo> {
    EnvoyRouteWsParamInfo getByRoute(long gwId, long serviceId, long routeId);
}
