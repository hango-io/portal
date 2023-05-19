package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.BaseConst;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/5/5
 */
@Getter
@Setter
public class UpdateRouteDto extends RouteMatchDto implements Serializable {
    private static final long serialVersionUID = -6109860680574545590L;

    @NotNull
    @JSONField(name = "Id")
    private Long id;

    /**
     * 路由规则别名，可选填
     */
    @Size(max = 100, message = "路由别名长度不能超过100个字符")
    @JSONField(name = "Alias")
    private String alias;

    /**
     * 描述信息
     */
    @Size(max = 200, message = "路由描述信息长度不能超过200个字符")
    @JSONField(name = "Description")
    private String description;

    /**
     * 路由关联服务信息
     */
    @Valid
    @JSONField(name = "ServiceMetaForRoute")
    private List<ServiceMetaForRouteDto> serviceMetaForRouteDtos;

    /**
     * 使能状态
     */
    @JSONField(name = "EnableState")
    @Pattern(regexp = "enable|disable", message = "无效的路由使能状态")
    private String enableState = BaseConst.DISABLE_STATE;

    /**
     * 路由超时时间
     */
    @JSONField(name = "Timeout")
    @Min(0)
    @Max(1024000)
    private Long timeout = 60000L;

    /**
     * route retry policy
     */
    @JSONField(name = "HttpRetry")
    @Valid
    private HttpRetryDto httpRetryDto;

}
