package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 网关插件绑定关系dto
 *
 * @author hzchenzhongyang 2019-11-13
 */
@Getter
@Setter
public class PluginBindingDto extends CommonExtensionDto implements Serializable{
    private static final long serialVersionUID = -7200982150875283006L;
    /**
     * 表自增id
     */
    @JSONField(name = "PluginBindingInfoId")
    private Long id;
    /**
     * 对象-插件绑定关系作用的网关id
     */
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;
    /**
     * 插件绑定对象id，与bindingObjectType共同确定一个具体对象
     */
    @JSONField(name = "BindingObjectId")
    @NotNull
    private String bindingObjectId;
    /**
     * 插件绑定对象类型，与bindingObjectId共同确定一个具体对象
     */
    @NotNull
    @Pattern(regexp = "routeRule|service|global|host|gateway", message = "插件范围仅支持routeRule/service/global/host/gateway")
    @JSONField(name = "BindingObjectType")
    private String bindingObjectType;
    /**
     * 插件配置
     */
    @JSONField(name = "PluginConfiguration")
    private String pluginConfiguration;

    /**
     * 绑定关系更新时间，时间戳格式，精确到毫秒
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;
    /**
     * 绑定插件类型
     */
    @NotNull(message = "插件类型不能为空")
    @JSONField(name = "PluginType")
    private String pluginType;
    /**
     * 绑定关系所属项目id
     */
    @JSONField(name = "ProjectId")
    private long projectId;
    /**
     * 状态，disable/enable，disable时插件在数据面不生效
     */
    @JSONField(name = "BindingStatus")
    private String bindingStatus;

    @JSONField(name = "PluginName")
    private String pluginName;

    @JSONField(name = "BindingObjectName")
    private String bindingObjectName;

    //历史原因，当前返回给前端的是GwName，实际是virtualGwName
    @JSONField(name = "GwName")
    private String virtualGwName;

    @JSONField(name = "TemplateId")
    private Long templateId;

    @JSONField(name = "TemplateVersion")
    private Long templateVersion;

    @JSONField(name = "TemplateStatus")
    private long templateStatus;

    @JSONField(name = "GwType")
    private String gwType;
}
