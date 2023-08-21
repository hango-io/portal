package org.hango.cloud.envoy.infra.plugin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.plugin.dto.PluginUpdateDto;
import org.hango.cloud.common.infra.plugin.dto.UpdatePluginStatusDto;
import org.hango.cloud.envoy.infra.plugin.dto.*;
import org.hango.cloud.envoy.infra.plugin.service.CustomPluginInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

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

    @PostMapping(params = {"Action=PluginUpdate"})
    public String pluginUpdate(@Valid PluginUpdateDto pluginUpdateDto) {
        ErrorCode errorCode = customPluginInfoService.checkUpdateCustomPlugin(pluginUpdateDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        customPluginInfoService.pluginUpdate(pluginUpdateDto);
        Map<String,Object> result = new HashMap<>();
        result.put(RESULT,pluginUpdateDto.getId());
        return apiReturnSuccess(result);
    }

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

    @GetMapping(params = {"Action=DeletePlugin"})
    public String deletePlugin(@RequestParam(value = "Id") Long id) {
        ErrorCode errorCode = customPluginInfoService.checkDeletePlugin(id);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        errorCode = customPluginInfoService.deletePlugin(id);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        Map<String,Object> result = new HashMap<>();
        result.put(RESULT,id);
        return apiReturnSuccess(result);
    }

    @GetMapping(params = {"Action=DescribeCustomPluginInfo"})
    public String describeCustomPluginInfo(@RequestParam(value = "Id") Long id) {
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
