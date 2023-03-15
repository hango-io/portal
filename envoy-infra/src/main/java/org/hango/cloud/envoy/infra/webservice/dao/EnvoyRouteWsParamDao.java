package org.hango.cloud.envoy.infra.webservice.dao;


import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyRouteWsParamInfo;

public interface EnvoyRouteWsParamDao extends IBaseDao<EnvoyRouteWsParamInfo> {
    EnvoyRouteWsParamInfo getByRoute(long virtualGwId, long serviceId, long routeId);
}
