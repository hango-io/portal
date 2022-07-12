package org.hango.cloud.dashboard.envoy.web.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.httpclient.HttpStatus;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.RegistryCenterDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.gateway.PermissionScopeDto;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayProjectService;
import org.hango.cloud.dashboard.apiserver.service.IRegistryCenterService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGatewayService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIstioGatewayService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyGatewaySettingDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyGatewayVirtualHostDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginManagerDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyVirtualHostDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyVirtualHostUpdateDto;
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Envoy网关特有操作Controller层
 *
 * @author hzchenzhongyang 2020-01-09
 */
@RestController
@Validated
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyGatewayController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyGatewayController.class);

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private IRegistryCenterService registryCenterService;

    @Autowired
    private IEnvoyGatewayService envoyGatewayService;

    @Autowired
    private IEnvoyIstioGatewayService envoyIstioGatewayService;

    @Autowired
    private IGatewayProjectService gatewayProjectService;

    @PostMapping(params = {"Action=UpdateGatewayVirtualHosts"})
    public String updateGatewayVirtualHosts(@Validated @RequestBody EnvoyGatewayVirtualHostDto gatewayVirtualHost) {
        logger.info("项目关联网关，修改网关virtual host， gatewayVirtualHost:{}", gatewayVirtualHost);

        ErrorCode checkResult = envoyGatewayService.checkVirtualHostList(gatewayVirtualHost.getGwId(), gatewayVirtualHost.toEnvoyVirutalHostList());
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        boolean updateSucc = envoyGatewayService.updateVirtualHostList(gatewayVirtualHost.getGwId(), gatewayVirtualHost.toEnvoyVirutalHostList());
        if (!updateSucc) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(CommonErrorCode.Success);
    }

    @PostMapping(params = {"Action=CreateGatewayVirtualHost"})
    @Audit(eventName = "CreateGatewayVirtualHost", description = "创建网关绑定项目关系")
    public String createGatewayVirtualHost(@Validated @RequestBody EnvoyVirtualHostDto virtualHostDto) {
        logger.info("创建单个vh， virtualHostDto:{} ", virtualHostDto);
        ErrorCode checkResult = envoyGatewayService.checkCreateVirtualHost(virtualHostDto.toMeta());
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        boolean createSucc;
        try {
            createSucc = envoyGatewayService.createVirtualHost(virtualHostDto.toMeta());
        } catch (RuntimeException e) {
            return apiReturn(CommonErrorCode.NoSuchProject);
        }
        if (!createSucc) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(CommonErrorCode.Success);
    }

    @PostMapping(params = {"Action=UpdateGatewayVirtualHost"})
    @Audit(eventName = "UpdateGatewayVirtualHost", description = "更新网关绑定项目域名")
    public String updateGatewayVirtualHost(@Validated @RequestBody EnvoyVirtualHostUpdateDto updateDto) {
        logger.info("更新vh，updateDto:{}", updateDto);
        ErrorCode checkResult = envoyGatewayService.checkUpdateVirtualHost(updateDto.getVirtualHostId(), updateDto.getHostList(), updateDto.getBindType(), updateDto.getProjectList());
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        boolean updateSucc = envoyGatewayService.updateVirtualHost(updateDto);
        if (!updateSucc) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(CommonErrorCode.Success);
    }

    @GetMapping(params = {"Action=DescribeGatewayVirtualHosts"})
    public String getGatewayVirtualHosts(@RequestParam(value = "GwId", required = false, defaultValue = "0") long gwId,
                                         @RequestParam(value = "ProjectId", required = false, defaultValue = "0") long projectId,
                                         @RequestParam(value = "TenantId", required = false, defaultValue = "0") long tenantId,
                                         @RequestParam(value = "Domain", required = false) String domain,
                                         @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                                         @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset) {
        logger.info("查询网关的virtual host列表信息, gwId:{}, projectId:{}, tenantId:{}, domain:{}, limit:{}, offset:{}",
                gwId, projectId, tenantId, domain, limit, offset);
        List<Long> projectIdList = Lists.newArrayList();
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        if (0 < projectId) {
            projectIdList.add(projectId);
        } else if (0 < tenantId) {
            List<PermissionScopeDto> projectDtoList = gatewayProjectService.getProjectScopeList(tenantId);
            //bugfix：租户非空，项目为空的情况
            if (CollectionUtils.isEmpty(projectDtoList)) {
                result.put("VirtualHostList", Lists.newArrayList());
                return apiReturn(HttpStatus.SC_OK, null, null, result);
            }
            projectIdList = projectDtoList.stream().map(PermissionScopeDto::getId).collect(Collectors.toList());
        }
        //查询全量项目
        List<EnvoyVirtualHostInfo> virtualHostInfoList = envoyGatewayService.getVirtualHostList(gwId, projectIdList, domain);
        result.put("TotalCount", virtualHostInfoList.size());
        //内存分页
        List<EnvoyVirtualHostDto> virtualHostDtos = virtualHostInfoList.stream()
                .sorted(Comparator.comparing(EnvoyVirtualHostInfo::getId))
                .skip(offset).limit(limit)
                .map(EnvoyVirtualHostDto::fromMeta)
                .collect(Collectors.toList());
        result.put("VirtualHostList", virtualHostDtos);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @GetMapping(params = {"Action=DescribeGatewayVirtualHost"})
    public String getGatewayVirtualHost(@RequestParam(value = "VirtualHostId") long virtualHostId) {
        logger.info("查询网关的 VirtualHost 详情, virtualHostId:{}", virtualHostId);
        EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService.getVirtualHost(virtualHostId);
        if (null == virtualHostInfo) {
            return apiReturn(HttpStatus.SC_OK, null, null, null);
        }
        Map<String, Object> result = (Map<String, Object>) JSON.toJSON(EnvoyVirtualHostDto.fromMeta(virtualHostInfo));
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @GetMapping(params = {"Action=DeleteGatewayVirtualHost"})
    @Audit(eventName = "DeleteGatewayVirtualHost", description = "删除网关绑定项目关系")
    public String deleteGatewayVirtualHost(@RequestParam(value = "VirtualHostId") long virtualHostId) {
        logger.info("删除网关的virtual host, virtualHostId:{}", virtualHostId);
        ErrorCode errorCode = envoyGatewayService.checkDeleteVirtualHost(virtualHostId);
        if (!CommonErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        return apiReturn(envoyGatewayService.deleteVirtualHost(virtualHostId));
    }

    @GetMapping(params = {"Action=DescribeHostTypeProjectId"})
    public String getHostTypeProjectId(@RequestParam(value = "GwId") long gwId) {
        Set<Long> hostTypeVhProject = envoyGatewayService.getHostTypeVhProject(gwId);
        Map<String, Object> result = new HashMap<>(1);
        result.put("ProjectList", hostTypeVhProject);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @PostMapping(params = "Action=SaveRegistryCenter")
    public String saveRegistryCenter(@RequestBody @Validated RegistryCenterDto registryCenterDto) {
        logger.info("saveRegistryCenter params is {}", JSON.toJSONString(registryCenterDto));
        ErrorCode errorCode = registryCenterService.checkParam(registryCenterDto);
        if (!CommonErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        registryCenterDto.setProjectId(ProjectTraceHolder.getProId());
        registryCenterService.saveRegistryCenter(registryCenterDto);
        return apiReturn(CommonErrorCode.Success);
    }

    @GetMapping(params = "Action=GetRegistryCenterList")
    public String describeRegistryCenter(@RequestParam("Type") String registryType, @RequestParam("GwId") long gwId) {
        if (RegistryCenterEnum.get(registryType) == null) {
            return apiReturn(CommonErrorCode.InvalidParameterRegistryCenterType(registryType));
        }
        List<RegistryCenterDto> registryCenterList = registryCenterService.findByType(registryType, gwId);
        List<String> result = registryCenterList.stream().map(RegistryCenterDto::getRegistryAddr).distinct().collect(Collectors.toList());
        return apiReturnSuccess(result);
    }

    @GetMapping(params = "Action=DeleteRegistryCenter")
    public String deleteRegistryCenter(@RequestParam("Id") long id) {
        logger.info("deleteRegistryCenter Id is {}", id);
        registryCenterService.deleteRegistryCenter(id);
        return apiReturn(CommonErrorCode.Success);
    }

    @GetMapping(params = "Action=UpdateApiPlaneAddr")
    public String updateApiPlaneAddr(@RequestParam("GwId") long gwId,
                                     @RequestParam("ApiPlaneAddr") String apiPlaneAddr) {
        logger.info("更新网关gwId:{},api-plane addr:{}", gwId, apiPlaneAddr);
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        gatewayInfo.setApiPlaneAddr(apiPlaneAddr);
        gatewayInfoService.update(gatewayInfo);
        return apiReturn(CommonErrorCode.Success);
    }

    /**
     * 更新网关高级配置
     *
     * @return
     */
    @PostMapping(params = {"Action=UpdateGatewaySetting"})
    public Object updateGatewaySetting(@RequestBody @Validated EnvoyGatewaySettingDto setting) {
        logger.info("更新网关高级配置, GatewayInfo = {}", JSON.toJSONString(setting));
        GatewayInfo gatewayInfo = gatewayInfoService.get(setting.getGwId());
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        boolean result = envoyIstioGatewayService.updateGatewaySetting(setting, gatewayInfo);
        return apiReturn(result ? CommonErrorCode.Success : CommonErrorCode.InternalServerError);
    }

    /**
     * 获取网关高级配置
     *
     * @return
     */
    @GetMapping(params = {"Action=GetGatewaySetting"})
    public Object getGatewaySetting(@RequestParam(name = "GwId") long gwId) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.info("未查询到网关信息, GwId = {}", gwId);
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        EnvoyGatewaySettingDto gatewaySetting = envoyIstioGatewayService.getGatewaySetting(gatewayInfo);
        return apiReturnSuccess(gatewaySetting);
    }


    /**
     * 获取插件列表
     *
     * @return
     */
    @GetMapping(params = {"Action=DescribePluginManager"})
    public Object describePluginManager(@RequestParam(name = "GwId") long gwId) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.info("未查询到网关信息, GwId = {}", gwId);
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        List<EnvoyPluginManagerDto> envoyPluginManager = envoyGatewayService.getEnvoyPluginManager(gatewayInfo);
        return apiReturnSuccess(envoyPluginManager);
    }

    /**
     * 修改插件列表
     *
     * @return
     */
    @PostMapping(params = {"Action=UpdatePluginManager"})
    public Object updatePluginManager(@RequestParam(name = "GwId") long gwId,
                                      @RequestParam(name = "Name") String name,
                                      @RequestParam(name = "Enable") boolean enable
    ) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.info("未查询到网关信息, GwId = {}", gwId);
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        ErrorCode errorCode = envoyGatewayService.checkEnvoyPluginManager(gatewayInfo, name, enable);
        if (!CommonErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        boolean result = envoyGatewayService.updateEnvoyPluginManager(gatewayInfo, name, enable);
        return apiReturnSuccess(result);
    }


}
