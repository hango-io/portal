package org.hango.cloud.envoy.infra.webservice.dao;


import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyServiceWsdlInfo;

public interface EnvoyServiceWsdlInfoDao extends IBaseDao<EnvoyServiceWsdlInfo> {
    /**
     * 根据serviceId删除Wsdl
     *
     * @param virtualGwId      网关id
     * @param serviceId 服务Id
     * @return 0
     */
    int deleteByServiceId(long virtualGwId, long serviceId);

    /**
     * 根据serviceId获取EnvoyServiceWsInfo
     *
     * @param serviceId 服务id
     * @return EnvoyServiceWsInfo
     */
    EnvoyServiceWsdlInfo getByServiceId(long serviceId);
}
