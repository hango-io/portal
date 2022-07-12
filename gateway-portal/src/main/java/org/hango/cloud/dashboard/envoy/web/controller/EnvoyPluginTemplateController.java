package org.hango.cloud.dashboard.envoy.web.controller;

import com.google.common.collect.Lists;
import org.apache.commons.httpclient.HttpStatus;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginTemplateInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginTemplateService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginBindingDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginTemplateDto;
import org.hango.cloud.dashboard.envoy.web.dto.SyncPluginTemplateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 插件模板管理Controller
 *
 * @author hzchenzhongyang 2020-04-20
 */
@RestController
@Validated
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyPluginTemplateController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyPluginTemplateController.class);

    @Autowired
    private IEnvoyPluginTemplateService envoyPluginTemplateService;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @PostMapping(params = {"Action=CreatePluginTemplate"})
    public String createTemplate(@RequestBody @Validated EnvoyPluginTemplateDto templateDto) {
        logger.info("创建插件模板, templateDto:{}", templateDto);
        EnvoyPluginTemplateInfo templateInfo = templateDto.toMeta();
        long projectId = ProjectTraceHolder.getProId();
        templateInfo.setProjectId(projectId);
        ErrorCode errorCode = envoyPluginTemplateService.checkCreatePluginTemplate(templateInfo);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        long id = envoyPluginTemplateService.createPluginTemplate(templateInfo);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("Id", id);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @PostMapping(params = {"Action=CreateGlobalPluginTemplate"})
    public String createGlobalTemplate(@RequestBody @Validated EnvoyPluginTemplateDto templateDto) {
        logger.info("创建插件模板, templateDto:{}", templateDto);
        EnvoyPluginTemplateInfo templateInfo = templateDto.toMeta();
        //代表全局插件
        templateInfo.setProjectId(0);
        ErrorCode errorCode = envoyPluginTemplateService.checkCreatePluginTemplate(templateInfo);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        long id = envoyPluginTemplateService.createPluginTemplate(templateInfo);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("Id", id);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @PostMapping(params = {"Action=UpdatePluginTemplate"})
    public String updateTemplate(@RequestBody EnvoyPluginTemplateDto templateDto) {
        logger.info("更新插件模板, templateDto:{}", templateDto);
        EnvoyPluginTemplateInfo templateInfo = templateDto.toMeta();
        ErrorCode errorCode = envoyPluginTemplateService.checkUpdatePluginTemplate(templateInfo);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        boolean updateSucc = envoyPluginTemplateService.updatePluginTemplate(templateInfo);
        if (!updateSucc) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(HttpStatus.SC_OK, null, null, null);
    }

    @GetMapping(params = {"Action=DeletePluginTemplate"})
    public String deleteTemplate(@RequestParam(name = "Id") long id) {
        logger.info("删除插件模板, id:{}", id);
        ErrorCode errorCode = envoyPluginTemplateService.checkDeletePluginTemplate(id);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        boolean deleteSucc = envoyPluginTemplateService.deletePluginTemplate(id);
        if (!deleteSucc) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(HttpStatus.SC_OK, null, null, null);
    }

    @GetMapping(params = {"Action=DescribePluginTemplate"})
    public String describeTemplate(@RequestParam(name = "Id") long id) {
        logger.info("查询插件模板详情, id:{}", id);
        EnvoyPluginTemplateInfo templateInfo = envoyPluginTemplateService.getTemplateById(id);
        if (null == templateInfo) {
            return apiReturn(CommonErrorCode.NoSuchPluginTemplate);
        }
        EnvoyPluginTemplateDto templateDto = envoyPluginTemplateService.fromMeta(templateInfo);
        fillTemplateDto(templateDto);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("TemplateInfo", templateDto);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    private void fillTemplateDto(EnvoyPluginTemplateDto templateDto) {
        List<EnvoyPluginBindingInfo> bindingInfos = envoyPluginInfoService.getBindingListByTemplateId(templateDto.getId());
        if (!CollectionUtils.isEmpty(bindingInfos)) {
            List<EnvoyPluginBindingDto> bindingDtoList =
                    bindingInfos.stream().map(item -> envoyPluginInfoService.fromMeta(item)).collect(Collectors.toList());
            envoyPluginInfoService.fillDtoFiled(bindingDtoList);
            templateDto.setBindingDtoList(bindingDtoList);
            fillTemplateStatus(templateDto, bindingInfos);
        } else {
            templateDto.setTemplateStatus(EnvoyPluginTemplateInfo.STATUS_NO_NEED_SYNC);
            templateDto.setBindingDtoList(Lists.newArrayList());
        }

    }

    private void fillTemplateStatus(EnvoyPluginTemplateDto templateDto, List<EnvoyPluginBindingInfo> bindingInfos) {
        boolean allNeedSync = true;
        boolean noNeedSync = true;
        for (EnvoyPluginBindingInfo bindingInfo : bindingInfos) {
            if (bindingInfo.getTemplateVersion() != templateDto.getTemplateVersion()) {
                noNeedSync = false;
            }
            if (bindingInfo.getTemplateVersion() == templateDto.getTemplateVersion()) {
                allNeedSync = false;
            }
        }
        if (allNeedSync && !noNeedSync) {
            templateDto.setTemplateStatus(EnvoyPluginTemplateInfo.STATUS_NEED_SYNC);
        } else if (noNeedSync && !allNeedSync) {
            templateDto.setTemplateStatus(EnvoyPluginTemplateInfo.STATUS_NO_NEED_SYNC);
        } else {
            templateDto.setTemplateStatus(EnvoyPluginTemplateInfo.STATUS_INCOMPLETE_SYNC);
        }
    }

    @GetMapping(params = {"Action=DescribePluginTemplates"})
    public String describeTemplates(@RequestParam(name = "PluginType", required = false) String pluginType,
                                    @Min(0) @RequestParam(name = "Offset", required = false, defaultValue = "0") long offset,
                                    @Min(0) @Max(1000) @RequestParam(name = "Limit", required = false, defaultValue = "1000") long limit,
                                    @RequestParam(name = "IsGlobal", required = false) boolean isGlobal) {
        long projectId = isGlobal ? 0 : ProjectTraceHolder.getProId();
        logger.info("查询插件模板列表，projectId:{}, pluginType:{} offset:{}, limit:{}", projectId, pluginType, offset, limit);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        long totalCount = envoyPluginTemplateService.getPluginTemplateInfoCount(projectId, pluginType);
        result.put("TotalCount", totalCount);
        List<EnvoyPluginTemplateInfo> templateInfoList = null;
        if (totalCount > offset) {
            templateInfoList = envoyPluginTemplateService.getPluginTemplateInfoList(projectId, pluginType, offset, limit);
        }
        if (!CollectionUtils.isEmpty(templateInfoList)) {
            result.put("TemplateInfoList", templateInfoList.stream().map(item -> {
                EnvoyPluginTemplateDto templateDto = envoyPluginTemplateService.fromMeta(item);
                fillTemplateDto(templateDto);
                return templateDto;
            }).collect(Collectors.toList()));
        } else {
            result.put("TemplateInfoList", Lists.newArrayList());
        }
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @GetMapping(params = {"Action=DescribeAllPluginTemplates"})
    public String describeAllTemplates(@RequestParam(name = "PluginType", required = false) String pluginType) {
        long projectId = ProjectTraceHolder.getProId();
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        List<EnvoyPluginTemplateInfo> templateInfoList = envoyPluginTemplateService.getPluginTemplateInfoList(projectId, pluginType, 0, 1000);
        List<EnvoyPluginTemplateInfo> templateInfoGlobalList = envoyPluginTemplateService.getPluginTemplateInfoList(0, pluginType, 0, 1000);
        templateInfoList.addAll(templateInfoGlobalList);
        if (!CollectionUtils.isEmpty(templateInfoList)) {
            result.put("TemplateInfoList", templateInfoList.stream().map(item -> {
                EnvoyPluginTemplateDto templateDto = envoyPluginTemplateService.fromMeta(item);
                fillTemplateDto(templateDto);
                return templateDto;
            }).collect(Collectors.toList()));
        } else {
            result.put("TemplateInfoList", Lists.newArrayList());
        }
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @PostMapping(params = {"Action=SyncPluginTemplates"})
    public String syncTemplate(@RequestBody SyncPluginTemplateDto syncPluginTemplateDto) {
        logger.info("同步模板配置到插件, syncPluginTemplateDto:{}", syncPluginTemplateDto.toString());
        ErrorCode errorCode = envoyPluginTemplateService.checkSyncTemplate(syncPluginTemplateDto.getId(), syncPluginTemplateDto.getPluginBindingInfoIds());
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        List<EnvoyPluginBindingInfo> failedList = envoyPluginTemplateService.syncTemplate(syncPluginTemplateDto.getId(), syncPluginTemplateDto.getPluginBindingInfoIds());
        if (CollectionUtils.isEmpty(failedList)) {
            return apiReturn(HttpStatus.SC_OK, null, null, null);
        } else {
            Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
            result.put("FailedIdList", failedList);
            return apiReturn(HttpStatus.SC_OK, null, null, result);
        }
    }
}
