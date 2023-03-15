package org.hango.cloud.common.infra.routeproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 同步路由获取网关及同步状态是否相同
 *
 * @author hanjiahao
 */
@Getter
@Setter
@Builder
public class SyncRouteRuleGwDto {

    /**
     * 虚拟网关ID
     */
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;

    /**
     * 所属环境
     */
    @JSONField(name = "EnvId")
    private String envId;

    /**
     * 虚拟网关名称
     */
    @NotBlank
    @JSONField(name = "Name")
    private String name;

    /**
     * 路由元数据是否相同
     */
    @JSONField(name = "IsSameRaw")
    private Boolean isSameRaw;

}
