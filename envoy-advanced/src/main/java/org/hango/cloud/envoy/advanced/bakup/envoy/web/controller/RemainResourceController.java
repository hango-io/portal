package org.hango.cloud.envoy.advanced.bakup.envoy.web.controller;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.advanced.bakup.apiserver.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2021/1/15
 */
@RestController
public class RemainResourceController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(RemainResourceController.class);

    private static final String HAS_RESOURCE = "HasResource";
    private static final String RESOURCE_TYPE_NAME = "ResourceTypeName";
    private static final String RESOURCE_NAME = "ResourceName";

    @Autowired
    private IPluginInfoService pluginInfoService;
    @Autowired
    private IPluginTemplateService pluginTemplateService;
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @GetMapping(value = "authority", params = {"Action=HasResourceRemain", "Version=2020-03-19"})
    public Object hasResourceRemain(@RequestParam(value = "PermissionScopeType") String permissionScopeType,
                                    @RequestParam(value = "PermissionScopeEnName") String permissionScopeEnName,
                                    @RequestParam(value = "PermissionScopeId") long permissionScopeId
    ) {
        logger.info("开始查询残留资源，PermissionScopeType = {}, PermissionScopeEnName = {}, PermissionScopeId{}",
                permissionScopeType, permissionScopeEnName, permissionScopeId);
        Map<String, Object> result = Maps.newHashMap();
        result.put(HAS_RESOURCE, true);
        if (Const.SCOPE_TYPE_TENANT.equals(permissionScopeType)) {
            result.put(HAS_RESOURCE, false);
            return apiReturnSuccess(result);
        }
        //plugin bind check
        PluginBindingInfoQuery pluginQuery = PluginBindingInfoQuery.builder()
                .projectId(permissionScopeId)
                .build();
        List<PluginBindingDto> bindingPluginList = pluginInfoService.getBindingPluginInfoList(pluginQuery);
        if (!CollectionUtils.isEmpty(bindingPluginList)) {
            PluginBindingDto pluginBindingDto = bindingPluginList.get(0);
            result.put(RESOURCE_TYPE_NAME, Const.AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN);
            result.put(RESOURCE_NAME, pluginBindingDto.getPluginType());
            return apiReturnSuccess(result);
        }
        //plugin template check
        List<PluginTemplateDto> pluginTemplateInfoList = pluginTemplateService.getPluginTemplateInfoList(
                permissionScopeId, null, NumberUtils.INTEGER_ZERO, NumberUtils.LONG_ONE);
        if (!CollectionUtils.isEmpty(pluginTemplateInfoList)) {
            PluginTemplateDto pluginTemplateInfo = pluginTemplateInfoList.get(0);
            result.put(RESOURCE_TYPE_NAME, Const.AUDIT_RESOURCE_TYPE_PLUGIN_TEMPLATE);
            result.put(RESOURCE_NAME, pluginTemplateInfo.getTemplateName());
            return apiReturnSuccess(result);
        }
        QueryVirtualGatewayDto query = new QueryVirtualGatewayDto();
        query.setProjectIdList(Collections.singletonList(permissionScopeId));
        List<VirtualGatewayDto> virtualGatewayList = virtualGatewayInfoService.getVirtualGatewayList(query);
        //gateway virtual host association
        if (!CollectionUtils.isEmpty(virtualGatewayList)) {
            VirtualGatewayDto gateway = virtualGatewayList.get(0);
            result.put(RESOURCE_TYPE_NAME, Const.AUDIT_RESOURCE_TYPE_GATEWAY);
            result.put(RESOURCE_NAME, gateway.getName());
            return apiReturnSuccess(result);
        }
        result.put(HAS_RESOURCE, false);
        return apiReturnSuccess(result);
    }
}
