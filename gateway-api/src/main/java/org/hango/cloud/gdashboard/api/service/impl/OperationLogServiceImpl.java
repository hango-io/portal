package org.hango.cloud.gdashboard.api.service.impl;

import org.hango.cloud.gdashboard.api.dao.OperationLogDao;
import org.hango.cloud.gdashboard.api.meta.OperationLog;
import org.hango.cloud.gdashboard.api.service.IOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志 相关操作
 *
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/4/25 15:57.
 */
@Service
public class OperationLogServiceImpl implements IOperationLogService {

    @Autowired
    private OperationLogDao operationLogDao;

    @Override
    public long addApiOperationLog(OperationLog operationLog) {
        return operationLogDao.add(operationLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addApiOperationLog(List<OperationLog> operationLogList) {
        if (!CollectionUtils.isEmpty(operationLogList)) {
            for (OperationLog operationLog : operationLogList) {
                operationLogDao.add(operationLog);
            }
        }
    }


    @Override
    public List<OperationLog> listApiOperationLog(long objectId, String type, long limit, long offset) {
        return operationLogDao.findAll(objectId, type, limit, offset);
    }

    @Override
    public long getCount(long objectId, String type) {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", objectId);
        params.put("type", type);
        return operationLogDao.getCountByFields(params);
    }

    @Override
    public OperationLog getOperationLog(long createDate, String accountId, long objectId, String type, String operation) {
        OperationLog operationLog = new OperationLog();
        operationLog.setCreateDate(createDate);
        operationLog.setEmail(accountId);
        operationLog.setObjectId(objectId);
        operationLog.setType(type);
        operationLog.setOperation(operation);
        return operationLog;
    }

}
