package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;

import java.io.Serializable;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/5/31
 */
@Getter
@Setter
public class IngressRuleViewDTO implements Serializable {
    private static final long serialVersionUID = 6119134222096542998L;
    /**
     * 域名
     */
    @JSONField(name = "Host")
    private String host;

    /**
     * 域名
     */
    @JSONField(name = "DomainId")
    private Long domainId;

    /**
     * path
     */
    @JSONField(name = "Path")
    private String path;

    /**
     * 名称
     */
    @JSONField(name = "PathType")
    private String pathType;

    /**
     * 名称
     */
    @JSONField(name = "ServiceName")
    private String serviceName;

    /**
     * 名称
     */
    @JSONField(name = "ServicePort")
    private Integer servicePort;


    /**
     * 插件列表
     */
    @JSONField(name = "Plugins")
    private List<PluginBindingDto> pluginBindingDtos;
}
