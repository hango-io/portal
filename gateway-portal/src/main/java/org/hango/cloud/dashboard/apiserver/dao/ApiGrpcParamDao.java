package org.hango.cloud.dashboard.apiserver.dao;

import org.hango.cloud.dashboard.apiserver.meta.grpc.ApiGrpcParam;


/**
 * Dao层
 *
 * @Author: TC_WANG
 * @Date: 2019/7/2
 */
public interface ApiGrpcParamDao extends IBaseDao<ApiGrpcParam> {
    /**
     * 根据apiId删除grpc参数
     *
     * @param apiId
     * @return
     */
    int delete(long apiId);
}
