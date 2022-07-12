package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.grpc.EnvoyServiceProtobuf;


/**
 * Dao层
 *
 * @Author: TC_WANG
 */
public interface EnvoyServiceProtobufDao extends IBaseDao<EnvoyServiceProtobuf> {

    /**
     * 根据serviceId删除pb
     *
     * @param serviceId
     * @return
     */
    int delete(long serviceId);
}
