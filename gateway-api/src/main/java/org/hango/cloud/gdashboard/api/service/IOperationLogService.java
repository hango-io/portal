package org.hango.cloud.gdashboard.api.service;

import org.hango.cloud.gdashboard.api.meta.OperationLog;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/4/25 15:57.
 */
public interface IOperationLogService {

    /**
     * 添加操作日志
     *
     * @param operationLog
     * @return
     */
    long addApiOperationLog(OperationLog operationLog);


    /**
     * 添加操作日志
     *
     * @param operationLogList
     * @return
     */
    void addApiOperationLog(List<OperationLog> operationLogList);

    /**
     * 查询操作日志，分页
     *
     * @param objectId
     * @param type
     * @param limit
     * @param offset
     * @return
     */
    List<OperationLog> listApiOperationLog(long objectId, String type, long limit, long offset);

    long getCount(long objectId, String type);

    OperationLog getOperationLog(long createDate, String accountId, long objectId, String type, String operation);
}
