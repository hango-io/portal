package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.annotation.StringCheckInList;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

/**
 * 路由规则StringMatch VirtualService,StringMatch
 * exact: "value" for exact string match
 * prefix: "value" for prefix-based match
 * regex: "value" for ECMAscript style regex-based match
 */
@Getter
@Setter
public class RouteStringMatchDto {

    /**
     * StringMatch 匹配方式
     */
    @JSONField(name = "Type")
    @Pattern(regexp = "exact|prefix|regex")
    private String type;
    /**
     * StringMatch 匹配值
     */
    @Size(max = 5, message = "路由path匹配值仅支持配置至多5组")
    @StringCheckInList(max = 100, message = "路由path匹配值支持最长100个字符")
    @JSONField(name = "Value")
    private List<String> value;

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}