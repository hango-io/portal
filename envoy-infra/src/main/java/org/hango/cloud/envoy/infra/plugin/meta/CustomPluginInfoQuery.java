package org.hango.cloud.envoy.infra.plugin.meta;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.PageQuery;

/**
 * @Author zhufengwei
 * @Date 2023/7/5
 */
@Getter
@Setter
@Builder
public class CustomPluginInfoQuery extends PageQuery {
    /**
     * 插件类型
     */
    private String pluginType;
    /**
     * 插件名称
     */
    private String pluginName;
}
