package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName CustomPluginInstanceDto
 * @Description 网关作用实例
 * @Author xianyanglin
 * @Date 2023/7/10 18:01
 */
@Getter
@Setter
@Builder
public class CustomPluginInstanceDto {
    /**
     * 插件实例id
     */
    @JSONField(name = "PluginBindingInfoId")
    private Long id;
    /**
     * 插件绑定对象id，与bindingObjectType共同确定一个具体对象
     */
    @JSONField(name = "BindingObjectId")
    private String bindingObjectId;

    @JSONField(name = "BindingObjectType")
    private String bindingObjectType;
    @JSONField(name = "BindingObjectName")
    private String bindingObjectName;
    @JSONField(name = "VirtualGwName")
    private String virtualGwName;
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;
    @JSONField(name = "Project")
    private String project;
    @JSONField(name = "UpdateTime")
    private Long updateTime;
    @JSONField(name = "BindingStatus")
    private String bindingStatus;
}
