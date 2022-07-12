package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.webservice.EnvoyServiceWsdlInfo;

public interface EnvoyServiceWsdlInfoDao extends IBaseDao<EnvoyServiceWsdlInfo> {
    /**
     * 根据serviceId删除Wsdl
     *
     * @param gwId      网关id
     * @param serviceId 服务Id
     * @return 0
     */
    int deleteByServiceId(long gwId, long serviceId);

    /**
     * 根据serviceId获取EnvoyServiceWsInfo
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @return EnvoyServiceWsInfo
     */
    EnvoyServiceWsdlInfo getByServiceId(long gwId, long serviceId);
}
