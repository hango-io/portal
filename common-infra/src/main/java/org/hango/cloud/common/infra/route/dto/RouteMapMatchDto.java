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

import static org.hango.cloud.common.infra.base.meta.RegexConst.REGEX_MATCH_KEY;

/**
 * 路由规则virtualService 匹配方式
 * 针对header和query
 *
 * @author hanjiahao
 */
@Getter
@Setter
public class RouteMapMatchDto {

    /**
     * 路由规则匹配，header key或queryString key
     */
    @Pattern(regexp = REGEX_MATCH_KEY, message = "路由规则匹配名称支持字母数字中划线，长度1-64个字符")
    @JSONField(name = "Key")
    private String key;
    /**
     * 路由规则匹配方式
     */
    @JSONField(name = "Type")
    @Pattern(regexp = "exact|prefix|regex")
    private String type;

    /**
     * 路由规则匹配值
     */
    @Size(max = 5, message = "路由规则匹配值仅支持配置至多5组")
    @StringCheckInList(max = 100, unique = true, message = "路由规则匹配值支持最长100个字符，不允许重复")
    @JSONField(name = "Value")
    private List<String> value;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
