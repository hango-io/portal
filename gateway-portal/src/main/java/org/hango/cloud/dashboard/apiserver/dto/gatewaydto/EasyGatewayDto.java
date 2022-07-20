package org.hango.cloud.dashboard.apiserver.dto.gatewaydto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * 简单的网关对象，仅包含少数网关元数据，用于显示
 *
 * @author yutao04
 * @date 2022/2/18 14:10
 */
public class EasyGatewayDto {
    @JSONField(name = "GwId")
    private long id;

    /**
     * 网关类型，scg/envoy
     */
    @JSONField(name = "GwType")
    @NotEmpty(message = "网关类型不能为空")
    @Pattern(regexp = "envoy", message = "网关类型填写错误")
    private String gwType;

    /**
     * 网关名称
     */
    @JSONField(name = "GwName")
    @NotEmpty
    @Pattern(regexp = Const.REGEX_GATEWAY_NAME)
    private String gwName;

    /**
     * 网关描述
     */
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    /**
     * 用于从完整的网关对象转换为EasyGatewayDto
     *
     * @param gatewayInfo 完整的网关信息
     * @return 简单网关对象
     */
    public static EasyGatewayDto fromMeta(GatewayInfo gatewayInfo) {
        return BeanUtil.copy(gatewayInfo, EasyGatewayDto.class);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGwType() {
        return gwType;
    }

    public void setGwType(String gwType) {
        this.gwType = gwType;
    }

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
