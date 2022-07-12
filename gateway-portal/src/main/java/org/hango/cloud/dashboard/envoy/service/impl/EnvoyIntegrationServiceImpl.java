package org.hango.cloud.dashboard.envoy.service.impl;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyIntegrationInfoDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyIntegrationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 集成Service层
 */
@Service
public class EnvoyIntegrationServiceImpl implements IEnvoyIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationServiceImpl.class);

    @Autowired
    private IEnvoyIntegrationInfoDao envoyIntegrationInfoDao;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IRouteRuleInfoService envoyRouteRuleInfoService;

    @Override
    public ErrorCode checkAddParam(EnvoyIntegrationInfo envoyIntegrationInfo) {
        if (envoyIntegrationInfo.getIntegrationName() == null || envoyIntegrationInfo.getIntegrationName().length() == 0) {
            logger.info("集成名称为空，不能创建");
            return CommonErrorCode.MissingParameter("IntegrationName");
        }
        if (!checkSameName(envoyIntegrationInfo.getIntegrationName(), envoyIntegrationInfo.getProjectId())) {
            logger.info("同名集成已存在，不能创建");
            return CommonErrorCode.AlreadyExistIntegrationName;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean checkSameName(String integrationName, long projectId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("integrationName", integrationName);
        params.put("projectId", projectId);
        List<EnvoyIntegrationInfo> records = envoyIntegrationInfoDao.getRecordsByField(params);
        if (records.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long addIntegration(EnvoyIntegrationInfo envoyIntegrationInfo) {
        envoyIntegrationInfo.setCreateTime(System.currentTimeMillis());
        envoyIntegrationInfo.setUpdateTime(System.currentTimeMillis());
        return envoyIntegrationInfoDao.add(envoyIntegrationInfo);
    }

    @Override
    public ErrorCode checkUpdateParams(EnvoyIntegrationInfo envoyIntegrationInfo) {
        if (envoyIntegrationInfo.getId() == 0) {
            logger.info("更新集成，集成ID为空，无法更新");
            return CommonErrorCode.MissingParameter("IntegrationId");
        }
        if (envoyIntegrationInfo.getType() != null && envoyIntegrationInfo.getType().length() > 0) {
            if (!envoyIntegrationInfo.getType().equals("sub") && !envoyIntegrationInfo.getType().equals("main")) {
                logger.info("更新集成，集成类型错误，非sub或main");
                return CommonErrorCode.InvalidParameterValue(envoyIntegrationInfo.getType(), "type");
            }
        }
        EnvoyIntegrationInfo dbInfo = envoyIntegrationInfoDao.get(envoyIntegrationInfo.getId());
        if (dbInfo == null) {
            logger.info("更新集成，未找到对应ID的集成，无法更新");
            return CommonErrorCode.NoSuchIntegration;
        }
        if (dbInfo.getPublishStatus() == 1) {
            logger.info("更新集成，集成已经发布，无法更新");
            return CommonErrorCode.CannotUpdateIntegrationRule;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode updateIntegration(EnvoyIntegrationInfo envoyIntegrationInfo) {
        EnvoyIntegrationInfo dbInfo = envoyIntegrationInfoDao.get(envoyIntegrationInfo.getId());
        if (dbInfo == null) {
            logger.error("更新集成时id指定的集成已不存在，请检查! id:{}", envoyIntegrationInfo.getId());
            return CommonErrorCode.NoSuchIntegration;
        }

        //设置参数
        dbInfo.setUpdateTime(System.currentTimeMillis());
        dbInfo.setDescription(envoyIntegrationInfo.getDescription());
        dbInfo.setStep(envoyIntegrationInfo.getStep());
        if (envoyIntegrationInfo.getType() != null && envoyIntegrationInfo.getType().length() > 0) {
            dbInfo.setType(envoyIntegrationInfo.getType());
        }
        if (envoyIntegrationInfoDao.update(dbInfo) != 1) {
            return CommonErrorCode.InternalServerError;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkDeleteParam(long id) {
        EnvoyIntegrationInfo dbInfo = envoyIntegrationInfoDao.get(id);
        if (dbInfo == null) {
            logger.info("删除集成，未找到对应ID的集成，无法删除");
            return CommonErrorCode.NoSuchIntegration;
        }
        if (dbInfo.getPublishStatus() == 1) {
            logger.info("删除集成，集成已发布，无法删除");
            return CommonErrorCode.CannotDeleteIntegration;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode deleteIntegration(long id) {
        EnvoyIntegrationInfo dbInfo = envoyIntegrationInfoDao.get(id);
        if (dbInfo != null) {
            if (envoyIntegrationInfoDao.delete(dbInfo) != 1) {
                return CommonErrorCode.InternalServerError;
            } else {
                return CommonErrorCode.Success;
            }
        } else {
            return CommonErrorCode.NoSuchIntegration;
        }
    }

    @Override
    public long getIntegrationInfoCount(long projectId, String pattern, String type) {
        if (type == null || type.length() == 0) {
            return envoyIntegrationInfoDao.getIntegrationInfoCount(projectId, pattern);
        } else {
            return envoyIntegrationInfoDao.getIntegrationInfoCount(projectId, pattern, type);
        }
    }

    @Override
    public List<EnvoyIntegrationInfo> getEnvoyIntegrationInfoByPattern(long projectId, String pattern, long offset, long limit, String type) {
        if (type == null || type.length() == 0) {
            return envoyIntegrationInfoDao.getEnvoyIntegrationInfoByLimit(projectId, pattern, offset, limit);
        } else {
            return envoyIntegrationInfoDao.getEnvoyIntegrationInfoByLimit(projectId, pattern, offset, limit, type);
        }
    }

    @Override
    public EnvoyIntegrationDto fromMeta(EnvoyIntegrationInfo integrationInfo) {
        EnvoyIntegrationDto integrationDto = new EnvoyIntegrationDto();
        integrationDto.setId(integrationInfo.getId());
        integrationDto.setIntegrationName(integrationInfo.getIntegrationName());
        integrationDto.setIntegrationDescription(integrationInfo.getDescription());
        integrationDto.setCreateTime(integrationInfo.getCreateTime());
        integrationDto.setPublishTime(integrationInfo.getPublishTime());
        integrationDto.setUpdateTime(integrationInfo.getUpdateTime());
        integrationDto.setPublishStatus(integrationInfo.getPublishStatus());
        integrationDto.setStep(integrationInfo.getStep());
        integrationDto.setType(integrationInfo.getType());
        return integrationDto;
    }

    @Override
    public EnvoyIntegrationInfo getIntegrationInfoById(long id) {
        return envoyIntegrationInfoDao.get(id);
    }

    @Override
    public long updateAll(EnvoyIntegrationInfo envoyIntegrationInfo) {
        return envoyIntegrationInfoDao.update(envoyIntegrationInfo);
    }

    @Override
    public List<EnvoyIntegrationInfo> getByIdlist(List<Long> idList) {
        Map<String, Long> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        for (int i = 0; i < idList.size(); i++) {
            params.put("id" + i, idList.get(i));
        }
        return envoyIntegrationInfoDao.getByIdlist(params);
    }

    @Override
    public ErrorCode checkDescribeParam(long offset, long limit) {
        return CommonUtil.checkOffsetAndLimit(offset, limit);
    }

    @Override
    public ErrorCode checkType(String type) {
        if (type.equals("sub") || type.equals("main")) {
            return CommonErrorCode.Success;
        } else {
            return CommonErrorCode.InvalidParameterValue(type, "type");
        }
    }
}