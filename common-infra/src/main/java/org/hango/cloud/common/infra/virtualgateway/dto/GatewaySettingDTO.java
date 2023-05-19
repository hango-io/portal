package org.hango.cloud.common.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

import static org.hango.cloud.common.infra.base.meta.RegexConst.REGEX_MATCH_KEY;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/1/9
 */
@Getter
@Setter
public class GatewaySettingDTO implements Serializable {

    private static final long serialVersionUID = 1949653022969532468L;

    @NotNull
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

    /**
     * ip获取方式 xff/customHeader
     */
    @Pattern(regexp = "xff|customHeader")
    @JSONField(name = "IpSource")
    private String ipSource;

    /**
     * 配置记录XFF右起第几跳IP
     */
    @Range(min = 1, max = 100)
    @JSONField(name = "XffNumTrustedHops")
    private Integer xffNumTrustedHops;

    /**
     * ip自定义header名称
     */
    @Pattern(regexp = REGEX_MATCH_KEY)
    @JSONField(name = "CustomIpAddressHeader")
    private String customIpAddressHeader;

}
