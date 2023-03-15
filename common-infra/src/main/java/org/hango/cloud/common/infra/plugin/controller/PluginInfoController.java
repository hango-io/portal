package org.hango.cloud.common.infra.plugin.controller;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.annotation.MethodReentrantLock;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.operationaudit.annotation.Audit;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import java.util.List;
import java.util.Map;

/**
 * @author hzchenzhongyang 2019-11-05
 * <p>
 * 插件信息controller层实现类
 */
@RestController
@Validated
@RequestMapping(value = { BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class PluginInfoController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(PluginInfoController.class);

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IPluginTemplateService pluginTemplateService;

    @MethodReentrantLock
    @Audit(eventName = "BindingPlugin", description = "绑定插件")
    @PostMapping(params = {"Action=BindingPlugin"})
    public String bindingPlugin(@Validated @RequestBody PluginBindingDto pluginBindingDto) {
        logger.info("绑定插件， pluginBindingDto:{}", pluginBindingDto);
        ErrorCode checkResult = pluginInfoService.checkCreateParam(pluginBindingDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        long result = pluginInfoService.create(pluginBindingDto);
        if (BaseConst.ERROR_RESULT == result) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @MethodReentrantLock
    @Audit(eventName = "UnbindingPlugin", description = "解绑插件")
    @GetMapping(params = {"Action=UnbindingPlugin"})
    public String unbindingPlugin(@RequestParam(value = "PluginBindingInfoId") long pluginBindingInfoId) {
        // 由于一个对象可以绑定多个一样的插件，所以解绑的时候需要指定具体的绑定关系
        logger.info("解绑插件, pluginBindingInfoId:{}", pluginBindingInfoId);

        ErrorCode errorCode = pluginInfoService.checkUnbindParam(pluginBindingInfoId);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)){
            return apiReturn(errorCode);
        }
        pluginInfoService.delete(pluginInfoService.get(pluginBindingInfoId));
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdatePluginConfiguration", description = "更新插件配置")
    @PostMapping(params = {"Action=UpdatePluginConfiguration"})
    public String updatePluginConfiguration(@RequestBody PluginBindingDto pluginBindingDto) {
        logger.info("更新插件配置, pluginBindingDto:{}", pluginBindingDto);
        ErrorCode errorCode = pluginInfoService.checkUpdateParam(pluginBindingDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        //更新插件只传入了PluginConfiguration，使用数据库中的值
        String pluginConfig;
        PluginBindingDto pluginBindingDtoInDB = pluginInfoService.get(pluginBindingDto.getId());
        if (pluginBindingDto.getTemplateId() > 0) {
            // 传了插件模板id，优先用插件模板同步配置
            PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(pluginBindingDto.getTemplateId());
            pluginConfig = pluginTemplateDto.getPluginConfiguration();
        } else {
            pluginConfig = pluginBindingDto.getPluginConfiguration();
        }
        pluginBindingDtoInDB.setPluginConfiguration(pluginConfig);
        long result = pluginInfoService.update(pluginBindingDtoInDB);
        if (BaseConst.ERROR_RESULT == result) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, null);
    }

    @GetMapping(params = {"Action=DescribeBindingPlugin"})
    public String getBindingPlugin(@RequestParam(value = "PluginBindingInfoId") long pluginBindingInfoId) {
        logger.info("查询绑定插件详情, pluginBindingInfoId:{}", pluginBindingInfoId);
        PluginBindingDto pluginBindingDto = pluginInfoService.get(pluginBindingInfoId);
        if (null == pluginBindingDto) {
            return apiReturn(CommonErrorCode.NO_SUCH_PLUGIN_BINDING);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("PluginBindingInfo", pluginBindingDto);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @GetMapping(params = {"Action=DescribeBindingPlugins"})
    public String getBindingPlugins(@RequestParam(value = "VirtualGwId", required = false, defaultValue = "0") long virtualGwId,
                                    @RequestParam(value = "BindingObjectId", required = false, defaultValue = "") String bindingObjectId,
                                    @RequestParam(value = "BindingObjectType", required = false, defaultValue = "") String bindingObjectType,
                                    @RequestParam(value = "Pattern", required = false, defaultValue = "") String pattern,
                                    @RequestParam(value = "SortByKey", required = false, defaultValue = "create_time") String sortKey,
                                    @RequestParam(value = "SortByValue", required = false, defaultValue = "desc") String sortValue,
                                    @Min(0) @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                    @Min(0) @Max(1000) @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit) {
        long projectId = ProjectTraceHolder.getProId();
        logger.info("查询某个对象的绑定插件列表， virtualGwId:{}, bindingObjectId:{} bindingObjectType:{}, offset:{}, limit:{}, projectId:{}, pattern:{}, sortKey:{}, sortValue:{}",
                virtualGwId, bindingObjectId, bindingObjectType, offset, limit, projectId, pattern, sortKey, sortValue);
        ErrorCode checkResult = pluginInfoService.checkDescribeBindingPlugins(bindingObjectId, bindingObjectType);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }

        long totalCount = pluginInfoService.getBindingPluginCountExcludedInnerPlugins(virtualGwId, projectId, bindingObjectId, bindingObjectType, pattern);
        List<PluginBindingDto> pluginBindingInfoList = pluginInfoService.getBindingPluginListOutSide(virtualGwId, projectId, bindingObjectId, bindingObjectType, pattern, offset, limit, sortKey, sortValue);
        Map<String, Object> result = Maps.newHashMap();
        result.put("TotalCount", totalCount);
        result.put("PluginBindingList", pluginBindingInfoList);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdatePluginBindingStatus", description = "更新插件绑定状态")
    @GetMapping(params = {"Action=UpdatePluginBindingStatus"})
    public String updatePluginStatus(@RequestParam(value = "PluginBindingInfoId") long pluginBindingInfoId,
                                     @Pattern(regexp = "enable|disable", message = "状态仅能为enable或disable") @RequestParam(value = "BindingStatus") String bindingStatus) {
        logger.info("更新插件绑定关系状态! pluginBindingInfoId:{}, bindingStatus:{}", pluginBindingInfoId, bindingStatus);
        ErrorCode checkResult = pluginInfoService.checkUpdatePluginBindingStatus(pluginBindingInfoId, bindingStatus);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        PluginBindingDto pluginBindingDto = pluginInfoService.get(pluginBindingInfoId);
        pluginBindingDto.setBindingStatus(bindingStatus);
        long result = pluginInfoService.update(pluginBindingDto);
        if (BaseConst.ERROR_RESULT == result) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturn(HttpStatus.SC_OK, null, null, null);
    }

    @MethodReentrantLock
    @Audit(eventName = "CopyGlobalPlugin", description = "拷贝全局插件至目标网关")
    @PostMapping(params = {"Action=CopyGlobalPlugin"})
    public String copyGlobalPlugin(@RequestBody CopyGlobalPluginDto copyGlobalPlugin) {
        logger.info("[copyGlobalPlugin] 拷贝全局插件至目标网关 {}", copyGlobalPlugin);
        ErrorCode checkResult = pluginInfoService.checkCopyGlobalPluginToGateway(copyGlobalPlugin);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }

        boolean copySuccess = pluginInfoService.copyGlobalPluginToGatewayByVirtualGwId(copyGlobalPlugin);
        if (!copySuccess) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturnSuccess(null);
    }

}
