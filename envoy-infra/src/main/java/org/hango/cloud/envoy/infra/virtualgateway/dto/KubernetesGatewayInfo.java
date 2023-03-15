package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/12/5
 */
@Getter
@Setter
public class KubernetesGatewayInfo {

    /**
     * 名称
     */
    @JSONField(name = "VirtualGatewayId")
    private Long virtualGatewayId;

    /**
     * 名称
     */
    @JSONField(name = "Name")
    private String name;


    /**
     * 所属项目
     */
    @JSONField(name = "ProjectId")
    private Long projectId;



    @JSONField(name = "Protocol")
    private String protocol;

    /**
     * 监听域名
     */
    @JSONField(name = "Host")
    private String host;

    /**
     * 监听域名
     */
    @JSONField(name = "RouteHosts")
    private List<String> routeHosts;


    /**
     * 监听端口
     */
    @JSONField(name = "Port")
    private int port;



    /**
     * 配置详细内容
     */
    @JSONField(name = "Content")
    private String content;
}
