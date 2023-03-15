package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/12/6
 */
@Getter
@Setter
public class KubernetesGatewayDTO {
    /**
     * 域名id
     */
    @JSONField(name = "DomainId")
    private Long domainId;
    /**
     * 域名
     */
    @JSONField(name = "Hostname")
    private String hostname;


    /**
     * 插件列表
     */
    private List<PluginBindingDto> pluginBindingDtos;


}
