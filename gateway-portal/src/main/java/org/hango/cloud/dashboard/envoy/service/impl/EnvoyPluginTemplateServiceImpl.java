package org.hango.cloud.dashboard.envoy.service.impl;

import com.google.common.collect.Lists;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyPluginTemplateDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginTemplateInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginTemplateService;
import org.hango.cloud.dashboard.envoy.service.cache.PluginCacheService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginTemplateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 插件模板service层实现类
 *
 * @author hzchenzhongyang 2020-04-08
 */
@Service
public class EnvoyPluginTemplateServiceImpl implements IEnvoyPluginTemplateService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyPluginTemplateServiceImpl.class);

    @Autowired
    private IEnvoyPluginTemplateDao envoyPluginTemplateDao;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @Autowired
    private PluginCacheService pluginCacheService;

    @Override
    public ErrorCode checkCreatePluginTemplate(EnvoyPluginTemplateInfo templateInfo) {
        List<EnvoyPluginTemplateInfo> pluginTemplateInfos = getTemplateByName(templateInfo.getTemplateName(), templateInfo.getProjectId());
        if (!CollectionUtils.isEmpty(pluginTemplateInfos)) {
            logger.info("创建模板时，项目下已存在同名模板/全局模版已存在");
            return CommonErrorCode.SameNamePluginTemplateExist;
        }

        return CommonErrorCode.Success;
    }

    private List<EnvoyPluginTemplateInfo> getTemplateByName(String templateName, long projectId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("templateName", templateName);
        params.put("projectId", projectId);
        return envoyPluginTemplateDao.getRecordsByField(params);
    }

    @Override
    public long createPluginTemplate(EnvoyPluginTemplateInfo templateInfo) {
        templateInfo.setCreateTime(System.currentTimeMillis());
        templateInfo.setUpdateTime(System.currentTimeMillis());
        templateInfo.setTemplateVersion(1);
        return envoyPluginTemplateDao.add(templateInfo);
    }

    @Override
    public EnvoyPluginTemplateInfo getTemplateById(long id) {
        return envoyPluginTemplateDao.get(id);
    }

    @Override
    public ErrorCode checkUpdatePluginTemplate(EnvoyPluginTemplateInfo templateInfo) {
        EnvoyPluginTemplateInfo templateInDB = getTemplateById(templateInfo.getId());
        if (null == templateInDB) {
            logger.info("更新模板详情时，指定模板不存在! id:{}", templateInfo.getId());
            return CommonErrorCode.NoSuchPluginTemplate;
        }

        List<EnvoyPluginTemplateInfo> pluginTemplateInfos = getTemplateByName(templateInfo.getTemplateName(), templateInfo.getProjectId());
        if (!CollectionUtils.isEmpty(pluginTemplateInfos)) {
            for (EnvoyPluginTemplateInfo template : pluginTemplateInfos) {
                if (template.getId() != templateInfo.getId()) {
                    logger.info("修改模板时，项目下已存在同名模板/全局模板已存在同名模版");
                    return CommonErrorCode.SameNamePluginTemplateExist;
                }
            }
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean updatePluginTemplate(EnvoyPluginTemplateInfo templateInfo) {
        EnvoyPluginTemplateInfo templateInDB = getTemplateById(templateInfo.getId());
        if (null == templateInDB) {
            return false;
        }
        if (!StringUtils.isEmpty(templateInfo.getPluginConfiguration())) {
            if (!templateInfo.getPluginConfiguration().equals(templateInDB.getPluginConfiguration())) {
                templateInDB.setPluginConfiguration(templateInfo.getPluginConfiguration());
                templateInDB.setTemplateVersion(templateInDB.getTemplateVersion() + 1);
            }
        }
        if (!StringUtils.isEmpty(templateInfo.getTemplateName())) {
            templateInDB.setTemplateName(templateInfo.getTemplateName());
        }
        if (null != templateInfo.getTemplateNotes()) {
            templateInDB.setTemplateNotes(templateInfo.getTemplateNotes());
        }
        templateInDB.setUpdateTime(System.currentTimeMillis());
        envoyPluginTemplateDao.update(templateInDB);
        return true;
    }

    @Override
    public ErrorCode checkDeletePluginTemplate(long id) {
        return CommonErrorCode.Success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePluginTemplate(long id) {
        EnvoyPluginTemplateInfo envoyPluginTemplateInfo = getTemplateById(id);
        if (null == envoyPluginTemplateInfo) {
            return true;
        }

        List<EnvoyPluginBindingInfo> envoyPluginBindingInfos = envoyPluginInfoService.getBindingListByTemplateId(id);
        List<Long> idList = envoyPluginBindingInfos.stream().map(EnvoyPluginBindingInfo::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(idList)) {
            envoyPluginInfoService.batchDissociateTemplate(idList);
        }
        envoyPluginTemplateDao.delete(envoyPluginTemplateInfo);
        return true;
    }

    @Override
    public long getPluginTemplateInfoCount(long projectId, String pluginType) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("projectId", projectId);
        if (!StringUtils.isEmpty(pluginType)) {
            params.put("pluginType", pluginType);
        }
        return envoyPluginTemplateDao.getCountByFields(params);
    }

    @Override
    public List<EnvoyPluginTemplateInfo> getPluginTemplateInfoList(long projectId, String pluginType, long offset, long limit) {
        return envoyPluginTemplateDao.getPluginTemplateInfoList(projectId, pluginType, offset, limit);
    }

    @Override
    public ErrorCode checkSyncTemplate(long id, List<Long> pluginBindingInfoIds) {
        EnvoyPluginTemplateInfo templateInfo = getTemplateById(id);
        if (null == templateInfo) {
            logger.info("指定的模板id不存在! id:{}", id);
            return CommonErrorCode.NoSuchPluginTemplate;
        }
        List<EnvoyPluginBindingInfo> envoyPluginBindingInfoList = envoyPluginInfoService.batchGetById(pluginBindingInfoIds);
        Set<Long> pluginBindingInfoIdSet = envoyPluginBindingInfoList.stream().map(EnvoyPluginBindingInfo::getId).collect(Collectors.toSet());
        Set<Long> noExistIdSet = pluginBindingInfoIds.stream().filter(item -> !pluginBindingInfoIdSet.contains(item)).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(noExistIdSet)) {
            logger.info("有不存在的插件绑定关系! noExistIdSet:{}", noExistIdSet);
            return CommonErrorCode.NoSuchPluginBinding;
        }
        List<Long> notBindingList = envoyPluginBindingInfoList.stream().filter(item -> item.getTemplateId() != id).map(EnvoyPluginBindingInfo::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(notBindingList)) {
            logger.info("有不存在的插件绑定关系! noExistIdSet:{}", noExistIdSet);
            return CommonErrorCode.NoSuchPluginBinding;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public List<EnvoyPluginBindingInfo> syncTemplate(long id, List<Long> pluginBindingInfoIds) {
        EnvoyPluginTemplateInfo templateInfo = getTemplateById(id);
        if (null == templateInfo) {
            return null;
        }

        List<EnvoyPluginBindingInfo> bindingInfos = envoyPluginInfoService.batchGetById(pluginBindingInfoIds);
        return bindingInfos.stream().filter(item -> {
            if (item.getTemplateVersion() != templateInfo.getTemplateVersion()) {
                return !envoyPluginInfoService.updatePluginConfiguration(item.getId(), templateInfo.getPluginConfiguration(), templateInfo.getId(), templateInfo.getTemplateVersion());
            }
            return false;
        }).collect(Collectors.toList());
    }

    @Override
    public List<EnvoyPluginTemplateInfo> batchGet(List<Long> templateId) {
        List<EnvoyPluginTemplateInfo> templateInfos = envoyPluginTemplateDao.batchGet(templateId);
        return CollectionUtils.isEmpty(templateInfos) ? Lists.newArrayList() : templateInfos;
    }

    @Override
    public EnvoyPluginTemplateDto fromMeta(EnvoyPluginTemplateInfo templateInfo) {
        EnvoyPluginTemplateDto templateDto = new EnvoyPluginTemplateDto();
        templateDto.setId(templateInfo.getId());
        templateDto.setTemplateNotes(templateInfo.getTemplateNotes());
        templateDto.setProjectId(templateInfo.getProjectId());
        templateDto.setCreateTime(templateInfo.getCreateTime());
        templateDto.setUpdateTime(templateInfo.getUpdateTime());
        templateDto.setPluginType(templateInfo.getPluginType());
        templateDto.setTemplateName(templateInfo.getTemplateName());
        templateDto.setTemplateVersion(templateInfo.getTemplateVersion());
        templateDto.setPluginConfiguration(templateInfo.getPluginConfiguration());
        templateDto.setPluginName(pluginCacheService.getPluginNameFromCache(templateInfo.getPluginType()));
        // 是否为全局插件，全局插件 true
        if (templateInfo.getProjectId() == 0) {
            templateDto.setGlobal(true);
        }
        return templateDto;
    }

}
