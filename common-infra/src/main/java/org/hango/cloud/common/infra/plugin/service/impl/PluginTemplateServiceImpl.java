package org.hango.cloud.common.infra.plugin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.plugin.dao.IPluginTemplateDao;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfoQuery;
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
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.*;

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
        PluginTemplateInfoQuery query = PluginTemplateInfoQuery.builder().projectId(templateInfo.getProjectId()).templateName(templateInfo.getTemplateName()).build();
        List<PluginTemplateInfo> pluginTemplateInfos = pluginTemplateDao.getPluginTemplateInfoList(query);
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
        PluginTemplateInfoQuery query = PluginTemplateInfoQuery.builder().projectId(pluginTemplateDto.getProjectId()).templateName(pluginTemplateDto.getTemplateName()).build();
        List<PluginTemplateInfo> pluginTemplateInfos = pluginTemplateDao.getPluginTemplateInfoList(query);
        if (CollectionUtils.isEmpty(pluginTemplateInfos)) {
            return CommonErrorCode.SUCCESS;
        }
        for (PluginTemplateInfo template : pluginTemplateInfos) {
            if (template.getId() != pluginTemplateDto.getId()) {
                logger.info("修改模板时，项目下已存在同名模板/全局模板已存在同名模版");
                return CommonErrorCode.SAME_NAME_PLUGIN_TEMPLATE_EXIST;
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeleteParam(PluginTemplateDto pluginTemplateDto) {
        if (pluginTemplateDto == null) {
            return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
        }
        PluginTemplateInfo pluginTemplateInfo = pluginTemplateDao.get(pluginTemplateDto.getId());
        if (pluginTemplateInfo == null) {
            return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
        }
        return CommonErrorCode.SUCCESS;
    }


    @Override
    public long create(PluginTemplateDto pluginTemplateDto) {
        pluginTemplateDto.setTemplateVersion(1);
        PluginTemplateInfo info = toMeta(pluginTemplateDto);
        pluginTemplateDao.add(info);
        return info.getId();
    }

    @Override
    public long update(PluginTemplateDto pluginTemplateDto) {
        PluginTemplateDto target = get(pluginTemplateDto.getId());
        if (StringUtils.hasText(pluginTemplateDto.getPluginConfiguration()) &&
                !pluginTemplateDto.getPluginConfiguration().equals(target.getPluginConfiguration())) {
            target.setPluginConfiguration(pluginTemplateDto.getPluginConfiguration());
            target.setTemplateVersion(target.getTemplateVersion() + 1);
        }
        if (StringUtils.hasText(pluginTemplateDto.getTemplateName())) {
            target.setTemplateName(pluginTemplateDto.getTemplateName());
        }
        if (null != pluginTemplateDto.getTemplateNotes()) {
            target.setTemplateNotes(pluginTemplateDto.getTemplateNotes());
        }
        return pluginTemplateDao.update(toMeta(target));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(PluginTemplateDto pluginTemplateDto) {
        PluginBindingInfoQuery query = PluginBindingInfoQuery.builder().templateId(pluginTemplateDto.getId()).build();
        List<PluginBindingDto> bindingPluginInfoList = pluginInfoService.getBindingPluginInfoList(query);
        for (PluginBindingDto pluginBindingDto : bindingPluginInfoList) {
            pluginBindingDto.setTemplateId(0L);
            pluginBindingDto.setTemplateVersion(0L);
            pluginInfoService.update(pluginBindingDto);
        }
        pluginTemplateDao.delete(toMeta(pluginTemplateDto));
    }


    @Override
    public Page<PluginTemplateInfo> getPluginTemplateInfoPage(PluginTemplateInfoQuery query) {
        return pluginTemplateDao.getPluginTemplateInfoPage(query);
    }

    @Override
    public List<PluginTemplateDto> getPluginTemplateInfoList(PluginTemplateInfoQuery query) {
        List<PluginTemplateInfo> pluginTemplateInfoList = pluginTemplateDao.getPluginTemplateInfoList(query);
        if (CollectionUtils.isEmpty(pluginTemplateInfoList)) {
            return Collections.emptyList();
        }
        return pluginTemplateInfoList.stream().map(this::toView)
                .map(this::fillTemplateDto)
                .collect(Collectors.toList());
    }

    @Override
    public PluginTemplateDto get(long id) {
        PluginTemplateDto pluginTemplateDto = toView(pluginTemplateDao.get(id));
        return fillTemplateDto(pluginTemplateDto);
    }

    private PluginTemplateDto fillTemplateDto(PluginTemplateDto pluginTemplateDto) {
        if (pluginTemplateDto == null) {
            return null;
        }
        PluginBindingInfoQuery query = PluginBindingInfoQuery.builder().templateId(pluginTemplateDto.getId()).build();
        List<PluginBindingDto> bindingInfos = pluginInfoService.getBindingPluginInfoList(query);
        pluginTemplateDto.setBindingDtoList(bindingInfos);
        if (CollectionUtils.isEmpty(bindingInfos)) {
            pluginTemplateDto.setTemplateStatus(STATUS_NO_NEED_SYNC);
            return pluginTemplateDto;
        }
        fillTemplateStatus(pluginTemplateDto, bindingInfos);
        return pluginTemplateDto;
    }

    private void fillTemplateStatus(PluginTemplateDto templateDto, List<PluginBindingDto> bindingInfos) {
        templateDto.setTemplateStatus(STATUS_NO_NEED_SYNC);
        for (PluginBindingDto pluginBindingDto : bindingInfos) {
            if (pluginBindingDto.getTemplateVersion() == templateDto.getTemplateVersion()){
                pluginBindingDto.setTemplateStatus(STATUS_NO_NEED_SYNC);
                continue;
            }
            templateDto.setTemplateStatus(STATUS_NEED_SYNC);
            pluginBindingDto.setTemplateStatus(STATUS_NEED_SYNC);
        }
    }



    @Override
    public ErrorCode checkSyncTemplate(long id, List<Long> pluginBindingInfoIds) {
        PluginTemplateDto pluginTemplateDto = get(id);
        if (null == pluginTemplateDto) {
            logger.info("指定的模板id不存在! id:{}", id);
            return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
        }
        for (Long pluginBindingInfoId : pluginBindingInfoIds) {
            PluginBindingDto pluginBindingDto = pluginInfoService.get(pluginBindingInfoId);
            if (pluginBindingDto == null){
                logger.info("指定的插件绑定关系不存在! pluginBindingInfoId:{}", pluginBindingInfoId);
                return CommonErrorCode.invalidParameter("指定的插件绑定关系不存在");
            }
            if (pluginBindingDto.getTemplateId() != id) {
                logger.info("指定的插件绑定关系不属于该模板! pluginBindingInfoId:{}, templateId:{}", pluginBindingInfoId, id);
                return CommonErrorCode.invalidParameter("指定的插件绑定关系不属于该模板");
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<PluginBindingDto> syncTemplate(long id, List<Long> pluginBindingInfoIds) {
        PluginTemplateDto pluginTemplateDto = get(id);
        if (null == pluginTemplateDto) {
            return null;
        }
        List<PluginBindingDto> failedBindingInfos = Lists.newArrayList();
        for (Long pluginBindingInfoId : pluginBindingInfoIds) {
            PluginBindingDto pluginBindingDto = pluginInfoService.get(pluginBindingInfoId);
            long update = pluginInfoService.update(pluginBindingDto);
            if  (BaseConst.ERROR_RESULT == update) {
                failedBindingInfos.add(pluginBindingDto);
            }
        }
        return failedBindingInfos;
    }

    @Override
    public PluginTemplateDto toView(PluginTemplateInfo pluginTemplateInfo) {
        if (pluginTemplateInfo == null){
            return null;
        }
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
