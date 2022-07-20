package org.hango.cloud.gdashboard.api.dao;


import org.hango.cloud.gdashboard.api.meta.OperationLog;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/4/24 19:56.
 */
public interface OperationLogDao extends IBaseDao<OperationLog> {

    List<OperationLog> findAll(long objectId, String type, long limit, long offset);

}
