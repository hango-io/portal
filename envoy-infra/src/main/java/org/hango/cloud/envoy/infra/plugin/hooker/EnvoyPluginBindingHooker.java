package org.hango.cloud.envoy.infra.plugin.hooker;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.invoker.MethodAroundHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.hooker.AbstractPluginBindingHooker;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.hango.cloud.envoy.infra.plugin.service.IEnvoyPluginInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
    private IPluginTemplateService pluginTemplateService;

    @Override
    protected void preCreateHook(PluginBindingDto pluginBindingDto) {
        BindingPluginDto bindingPluginDto = BindingPluginDto.createBindingPluginFromDto(pluginBindingDto);
        long projectId = ProjectTraceHolder.getProId();
        long templateId = pluginBindingDto.getTemplateId();
        if (PluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL.equals(bindingPluginDto.getBindingObjectType())) {
            bindingPluginDto.setBindingObjectId(projectId);
        }
        PluginTemplateDto templateInfo;
        // 若有模板信息则插件配置来源于模板数据
        if (0 < templateId) {
            templateInfo = pluginTemplateService.get(templateId);
            if (null == templateInfo) {
                throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }
            bindingPluginDto.setPluginConfiguration(templateInfo.getPluginConfiguration());
        }

        boolean success = envoyPluginInfoService.publishGatewayPlugin(bindingPluginDto);
        if (!success) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void preUpdateHook(PluginBindingDto pluginBindingDto) {
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
        if (PluginBindingInfo.BINDING_STATUS_ENABLE.equals(pluginBindingDto.getBindingStatus())) {
            boolean result;
            if (!pluginBindingDtoInDB.getBindingStatus().equals(pluginBindingDto.getBindingStatus())) {
                // 配置下发和数据库中的状态不一致，则是插件启、禁用场景；启用插件场景，api-plane行为是发布插件
                result = envoyPluginInfoService.publishGatewayPlugin(bindingPluginDto);
            } else {
                // 插件启用状态下更新api-plane的插件资源
                result = envoyPluginInfoService.updateGatewayPlugin(bindingPluginDto, pluginBindingDto.getId());
            }
            if (!result) {
                logger.error("update plugin config to api-plane failed. plugin_id: {}", pluginBindingDto.getId());
                throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }
        } else {
            if (!envoyPluginInfoService.deleteGatewayPlugin(bindingPluginDto, pluginBindingDto.getId())) {
                throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void preDeleteHook(PluginBindingDto pluginBindingDto) {
        BindingPluginDto bindingPluginDto = BindingPluginDto.createBindingPluginFromPluginBindingInfo(pluginInfoService.toMeta(pluginBindingDto));
        if (!envoyPluginInfoService.deleteGatewayPlugin(bindingPluginDto, pluginBindingDto.getId())) {
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

        bindingPlugin.setBindingObjectType(BaseConst.PLUGIN_TYPE_GLOBAL);
        bindingPlugin.setVirtualGwId(copyGlobalPlugin.getVirtualGwId());
        List<PluginBindingInfo> sameTypePlugins = pluginInfoService.getPluginBindingListByVirtualGwIdAndTypeAndProjectId(bindingPlugin, copyGlobalPlugin.getProjectId());
        if (CollectionUtils.isEmpty(sameTypePlugins)) {
            if (PluginBindingInfo.BINDING_STATUS_ENABLE.equals(oldPlugin.getBindingStatus())) {
                // 调用api-plane创建GP
                boolean success = envoyPluginInfoService.publishGatewayPlugin(bindingPlugin);
                if (!success) {
                    throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
                }

            }
        } else {
            boolean success = true;
            PluginBindingInfo pluginBindingInfo = sameTypePlugins.get(0);
            if (oldPlugin.getBindingStatus().equals(pluginBindingInfo.getBindingStatus())) {
                if (pluginBindingInfo.getBindingStatus().equals(PluginBindingInfo.BINDING_STATUS_ENABLE)) {
                    // 都是启用场景，需调用api-plane更新GP配置
                    success = envoyPluginInfoService.updateGatewayPlugin(bindingPlugin, pluginBindingInfo.getId());
                }
            } else {
                if (pluginBindingInfo.getBindingStatus().equals(PluginBindingInfo.BINDING_STATUS_ENABLE)) {
                    // 新插件启用，旧插件禁用场景，需调用api-plane创建GP
                    success = envoyPluginInfoService.publishGatewayPlugin(bindingPlugin);
                } else {
                    // 新插件禁用，旧插件启用场景，需调用api-plane下线GP
                    success = envoyPluginInfoService.deleteGatewayPlugin(bindingPlugin, pluginBindingInfo.getId());
                }
            }
            if (!success) {
                throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
        return true;
    }

}
