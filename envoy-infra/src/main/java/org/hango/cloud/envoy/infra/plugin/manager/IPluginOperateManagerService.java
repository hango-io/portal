package org.hango.cloud.envoy.infra.plugin.manager;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;

/**
 * @Author zhufengwei
 * @Date 2023/7/29
 */
public interface IPluginOperateManagerService {
    ErrorCode create(BindingPluginDto bindingPluginDto);

    ErrorCode update(BindingPluginDto bindingPluginDto);

    ErrorCode delete(BindingPluginDto bindingPluginDto);
}
