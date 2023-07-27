package org.hango.cloud.envoy.infra.plugin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.operationaudit.annotation.Audit;
import org.hango.cloud.common.infra.plugin.dto.*;
import org.hango.cloud.envoy.infra.plugin.service.CustomPluginInfoService;
import org.hango.cloud.envoy.infra.plugin.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

/**
 * @ClassName CustomPluginController
 * @Description 自定义插件controller
 * @Author xianyanglin
 * @Date 2023/6/30 14:54
 */
@RestController
@Validated
@RequestMapping(value = { BaseConst.PLUGIN_PATH_V1})
public class CustomPluginController  extends AbstractController {

    @Autowired
    CustomPluginInfoService customPluginInfoService;

    @Audit(eventName = "PluginImport", description = "上传自定义插件")
    @PostMapping(params = {"Action=PluginImport"})
    public String pluginImport(@Valid CustomPluginInfoDto customPluginInfoDto) {
        ErrorCode errorCode = customPluginInfoService.checkPluginImportParameter(customPluginInfoDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        Long aLong = customPluginInfoService.pluginImport(customPluginInfoDto);
        Map<String,Object> result = new HashMap<>();
        result.put("Result",aLong);
        return apiReturnSuccess(result);
    }

    @Audit(eventName = "PluginUpdate", description = "修改自定义插件")
    @PostMapping(params = {"Action=PluginUpdate"})
    public String pluginUpdate(@Valid PluginUpdateDto pluginUpdateDto) {
        ErrorCode errorCode = customPluginInfoService.checkUpdateCustomPlugin(pluginUpdateDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        Integer id = customPluginInfoService.pluginUpdate(pluginUpdateDto);
        Map<String,Object> result = new HashMap<>();
        result.put(RESULT,id);
        return apiReturnSuccess(result);
    }

    @Audit(eventName = "UpdatePluginStatus", description = "上下架插件")
    @PostMapping(params = {"Action=UpdatePluginStatus"})
    public String updatePluginStatus(@RequestBody @Valid UpdatePluginStatusDto updatePluginStatusDto) {
        ErrorCode errorCode = customPluginInfoService.checkUpdatePluginStatus(updatePluginStatusDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        errorCode = customPluginInfoService.updatePluginStatus(updatePluginStatusDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        Map<String,Object> result = new HashMap<>();
        result.put(RESULT,updatePluginStatusDto.getId());
        return apiReturnSuccess(result);
    }

    @Audit(eventName = "DeletePlugin", description = "删除插件")
    @PostMapping(params = {"Action=DeletePlugin"})
    public String deletePlugin(@RequestBody DeletePluginDto deletePluginDto) {
        ErrorCode errorCode = customPluginInfoService.checkDeletePlugin(deletePluginDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        Long id = customPluginInfoService.deletePlugin(deletePluginDto);
        Map<String,Object> result = new HashMap<>();
        result.put(RESULT,id);
        return apiReturnSuccess(result);
    }

    @PostMapping(params = {"Action=DescribeCustomPluginInfo"})
    public String describeCustomPluginInfo(@RequestBody Map<String, Object> requestBody) {
        Object id = requestBody.get("Id");
        if (Objects.isNull(id)) {
            return apiReturn(Result.err(CommonErrorCode.invalidParameter("Id is null")));
        }
        Long pluginId = Long.parseLong(id.toString());
        DescribeCustomPluginDto describeCustomPluginDto = customPluginInfoService.describeCustomPluginInfo(pluginId);
        Map<String,Object> result = new HashMap<>();
        result.put(RESULT,describeCustomPluginDto);
        return apiReturnSuccess(result);
    }

    @PostMapping(params = {"Action=DescribeCustomPluginList"})
    public String describeCustomPluginList(@RequestBody CustomPluginQueryDto customPluginQueryDto) {
        Page<DescribeCustomPluginDto> customPluginList = customPluginInfoService.getCustomPluginList(customPluginQueryDto);
        Map<String,Object> result = new HashMap<>();
        result.put(TOTAL_COUNT, customPluginList.getTotal());
        result.put(RESULT,customPluginList.getRecords());
        return apiReturnSuccess(result);
    }

    @PostMapping(params = {"Action=DescribeCustomPluginInstanceList"})
    public String describeCustomPluginInstanceList(@RequestBody CustomPluginInstanceListQueryDto customPluginInstanceListQueryDto) {
        Page<CustomPluginInstanceDto> page = customPluginInfoService.getCustomPluginInstancePage(customPluginInstanceListQueryDto);
        Map<String, Object> result = new HashMap<>();
        result.put(TOTAL_COUNT, page.getTotal());
        result.put(RESULT, page.getRecords());
        return apiReturnSuccess(result);
    }

}
