package org.hango.cloud.common.infra.route.common;


import lombok.Getter;
import lombok.Setter;

/**
 * 路由规则发布目的服务Info
 *
 * @author hzchenzhongyang 2019-09-19
 */
@Getter
@Setter
public class DestinationInfo {

    /**
     * 路由规则目标服务serviceId
     */
    private Long serviceId;

    /**
     * 路由规则目标服务权重
     */
    private Long weight;

    /**
     * 路由规则目标服务端口
     */
    private Integer port;

    /**
     * subset名称
     */
    private String subsetName;

    /**
     * 流量镜像配置服务的类型
     * | 服务: application
     * | 版本: subset
     */
    private String mirrorType;

}
