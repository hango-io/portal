package org.hango.cloud.dashboard.envoy.web.controller;

import com.google.common.collect.Lists;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.dto.plugindto.CopyGlobalPluginDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.BindingPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginBindingDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hzchenzhongyang 2019-11-05
 * <p>
 * 插件信息controller层实现类
 */
@RestController
@Validated
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyPluginInfoController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyPluginInfoController.class);

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @GetMapping(params = {"Action=DescribePluginInfo"})
    public String getPluginInfo(@RequestParam(value = "PluginType") String pluginType,
                                @RequestParam(value = "GwId", required = false, defaultValue = "0") long gwId) {
        logger.info("查询插件详情, pluginType:{}, gwId:{}", pluginType, gwId);
        ErrorCode checkResult = envoyPluginInfoService.checkDescribePlugin(gwId);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        EnvoyPluginInfo pluginInfo = envoyPluginInfoService.getPluginInfoFromApiPlane(gwId, pluginType);
        if (null == pluginInfo) {
            return apiReturn(CommonErrorCode.NoSuchPlugin);
        }
        EnvoyPluginDto pluginDto = EnvoyPluginDto.fromMeta(pluginInfo);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("PluginInfo", pluginDto);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @GetMapping(params = {"Action=DescribePluginInfoList"})
    public String getPluginInfoList(@RequestParam(value = "GwId", required = false, defaultValue = "0") long gwId,
                                    @Pattern(regexp = "|routeRule|service|global", message = "插件范围仅支持routeRule/service/global") @RequestParam(value = "PluginScope", required = false, defaultValue = "") String pluginScope) {
        logger.info("分页查询插件详情列表, gwId:{}, pluginScope", gwId, pluginScope);
        ErrorCode checkResult = envoyPluginInfoService.checkDescribePlugin(gwId);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        List<EnvoyPluginInfo> pluginInfoList = envoyPluginInfoService.getPluginInfoListFromApiPlane(gwId);
        List<EnvoyPluginDto> pluginDtoList = pluginInfoList.stream().filter(item -> {
            if (StringUtils.isBlank(item.getPluginScope())) return false;
            if (StringUtils.isBlank(pluginScope)) {
                return true;
            }
            Set<String> pluginScopeSet = Arrays.stream(item.getPluginScope().split(",")).map(String::trim).collect(Collectors.toSet());
            return pluginScopeSet.contains(pluginScope);
        }).map(EnvoyPluginDto::fromMeta).collect(Collectors.toList());
        result.put("PluginDtoList", pluginDtoList);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "BindingPlugin", description = "绑定插件")
    @GetMapping(params = {"Action=BindingPlugin"})
    public String bindingPlugin(@RequestParam(value = "BindingObjectId") String bindingObjectId,
                                @RequestParam(value = "BindingObjectType") String bindingObjectType,
                                @RequestParam(value = "PluginType") String pluginType,
                                @RequestParam(value = "PluginConfiguration", required = false, defaultValue = "") String pluginConfiguration,
                                @RequestParam(value = "GwId") long gwId,
                                @RequestParam(value = "TemplateId", required = false, defaultValue = "0") long templateId) {
        logger.info("绑定插件， bindingObjectId:{}, bindingObjectType:{} pluginType:{}, pluginConfiguration:{}, gwId:{}, templateId",
                bindingObjectId, bindingObjectType, pluginType, pluginConfiguration, gwId, templateId);

        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN, bindingObjectId, null);
        AuditResourceHolder.set(resource);

        BindingPluginInfo bindingPluginInfo = new BindingPluginInfo(gwId,
                bindingObjectType,
                Long.parseLong(bindingObjectId),
                pluginType,
                pluginConfiguration);
        ErrorCode checkResult = envoyPluginInfoService.checkBindingPlugin(bindingPluginInfo,
                ProjectTraceHolder.getProId(),
                templateId);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        boolean bindingResult = envoyPluginInfoService.bindingPlugin(bindingPluginInfo, ProjectTraceHolder.getProId(), templateId);
        if (!bindingResult) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }

        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    /**
     * 为了防止浏览器发送GET请求url长度超长，新增一个POST接口
     * 由于GET接口传媒已经在使用暂不删除，仅Method不同
     **/
    @MethodReentrantLock
    @Audit(eventName = "BindingPlugin", description = "绑定插件")
    @PostMapping(params = {"Action=BindingPlugin"})
    public String bindingPlugin(@Validated @RequestBody EnvoyPluginBindingDto bindingDto) {
        logger.info("绑定插件， envoyPluginBindingDto:{}", bindingDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN,
                bindingDto.getBindingObjectId(),
                bindingDto.getPluginName());
        AuditResourceHolder.set(resource);

        // 前台数据转换
        BindingPluginInfo bindingPluginInfo = BindingPluginInfo.createBindingPluginFromDto(bindingDto);

        ErrorCode checkResult = envoyPluginInfoService.checkBindingPlugin(bindingPluginInfo,
                ProjectTraceHolder.getProId(),
                bindingDto.getTemplateId());
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        boolean bindingResult = envoyPluginInfoService.bindingPlugin(bindingPluginInfo,
                ProjectTraceHolder.getProId(),
                bindingDto.getTemplateId());
        if (!bindingResult) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }

        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @MethodReentrantLock
    @Audit(eventName = "UnbindingPlugin", description = "解绑插件")
    @GetMapping(params = {"Action=UnbindingPlugin"})
    public String unbindingPlugin(@RequestParam(value = "PluginBindingInfoId") long pluginBindingInfoId) {
        // 由于一个对象可以绑定多个一样的插件，所以解绑的时候需要指定具体的绑定关系
        logger.info("解绑插件, pluginBindingInfoId:{}", pluginBindingInfoId);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN, pluginBindingInfoId, null);
        AuditResourceHolder.set(resource);

        boolean unbindingResult = envoyPluginInfoService.unbindingPlugin(pluginBindingInfoId);
        if (!unbindingResult) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }

        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @Deprecated
    @MethodReentrantLock
    @Audit(eventName = "UpdatePluginConfiguration", description = "更新插件配置")
    @GetMapping(params = {"Action=UpdatePluginConfiguration"})
    public String updatePluginConfiguration(@RequestParam(value = "PluginBindingInfoId") long pluginBindingInfoId,
                                            @RequestParam(value = "PluginConfiguration", required = false) String pluginConfiguration,
                                            @RequestParam(value = "TemplateId", required = false, defaultValue = "0") long templateId) {
        logger.info("更新插件配置, pluginBindingInfoId:{}, pluginConfiguration:{}", pluginBindingInfoId, pluginConfiguration);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN, pluginBindingInfoId, null);
        AuditResourceHolder.set(resource);

        ErrorCode checkResult = envoyPluginInfoService.checkUpdatePluginConfiguration(pluginBindingInfoId, pluginConfiguration, templateId);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        boolean updateResult = envoyPluginInfoService.updatePluginConfiguration(pluginBindingInfoId, pluginConfiguration, templateId);
        if (!updateResult) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }

        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    /**
     * 同绑定插件接口，为了防止浏览器发送GET请求url长度超长，新增一个POST接口
     * 由于GET接口传媒已经在使用暂不删除，仅Method不同
     */
    @MethodReentrantLock
    @Audit(eventName = "UpdatePluginConfiguration", description = "更新插件配置")
    @PostMapping(params = {"Action=UpdatePluginConfiguration"})
    public String updatePluginConfiguration(@RequestBody EnvoyPluginBindingDto bindingDto) {
        logger.info("更新插件配置, pluginBindingInfoId:{}, pluginConfiguration:{}", bindingDto.getId(), bindingDto.getPluginConfiguration());
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN, bindingDto.getId(), bindingDto.getPluginName());
        AuditResourceHolder.set(resource);

        ErrorCode checkResult = envoyPluginInfoService.checkUpdatePluginConfiguration(bindingDto.getId(), bindingDto.getPluginConfiguration(), bindingDto.getTemplateId());
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        boolean updateResult = envoyPluginInfoService.updatePluginConfiguration(bindingDto.getId(), bindingDto.getPluginConfiguration(), bindingDto.getTemplateId());
        if (!updateResult) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }

        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @GetMapping(params = {"Action=DescribeBindingPlugin"})
    public String getBindingPlugin(@RequestParam(value = "PluginBindingInfoId") long pluginBindingInfoId) {
        logger.info("查询绑定插件详情, pluginBindingInfoId:{}", pluginBindingInfoId);
        EnvoyPluginBindingInfo bindingInfo = envoyPluginInfoService.getPluginBindingInfo(pluginBindingInfoId);
        if (null == bindingInfo) {
            return apiReturn(CommonErrorCode.NoSuchPluginBinding);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("PluginBindingInfo", envoyPluginInfoService.fromMeta(bindingInfo));
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @GetMapping(params = {"Action=DescribeBindingPlugins"})
    public String getBindingPlugins(@RequestParam(value = "GwId", required = false, defaultValue = "0") long gwId,
                                    @RequestParam(value = "BindingObjectId", required = false, defaultValue = "") String bindingObjectId,
                                    @RequestParam(value = "BindingObjectType", required = false, defaultValue = "") String bindingObjectType,
                                    @RequestParam(value = "Pattern", required = false, defaultValue = "") String pattern,
                                    @RequestParam(value = "SortByKey", required = false, defaultValue = "create_time") String sortKey,
                                    @RequestParam(value = "SortByValue", required = false, defaultValue = "desc") String sortValue,
                                    @Min(0) @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                    @Min(0) @Max(1000) @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit) {
        long projectId = ProjectTraceHolder.getProId();
        logger.info("查询某个对象的绑定插件列表， gwId:{}, bindingObjectId:{} bindingObjectType:{}, offset:{}, limit:{}, projectId:{}, pattern:{}, sortKey:{}, sortValue:{}",
                gwId, bindingObjectId, bindingObjectType, offset, limit, projectId, pattern, sortKey, sortValue);
        if (StringUtils.isNotBlank(bindingObjectId) && StringUtils.isBlank(bindingObjectType)) {
            logger.info("查询绑定插件列表时，参数BindingObjectType为空! bindingObjectId:{}", bindingObjectId);
            return apiReturn(CommonErrorCode.MissingParameter("BindingObjectType"));
        }
        List<String> bindingObjectTypeList = StringUtils.isEmpty(bindingObjectType) ? Lists.newArrayList() : Arrays.stream(bindingObjectType.split(",")).map(String::trim).collect(Collectors.toList());
        long totalCount = envoyPluginInfoService.getBindingPluginCount(gwId, projectId, bindingObjectId, bindingObjectTypeList, pattern);
        List<EnvoyPluginBindingInfo> pluginBindingInfoList = envoyPluginInfoService.getBindingPluginList(gwId, projectId, bindingObjectId, bindingObjectTypeList, pattern, offset, limit, sortKey, sortValue);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("TotalCount", totalCount);
        List<EnvoyPluginBindingDto> bindingDtoList =
                pluginBindingInfoList.stream().filter(item -> !envoyPluginInfoService.isInsidePlugin(item)).map(item -> envoyPluginInfoService.fromMeta(item)).collect(Collectors.toList());
        envoyPluginInfoService.fillDtoFiled(bindingDtoList);
        result.put("PluginBindingList", bindingDtoList);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdatePluginBindingStatus", description = "更新插件绑定状态")
    @GetMapping(params = {"Action=UpdatePluginBindingStatus"})
    public String updatePluginStatus(@RequestParam(value = "PluginBindingInfoId") long pluginBindingInfoId,
                                     @Pattern(regexp = "enable|disable", message = "状态仅能为enable或disable") @RequestParam(value = "BindingStatus") String bindingStatus) {
        logger.info("更新插件绑定关系状态! pluginBindingInfoId:{}, bindingStatus:{}", pluginBindingInfoId, bindingStatus);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN, pluginBindingInfoId, null);
        AuditResourceHolder.set(resource);

        ErrorCode checkResult = envoyPluginInfoService.checkUpdatePluginBindingStatus(pluginBindingInfoId, bindingStatus);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        boolean updateSucc = envoyPluginInfoService.updatePluginBindingStatus(pluginBindingInfoId, bindingStatus);
        if (!updateSucc) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(HttpStatus.SC_OK, null, null, null);
    }

    @MethodReentrantLock
    @Audit(eventName = "CopyGlobalPlugin", description = "拷贝全局插件至目标网关")
    @PostMapping(params = {"Action=CopyGlobalPlugin"})
    public String copyGlobalPlugin(@RequestBody CopyGlobalPluginDto copyGlobalPlugin) {
        logger.info("[copyGlobalPlugin] 拷贝全局插件至目标网关 {}", copyGlobalPlugin);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_PLUGIN, copyGlobalPlugin.getPluginId(), null);
        AuditResourceHolder.set(resource);

        ErrorCode checkResult = envoyPluginInfoService.checkCopyGlobalPluginToGateway(copyGlobalPlugin);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        boolean copySuccess = envoyPluginInfoService.copyGlobalPluginToGatewayByGwId(copyGlobalPlugin);
        if (!copySuccess) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturnSuccess(null);
    }
}
