package org.hango.cloud.common.infra.virtualgateway.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author zhufengwei
 * @Date 2023/5/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualGatewaySetting {
    /**
     * ip获取方式 xff/customHeader
     */
    private String ipSource;

    /**
     * 配置记录XFF右起第几跳IP
     */
    private Integer xffNumTrustedHops;

    /**
     * ip自定义header名称
     */
    private String customIpAddressHeader;
}
