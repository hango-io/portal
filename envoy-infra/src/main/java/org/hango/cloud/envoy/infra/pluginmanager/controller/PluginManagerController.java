package org.hango.cloud.envoy.infra.pluginmanager.controller;

import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginManagerDto;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/23
 */
@RestController
@Validated
@RequestMapping(value = BaseConst.HANGO_DASHBOARD_PREFIX, params = {"Version=2019-09-01"})
public class PluginManagerController extends AbstractController {

    @Autowired
    private IPluginManagerService pluginManagerService;
    /**
     * 获取插件列表
     *
     * @return
     */
    @GetMapping(params = {"Action=DescribePluginManager"})
    public Object describePluginManager(@RequestParam(name = "VirtualGwId") long virtualGwId) {
        List<PluginManagerDto> envoyPluginManager = pluginManagerService.getPluginManager(virtualGwId);
        return apiReturnSuccess(envoyPluginManager);
    }

    /**
     * 修改插件列表
     *
     * @return
     */
    @PostMapping(params = {"Action=UpdatePluginManager"})
    public Object updatePluginManager(@RequestParam(name = "VirtualGwId") long virtualGwId, @RequestParam(name = "Name") String name, @RequestParam(name = "Enable") boolean enable) {
        ErrorCode errorCode = pluginManagerService.checkPluginManager(virtualGwId, name, enable);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        PluginOrderItemDto itemDto = new PluginOrderItemDto();
        itemDto.setName(name);
        itemDto.setEnable(enable);
        boolean result = pluginManagerService.updatePluginManager(virtualGwId, itemDto);
        return apiReturnSuccess(result);
    }

}
