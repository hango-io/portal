package org.hango.cloud.common.infra.plugin.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.plugin.dao.IPluginTemplateDao;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
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
public class PluginTemplateServiceImpl implements IPluginTemplateService {
    private static final Logger logger = LoggerFactory.getLogger(PluginTemplateServiceImpl.class);

    @Autowired
    private IPluginTemplateDao pluginTemplateDao;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Override
    public ErrorCode checkCreateParam(PluginTemplateDto templateInfo) {
        List<PluginTemplateInfo> pluginTemplateInfos = getTemplateByName(templateInfo.getTemplateName(), templateInfo.getProjectId());
        if (!CollectionUtils.isEmpty(pluginTemplateInfos)) {
            logger.info("创建模板时，项目下已存在同名模板/全局模版已存在");
            return CommonErrorCode.SAME_NAME_PLUGIN_TEMPLATE_EXIST;
        }

        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUpdateParam(PluginTemplateDto pluginTemplateDto) {
        PluginTemplateDto pluginTemplateDtoInDB = get(pluginTemplateDto.getId());
        if (null == pluginTemplateDtoInDB) {
            logger.info("更新模板详情时，指定模板不存在! id:{}", pluginTemplateDto.getId());
            return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
        }
        List<PluginTemplateInfo> pluginTemplateInfos = getTemplateByName(pluginTemplateDto.getTemplateName(), pluginTemplateDto.getProjectId());
        if (!CollectionUtils.isEmpty(pluginTemplateInfos)) {
            for (PluginTemplateInfo template : pluginTemplateInfos) {
                if (template.getId() != pluginTemplateDto.getId()) {
                    logger.info("修改模板时，项目下已存在同名模板/全局模板已存在同名模版");
                    return CommonErrorCode.SAME_NAME_PLUGIN_TEMPLATE_EXIST;
                }
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeleteParam(PluginTemplateDto pluginTemplateDto) {
        if (pluginTemplateDto == null) {
            return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
        }
        return CommonErrorCode.SUCCESS;
    }

    private List<PluginTemplateInfo> getTemplateByName(String templateName, long projectId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("templateName", templateName);
        params.put("projectId", projectId);
        return pluginTemplateDao.getRecordsByField(params);
    }

    @Override
    public long create(PluginTemplateDto pluginTemplateDto) {
        pluginTemplateDto.setCreateTime(System.currentTimeMillis());
        pluginTemplateDto.setUpdateTime(System.currentTimeMillis());
        pluginTemplateDto.setTemplateVersion(1);
        return pluginTemplateDao.add(toMeta(pluginTemplateDto));
    }

    @Override
    public PluginTemplateDto get(long id) {
        PluginTemplateDto pluginTemplateDto = toView(pluginTemplateDao.get(id));
        return fillTemplateDto(pluginTemplateDto);
    }

    private PluginTemplateDto fillTemplateDto(PluginTemplateDto pluginTemplateDto) {
        List<PluginBindingDto> bindingInfos = pluginInfoService.getBindingListByTemplateId(pluginTemplateDto.getId());
        if (!CollectionUtils.isEmpty(bindingInfos)) {
            pluginTemplateDto.setBindingDtoList(bindingInfos);
            fillTemplateStatus(pluginTemplateDto, bindingInfos);
        } else {
            pluginTemplateDto.setTemplateStatus(PluginTemplateInfo.STATUS_NO_NEED_SYNC);
            pluginTemplateDto.setBindingDtoList(Lists.newArrayList());
        }
        return pluginTemplateDto;
    }

    private void fillTemplateStatus(PluginTemplateDto templateDto, List<PluginBindingDto> bindingInfos) {
        boolean allNeedSync = true;
        boolean noNeedSync = true;
        for (PluginBindingDto pluginBindingDto : bindingInfos) {
            if (pluginBindingDto.getTemplateVersion() != templateDto.getTemplateVersion()) {
                noNeedSync = false;
            }
            if (pluginBindingDto.getTemplateVersion() == templateDto.getTemplateVersion()) {
                allNeedSync = false;
            }
        }
        if (allNeedSync && !noNeedSync) {
            templateDto.setTemplateStatus(PluginTemplateInfo.STATUS_NEED_SYNC);
        } else if (noNeedSync && !allNeedSync) {
            templateDto.setTemplateStatus(PluginTemplateInfo.STATUS_NO_NEED_SYNC);
        } else {
            templateDto.setTemplateStatus(PluginTemplateInfo.STATUS_INCOMPLETE_SYNC);
        }
    }

    @Override
    public long update(PluginTemplateDto pluginTemplateDto) {
        PluginTemplateDto pluginTemplateInDB = get(pluginTemplateDto.getId());
        if (null == pluginTemplateInDB) {
            return BaseConst.ERROR_RESULT;
        }
        if (!StringUtils.isEmpty(pluginTemplateDto.getPluginConfiguration())) {
            if (!pluginTemplateDto.getPluginConfiguration().equals(pluginTemplateInDB.getPluginConfiguration())) {
                pluginTemplateInDB.setPluginConfiguration(pluginTemplateDto.getPluginConfiguration());
                pluginTemplateInDB.setTemplateVersion(pluginTemplateInDB.getTemplateVersion() + 1);
            }
        }
        if (!StringUtils.isEmpty(pluginTemplateDto.getTemplateName())) {
            pluginTemplateInDB.setTemplateName(pluginTemplateDto.getTemplateName());
        }
        if (null != pluginTemplateDto.getTemplateNotes()) {
            pluginTemplateInDB.setTemplateNotes(pluginTemplateDto.getTemplateNotes());
        }
        pluginTemplateInDB.setUpdateTime(System.currentTimeMillis());
        return pluginTemplateDao.update(toMeta(pluginTemplateInDB));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(PluginTemplateDto pluginTemplateDto) {
        List<PluginBindingDto> pluginBindingDtos = pluginInfoService.getBindingListByTemplateId(pluginTemplateDto.getId());
        List<Long> idList = pluginBindingDtos.stream().map(PluginBindingDto::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(idList)) {
            pluginInfoService.batchDissociateTemplate(idList);
        }
        pluginTemplateDao.delete(toMeta(pluginTemplateDto));
    }

    @Override
    public long getPluginTemplateInfoCount(long projectId, String pluginType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("projectId", projectId);
        if (!StringUtils.isEmpty(pluginType)) {
            params.put("pluginType", pluginType);
        }
        return pluginTemplateDao.getCountByFields(params);
    }

    @Override
    public List<PluginTemplateDto> getPluginTemplateInfoList(long projectId, String pluginType, long offset, long limit) {
        List<PluginTemplateInfo> pluginTemplateInfoList = pluginTemplateDao.getPluginTemplateInfoList(projectId, pluginType, offset, limit);
        if (CollectionUtils.isEmpty(pluginTemplateInfoList)) {
            return Collections.emptyList();
        }
        return pluginTemplateInfoList.stream().map(this::toView)
                .map(this::fillTemplateDto)
                .collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkSyncTemplate(long id, List<Long> pluginBindingInfoIds) {
        PluginTemplateDto pluginTemplateDto = get(id);
        if (null == pluginTemplateDto) {
            logger.info("指定的模板id不存在! id:{}", id);
            return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
        }
        List<PluginBindingDto> pluginBindingInfoList = pluginInfoService.batchGetById(pluginBindingInfoIds);
        Set<Long> pluginBindingInfoIdSet = pluginBindingInfoList.stream().map(PluginBindingDto::getId).collect(Collectors.toSet());
        Set<Long> noExistIdSet = pluginBindingInfoIds.stream().filter(item -> !pluginBindingInfoIdSet.contains(item)).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(noExistIdSet)) {
            logger.info("有不存在的插件绑定关系! noExistIdSet:{}", noExistIdSet);
            return CommonErrorCode.NO_SUCH_PLUGIN_BINDING;
        }
        List<Long> notBindingList = pluginBindingInfoList.stream().filter(item -> item.getTemplateId() != id).map(PluginBindingDto::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(notBindingList)) {
            logger.info("有不存在的插件绑定关系! noExistIdSet:{}", noExistIdSet);
            return CommonErrorCode.NO_SUCH_PLUGIN_BINDING;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<PluginBindingDto> syncTemplate(long id, List<Long> pluginBindingInfoIds) {
        PluginTemplateDto pluginTemplateDto = get(id);
        if (null == pluginTemplateDto) {
            return null;
        }
        List<PluginBindingDto> bindingInfos = pluginInfoService.batchGetById(pluginBindingInfoIds);
        return bindingInfos.stream().filter(item -> {
            if (item.getTemplateVersion() != pluginTemplateDto.getTemplateVersion()) {
                return BaseConst.ERROR_RESULT != pluginInfoService.update(item);
            }
            return false;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PluginTemplateDto> batchGet(List<Long> templateIdList) {
        List<PluginTemplateInfo> templateInfos = pluginTemplateDao.batchGet(templateIdList);
        return CollectionUtils.isEmpty(templateInfos) ? Lists.newArrayList()
                : templateInfos.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<PluginTemplateInfo> getPluginTemplateByType(String pluginType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("pluginType", pluginType);
        return pluginTemplateDao.getRecordsByField(params);
    }

    @Override
    public PluginTemplateDto toView(PluginTemplateInfo pluginTemplateInfo) {
        PluginTemplateDto templateDto = new PluginTemplateDto();
        templateDto.setId(pluginTemplateInfo.getId());
        templateDto.setTemplateNotes(pluginTemplateInfo.getTemplateNotes());
        templateDto.setProjectId(pluginTemplateInfo.getProjectId());
        templateDto.setCreateTime(pluginTemplateInfo.getCreateTime());
        templateDto.setUpdateTime(pluginTemplateInfo.getUpdateTime());
        templateDto.setPluginType(pluginTemplateInfo.getPluginType());
        templateDto.setTemplateName(pluginTemplateInfo.getTemplateName());
        templateDto.setTemplateVersion(pluginTemplateInfo.getTemplateVersion());
        templateDto.setPluginConfiguration(pluginTemplateInfo.getPluginConfiguration());
        templateDto.setPluginName(pluginTemplateInfo.getPluginName());
        // 是否为全局插件，全局插件 true
        if (pluginTemplateInfo.getProjectId() == 0) {
            templateDto.setGlobal(true);
        }
        return templateDto;
    }

    @Override
    public PluginTemplateInfo toMeta(PluginTemplateDto pluginTemplateDto) {
        PluginTemplateInfo templateInfo = new PluginTemplateInfo();
        templateInfo.setId(pluginTemplateDto.getId());
        templateInfo.setTemplateNotes(pluginTemplateDto.getTemplateNotes());
        templateInfo.setProjectId(pluginTemplateDto.getProjectId());
        templateInfo.setPluginType(pluginTemplateDto.getPluginType());
        templateInfo.setPluginName(pluginTemplateDto.getPluginName());
        templateInfo.setUpdateTime(pluginTemplateDto.getUpdateTime());
        templateInfo.setCreateTime(pluginTemplateDto.getCreateTime());
        templateInfo.setTemplateName(pluginTemplateDto.getTemplateName());
        templateInfo.setTemplateVersion(pluginTemplateDto.getTemplateVersion());
        templateInfo.setPluginConfiguration(pluginTemplateDto.getPluginConfiguration());
        return templateInfo;
    }
}
