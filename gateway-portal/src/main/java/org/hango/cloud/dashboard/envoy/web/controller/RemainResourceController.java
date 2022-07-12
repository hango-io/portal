package org.hango.cloud.dashboard.envoy.web.controller;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginTemplateInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;
    @Autowired
    private IEnvoyPluginTemplateService envoyPluginTemplateService;
    @Autowired
    private IGatewayInfoService envoyGatewayService;

    @GetMapping(value = "authority", params = {"Action=HasResourceRemain", "Version=2020-03-19"})
    public Object hasResourceRemain(@RequestParam(value = "PermissionScopeType") String permissionScopeType,
                                    @RequestParam(value = "PermissionScopeEnName") String permissionScopeEnName,
                                    @RequestParam(value = "PermissionScopeId") long permissionScopeId
    ) {
        logger.info("开始查询残留资源，PermissionScopeType = {}, PermissionScopeEnName = {}, PermissionScopeId{}",
                permissionScopeType, permissionScopeEnName, permissionScopeId);
        Map<String, Object> result = new HashMap<>();
        result.put(HAS_RESOURCE, true);
        if (Const.SCOPE_TYPE_TENANT.equals(permissionScopeType)) {
            result.put(HAS_RESOURCE, false);
            return apiReturnSuccess(result);
        }
        //service check
        List<ServiceInfo> serviceInfoList = serviceInfoService.findAllServiceByProjectId(permissionScopeId);
        if (!CollectionUtils.isEmpty(serviceInfoList)) {
            ServiceInfo serviceInfo = serviceInfoList.get(0);
            result.put(RESOURCE_TYPE_NAME, Const.AUDIT_RESOURCE_TYPE_SERVICE);
            result.put(RESOURCE_NAME, serviceInfo.getDisplayName());
            return apiReturnSuccess(result);
        }
        //plugin bind check
        List<EnvoyPluginBindingInfo> bindingPluginList = envoyPluginInfoService.getBindingPluginList(
                NumberUtils.LONG_ZERO, permissionScopeId, null, Lists.newArrayList(),
                null, NumberUtils.LONG_ZERO, NumberUtils.LONG_ONE, "id", "desc");
        if (!CollectionUtils.isEmpty(bindingPluginList)) {
            EnvoyPluginBindingInfo envoyPluginBindingInfo = bindingPluginList.get(0);
            result.put(RESOURCE_TYPE_NAME, Const.AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN);
            result.put(RESOURCE_NAME, envoyPluginBindingInfo.getPluginType());
            return apiReturnSuccess(result);
        }
        //plugin template check
        List<EnvoyPluginTemplateInfo> pluginTemplateInfoList = envoyPluginTemplateService.getPluginTemplateInfoList(
                permissionScopeId, null, NumberUtils.INTEGER_ZERO, NumberUtils.LONG_ONE);
        if (!CollectionUtils.isEmpty(pluginTemplateInfoList)) {
            EnvoyPluginTemplateInfo envoyPluginTemplateInfo = pluginTemplateInfoList.get(0);
            result.put(RESOURCE_TYPE_NAME, Const.AUDIT_RESOURCE_TYPE_PLUGIN_TEMPLATE);
            result.put(RESOURCE_NAME, envoyPluginTemplateInfo.getTemplateName());
            return apiReturnSuccess(result);
        }

        //gateway virtual host association
        List<GatewayInfo> gatewayList = envoyGatewayService.getGwEnvByProjectId(permissionScopeId);
        if (!CollectionUtils.isEmpty(gatewayList)) {
            GatewayInfo gateway = gatewayList.get(0);
            result.put(RESOURCE_TYPE_NAME, Const.AUDIT_RESOURCE_TYPE_GATEWAY);
            result.put(RESOURCE_NAME, gateway.getGwName());
            return apiReturnSuccess(result);
        }
        result.put(HAS_RESOURCE, false);
        return apiReturnSuccess(result);
    }
}
