package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * EnvoyFilter dto
 * 同yml格式
 *
 * @author xin li
 * @date 2022/5/13 14:29
 */
@Getter
@Setter
public class EnvoyFilterDTO {
    /**
     * filter名称
     */
    @JSONField(name = "Name")
    private String name;

    /**
     * 网关
     */
    @JSONField(name = "GwCluster")
    private String gwCluster;
    /**
     * 网关的端口号
     */
    @JSONField(name = "PortNumber")
    private Integer portNumber;

}
