package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author zhufengwei
 * @Date 2023/5/10
 */
@Getter
@Setter
public class IpSourceEnvoyFilterDTO extends EnvoyFilterDTO{
    /**
     * 自定义Ip地址获取方式
     */
    @JSONField(name = "CustomIpAddressHeader")
    private String customIpAddressHeader;

    /**
     * 配置记录XFF右起第几跳IP(默认为0)
     */
    @JSONField(name = "XffNumTrustedHops")
    private Integer xffNumTrustedHops;
}
