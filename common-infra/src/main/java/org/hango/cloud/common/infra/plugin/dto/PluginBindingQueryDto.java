package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.PageQuery;

import javax.validation.constraints.Pattern;

/**
 * @Author zhufengwei
 * @Date 2023/7/25
 */
@Getter
@Setter
public class PluginBindingQueryDto extends PageQuery {
    /**
     * 对象-插件绑定关系作用的网关id
     */
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;


    /**
     * 插件绑定对象id，与bindingObjectType共同确定一个具体对象
     */
    @JSONField(name = "BindingObjectId")
    private String bindingObjectId;
    /**
     * 插件绑定对象类型，与bindingObjectId共同确定一个具体对象
     */
    @Pattern(regexp = "routeRule|service|global|host|gateway", message = "插件范围仅支持routeRule/service/global/host/gateway")
    @JSONField(name = "BindingObjectType")
    private String bindingObjectType;

    /**
     * 模糊匹配条件pluginType
     */
    @JSONField(name = "Pattern")
    private String pattern;

    /**
     * 排序条件
     */
    @JSONField(name = "SortByKey")
    @Pattern(regexp = "|create_time|update_time")
    private String sortByKey;
}
