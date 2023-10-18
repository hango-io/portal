package org.hango.cloud.envoy.infra.plugin.hooker;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.plugin.convert.PluginInfoConvertService;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.hooker.AbstractPluginBindingHooker;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.hango.cloud.envoy.infra.plugin.manager.IPluginOperateManagerService;
import org.hango.cloud.envoy.infra.plugin.service.IEnvoyPluginInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.ENABLE_STATE;

/**
 * @author xin li
 * @date 2022/9/21 17:20
 */
@Service
public class EnvoyPluginBindingHooker extends AbstractPluginBindingHooker<PluginBindingInfo, PluginBindingDto> {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyPluginBindingHooker.class);

    @Override
    public int getOrder() {
        return 200;
    }


    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @Autowired
    private IPluginOperateManagerService pluginOperateManagerService;

    @Autowired
    private IPluginTemplateService pluginTemplateService;

    @Autowired
    private PluginInfoConvertService pluginInfoConvertService;

    @Override
    protected void preCreateHook(PluginBindingDto pluginBindingDto) {
        ErrorCode errorCode = pluginOperateManagerService.create(BindingPluginDto.createBindingPluginFromDto(pluginBindingDto));
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void preUpdateHook(PluginBindingDto pluginBindingDto) {
        pluginInfoConvertService.fillPluginInfo(pluginBindingDto);
        PluginBindingDto pluginBindingDtoInDB = pluginInfoService.get(pluginBindingDto.getId());
        if (null == pluginBindingDtoInDB) {
            logger.error("更新插件配置时指定的绑定关系不存在! pluginBindingInfoId:{}", pluginBindingDto.getId());
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        BindingPluginDto bindingPluginDto = BindingPluginDto.createBindingPluginFromPluginBindingInfo(pluginInfoService.toMeta(pluginBindingDto));
        if (StringUtils.isNotEmpty(pluginBindingDto.getPluginConfiguration())) {
            //更新插件配置UpdatePluginConfiguration
            bindingPluginDto.setPluginConfiguration(pluginBindingDto.getPluginConfiguration());
        }
        ErrorCode result;
        if (ENABLE_STATE.equals(pluginBindingDto.getBindingStatus())) {
            if (!pluginBindingDtoInDB.getBindingStatus().equals(pluginBindingDto.getBindingStatus())) {
                // 配置下发和数据库中的状态不一致，则是插件启、禁用场景；启用插件场景，api-plane行为是发布插件
                result = pluginOperateManagerService.create(bindingPluginDto);
            } else {
                bindingPluginDto.addPluginId(pluginBindingDto.getId());
                // 插件启用状态下更新api-plane的插件资源
                result = pluginOperateManagerService.update(bindingPluginDto);
            }
        } else {
            bindingPluginDto.addPluginId(pluginBindingDto.getId());
            result = pluginOperateManagerService.delete(bindingPluginDto);
        }
        if (!CommonErrorCode.SUCCESS.equals(result)) {
            logger.error(" plugin config to api-plane failed. pluginInfo: {}", JSONObject.toJSONString(bindingPluginDto));
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void preDeleteHook(PluginBindingDto pluginBindingDto) {
        BindingPluginDto bindingPluginDto = BindingPluginDto.createBindingPluginFromPluginBindingInfo(pluginInfoService.toMeta(pluginBindingDto));
        bindingPluginDto.addPluginId(pluginBindingDto.getId());
        ErrorCode errorCode = pluginOperateManagerService.delete(bindingPluginDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

    }

    //TODO copy or create impl
    @Override
    public boolean preCopyGlobalPluginToGatewayByVirtualGwId(CopyGlobalPluginDto copyGlobalPlugin) {
        PluginBindingDto oldPlugin = pluginInfoService.get(copyGlobalPlugin.getPluginId());
        oldPlugin.setVirtualGwId(copyGlobalPlugin.getVirtualGwId());


        // 查询目标网关下相同类型的全局插件（项目级）
        BindingPluginDto bindingPlugin = BindingPluginDto.createBindingPluginFromPluginBindingInfo(pluginInfoService.toMeta(oldPlugin));

        bindingPlugin.setBindingObjectType(BindingObjectTypeEnum.GLOBAL.getValue());
        bindingPlugin.setVirtualGwId(copyGlobalPlugin.getVirtualGwId());
        PluginBindingInfoQuery query = PluginBindingInfoQuery.builder()
                .virtualGwId(bindingPlugin.getVirtualGwId())
                .pluginType(Collections.singletonList(bindingPlugin.getPluginType()))
                .bindingObjectType(bindingPlugin.getBindingObjectType())
                .projectId(copyGlobalPlugin.getProjectId())
                .build();
        List<PluginBindingDto> sameTypePlugins = pluginInfoService.getBindingPluginInfoList(query);
        ErrorCode errorCode = CommonErrorCode.SUCCESS;
        if (CollectionUtils.isEmpty(sameTypePlugins)) {
            if (ENABLE_STATE.equals(oldPlugin.getBindingStatus())) {
                // 调用api-plane创建GP
                errorCode = pluginOperateManagerService.create(bindingPlugin);
            }
        } else {
            PluginBindingDto pluginBindingInfo = sameTypePlugins.get(0);
            if (oldPlugin.getBindingStatus().equals(pluginBindingInfo.getBindingStatus())) {
                if (pluginBindingInfo.getBindingStatus().equals(ENABLE_STATE)) {
                    // 都是启用场景，需调用api-plane更新GP配置
                    bindingPlugin.addPluginId(pluginBindingInfo.getId());
                    errorCode = pluginOperateManagerService.update(bindingPlugin);
                }
            } else {
                if (pluginBindingInfo.getBindingStatus().equals(ENABLE_STATE)) {
                    // 新插件启用，旧插件禁用场景，需调用api-plane创建GP
                    errorCode = pluginOperateManagerService.create(bindingPlugin);
                } else {
                    // 新插件禁用，旧插件启用场景，需调用api-plane下线GP
                    bindingPlugin.addPluginId(pluginBindingInfo.getId());
                    errorCode = pluginOperateManagerService.delete(bindingPlugin);
                }
            }
        }
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return true;
    }

}
