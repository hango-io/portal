package org.hango.cloud.common.infra.plugin.controller;

import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.dto.SyncPluginTemplateDto;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件模板管理Controller
 *
 * @author hzchenzhongyang 2020-04-20
 */
@RestController
@Validated
@RequestMapping(value = { BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class PluginTemplateController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(PluginTemplateController.class);

    @Autowired
    private IPluginTemplateService pluginTemplateService;

    @PostMapping(params = {"Action=CreatePluginTemplate"})
    public String createTemplate(@RequestBody @Validated PluginTemplateDto pluginTemplateDto) {
        return createPluginTemplateWithCheck(pluginTemplateDto, ProjectTraceHolder.getProId());
    }

    @PostMapping(params = {"Action=CreateGlobalPluginTemplate"})
    public String createGlobalTemplate(@RequestBody @Validated PluginTemplateDto pluginTemplateDto) {
        return createPluginTemplateWithCheck(pluginTemplateDto, BaseConst.GLOBAL_PLUGIN_PROJECT_ID);
    }

    private String createPluginTemplateWithCheck(PluginTemplateDto pluginTemplateDto, long projectId) {
        logger.info("创建插件模板, templateDto:{}", pluginTemplateDto);
        pluginTemplateDto.setProjectId(projectId);
        ErrorCode errorCode = pluginTemplateService.checkCreateParam(pluginTemplateDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }

        long id = pluginTemplateService.create(pluginTemplateDto);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("Id", id);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @PostMapping(params = {"Action=UpdatePluginTemplate"})
    public String updateTemplate(@RequestBody PluginTemplateDto pluginTemplateDto) {
        logger.info("更新插件模板, templateDto:{}", pluginTemplateDto);
        ErrorCode errorCode = pluginTemplateService.checkUpdateParam(pluginTemplateDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        if (pluginTemplateService.update(pluginTemplateDto) == BaseConst.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturn(HttpStatus.SC_OK, null, null, null);
    }

    @GetMapping(params = {"Action=DeletePluginTemplate"})
    public String deleteTemplate(@RequestParam(name = "Id") long id) {
        logger.info("删除插件模板, id:{}", id);
        PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(id);
        ErrorCode errorCode = pluginTemplateService.checkDeleteParam(pluginTemplateDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        pluginTemplateService.delete(pluginTemplateDto);
        return apiReturn(HttpStatus.SC_OK, null, null, null);
    }

    @GetMapping(params = {"Action=DescribePluginTemplate"})
    public String describeTemplate(@RequestParam(name = "Id") long id) {
        logger.info("查询插件模板详情, id:{}", id);
        PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(id);
        if (null == pluginTemplateDto) {
            return apiReturn(CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("TemplateInfo", pluginTemplateDto);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @GetMapping(params = {"Action=DescribePluginTemplates"})
    public String describeTemplates(@RequestParam(name = "PluginType", required = false) String pluginType, @Min(0) @RequestParam(name = "Offset", required = false, defaultValue = "0") long offset, @Min(0) @Max(1000) @RequestParam(name = "Limit", required = false, defaultValue = "1000") long limit, @RequestParam(name = "IsGlobal", required = false) boolean isGlobal) {
        long projectId = isGlobal ? 0 : ProjectTraceHolder.getProId();
        logger.info("查询插件模板列表，projectId:{}, pluginType:{} offset:{}, limit:{}", projectId, pluginType, offset, limit);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("TemplateInfoList", pluginTemplateService.getPluginTemplateInfoList(projectId, pluginType, offset, limit));
        result.put("TotalCount", pluginTemplateService.getPluginTemplateInfoCount(projectId, pluginType));
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @GetMapping(params = {"Action=DescribeAllPluginTemplates"})
    public String describeAllTemplates(@RequestParam(name = "PluginType", required = false) String pluginType) {
        long projectId = ProjectTraceHolder.getProId();
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        List<PluginTemplateDto> routePluginTemplateInfoList = pluginTemplateService.getPluginTemplateInfoList(projectId, pluginType, 0, 1000);
        List<PluginTemplateDto> globalPluginTemplateInfoList = pluginTemplateService.getPluginTemplateInfoList(0, pluginType, 0, 1000);
        List<PluginTemplateDto> allPluginTemplateInfoList = Lists.newArrayList();
        allPluginTemplateInfoList.addAll(routePluginTemplateInfoList);
        allPluginTemplateInfoList.addAll(globalPluginTemplateInfoList);
        result.put("TemplateInfoList", allPluginTemplateInfoList);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @PostMapping(params = {"Action=SyncPluginTemplates"})
    public String syncTemplate(@RequestBody SyncPluginTemplateDto syncPluginTemplateDto) {
        logger.info("同步模板配置到插件, syncPluginTemplateDto:{}", syncPluginTemplateDto.toString());
        ErrorCode errorCode = pluginTemplateService.checkSyncTemplate(syncPluginTemplateDto.getId(), syncPluginTemplateDto.getPluginBindingInfoIds());
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        List<PluginBindingDto> failedList = pluginTemplateService.syncTemplate(syncPluginTemplateDto.getId(), syncPluginTemplateDto.getPluginBindingInfoIds());
        if (!CollectionUtils.isEmpty(failedList)) {
            Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
            result.put("FailedIdList", failedList);
            return apiReturn(HttpStatus.SC_OK, null, null, result);
        }
        return apiReturn(HttpStatus.SC_OK, null, null, null);
    }
}