package org.hango.cloud.common.infra.route.pojo;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
    private Integer weight;

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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
