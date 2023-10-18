package org.hango.cloud.common.infra.plugin.meta;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.PageQuery;

/**
 * @Author zhufengwei
 * @Date 2023/7/27
 */
@Getter
@Setter
@Builder
public class PluginTemplateInfoQuery extends PageQuery {

    private Long projectId;

    private String pluginType;

    private String templateName;

    private String sortByKey;
}
