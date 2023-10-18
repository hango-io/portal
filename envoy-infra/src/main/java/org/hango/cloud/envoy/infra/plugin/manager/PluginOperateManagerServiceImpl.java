package org.hango.cloud.envoy.infra.plugin.manager;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/7/28
 */
@Slf4j
@Component
public class PluginOperateManagerServiceImpl implements IPluginOperateManagerService {

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private List<PluginOperateService> pluginOperateServices;

    @Override
    public ErrorCode create(BindingPluginDto bindingPluginDto) {
        //获取operator
        PluginOperateService pluginOperate = getPluginOperate(bindingPluginDto.getBindingObjectType());
        //创建插件
        return pluginOperate.create(bindingPluginDto);
    }

    @Override
    public ErrorCode update(BindingPluginDto bindingPluginDto) {
        List<Long> pluginIdList = bindingPluginDto.getPluginIdList();
        if (CollectionUtils.isEmpty(pluginIdList)){
            log.error("update plugin error, pluginIdList is empty");
            return CommonErrorCode.invalidParameter("无效的参数pluginIdList");
        }
        PluginBindingDto pluginBindingDto = pluginInfoService.get(pluginIdList.get(0));
        if (pluginBindingDto == null){
            log.error("update plugin error, plugin not exist. id:{}", pluginIdList.get(0));
            return CommonErrorCode.invalidParameter("无效的参数id");
        }
        //获取operator
        PluginOperateService pluginOperate = getPluginOperate(pluginBindingDto.getBindingObjectType());
        //更新插件
        return pluginOperate.update(bindingPluginDto);
    }

    @Override
    public ErrorCode delete(BindingPluginDto bindingPluginDto) {
        //获取operator
        PluginOperateService pluginOperate = getPluginOperate(bindingPluginDto.getBindingObjectType());
        //删除插件
        return pluginOperate.delete(bindingPluginDto);
    }

    private PluginOperateService getPluginOperate(String bindingObjectType){
        BindingObjectTypeEnum bindingObjectTypeEnum = BindingObjectTypeEnum.getByValue(bindingObjectType);
        if (bindingObjectTypeEnum == null){
            log.error("bindingObjectType is not exist, bindingObjectType:{}", bindingObjectType);
            throw new RuntimeException("bindingObjectType is not exist");
        }
        PluginOperateService pluginOperateService = pluginOperateServices.stream().filter(o -> o.getBindingObjectType().equals(bindingObjectTypeEnum)).findFirst().orElse(null);
        if (pluginOperateService == null){
            log.error("pluginOperateService is not exist, bindingObjectType:{}", bindingObjectType);
            throw new RuntimeException("暂不支持该类型插件");
        }
        log.info("start handle {} plugin, handler", pluginOperateService.getClass().getName());
        return pluginOperateService;
    }


}
