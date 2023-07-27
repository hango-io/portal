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
    //todo sx
    @JSONField(name = "BindingObjectId")
    private Long Id;
    @JSONField(name = "BindingObjectType")
    private String bindingObjectType;
    @JSONField(name = "BindingObjectName")
    private String bindingObjectName;
    //todo sx
    @JSONField(name = "GwName")
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
