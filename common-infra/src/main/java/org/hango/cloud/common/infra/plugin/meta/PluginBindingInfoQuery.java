package org.hango.cloud.common.infra.plugin.meta;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.PageQuery;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/7/25
 */
@Getter
@Setter
@Builder
public class PluginBindingInfoQuery extends PageQuery {

    private Long projectId;

    private Long virtualGwId;

    private String bindingObjectId;

    private String bindingObjectType;

    private List<String> bindingObjectTypes;

    private String bindingStatus;

    private Long templateId;

    private List<String> pluginType;

    /**
     * 需要排除的插件类型
     */
    private List<String> excludedPluginType;

    private String pattern;

    private String sortByKey;
}
