package org.hango.cloud.envoy.infra.plugin.controller;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.plugin.dto.PluginDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.plugin.service.IEnvoyPluginInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * Envoy网关特有操作Controller层
 *
 * @author hzchenzhongyang 2020-01-09
 */
@RestController
@Validated
@RequestMapping(value = {BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyPluginManagerController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyPluginManagerController.class);
    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @GetMapping(params = {"Action=DescribePluginInfo"})
    public String getPluginInfo(@RequestParam(value = "PluginType") String pluginType,
                                @RequestParam(value = "VirtualGwId", required = false, defaultValue = "0") long virtualGwId) {
        logger.info("查询插件详情, pluginType:{}, virtualGwId:{}", pluginType, virtualGwId);
        ErrorCode checkResult = envoyPluginInfoService.checkDescribePlugin(virtualGwId);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        PluginDto pluginDto = envoyPluginInfoService.getPluginInfo(virtualGwId, pluginType);
        if (null == pluginDto) {
            return apiReturn(CommonErrorCode.NO_SUCH_PLUGIN);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("PluginInfo", pluginDto);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @GetMapping(params = {"Action=DescribePluginInfoList"})
    public String getPluginInfoList(@RequestParam(value = "VirtualGwId", required = false, defaultValue = "0") long virtualGwId, @Pattern(regexp = "|routeRule|service|global|host", message = "插件范围仅支持routeRule/service/global/host") @RequestParam(value = "PluginScope", required = false, defaultValue = "") String pluginScope) {
        logger.info("分页查询插件详情列表, virtualGwId:{}, pluginScope:{}", virtualGwId, pluginScope);
        ErrorCode checkResult = envoyPluginInfoService.checkDescribePlugin(virtualGwId);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        Map<String, Object> result = Maps.newHashMap();
        List<PluginDto> pluginInfoList = envoyPluginInfoService.getPluginInfoList(virtualGwId, pluginScope);
        result.put("PluginDtoList", pluginInfoList);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

}
