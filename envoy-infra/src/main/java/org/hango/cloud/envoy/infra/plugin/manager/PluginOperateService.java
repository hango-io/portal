package org.hango.cloud.envoy.infra.plugin.manager;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;

/**
 * @Author zhufengwei
 * @Date 2023/7/28
 */
public interface PluginOperateService {
    /**
     * 创建插件
     */
    ErrorCode create(BindingPluginDto plugin);

    /**
     * 更新插件
     */
    ErrorCode update(BindingPluginDto plugin);

    /**
     * 删除插件
     */
    ErrorCode delete(BindingPluginDto plugin);

    /**
     * 获取插件operateService
     */
    BindingObjectTypeEnum getBindingObjectType();
}
