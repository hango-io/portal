package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteRuleMapMatchInfo;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 路由规则virtualService 匹配方式
 * 针对header和query
 *
 * @author hanjiahao
 */
public class EnvoyRouteRuleMapMatchDto {

    /**
     * 路由规则匹配，header key或queryString key
     */
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
    @JSONField(name = "Value")
    private List<String> value;

    public static EnvoyRouteRuleMapMatchDto fromMeta(EnvoyRouteRuleMapMatchInfo meta) {
        return BeanUtil.copy(meta, EnvoyRouteRuleMapMatchDto.class);
    }

    public static EnvoyRouteRuleMapMatchDto sortValue(EnvoyRouteRuleMapMatchDto dto) {
        dto.setKey(dto.getKey().trim());
        dto.setValue(dto.getValue().stream().map(s -> s.trim()).sorted().collect(Collectors.toList()));
        return dto;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public EnvoyRouteRuleMapMatchInfo toMeta() {
        return BeanUtil.copy(this, EnvoyRouteRuleMapMatchInfo.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnvoyRouteRuleMapMatchDto objDto = (EnvoyRouteRuleMapMatchDto) o;
        return Objects.equals(getKey(), objDto.getKey()) &&
                Objects.equals(getType(), objDto.getType()) &&
                getValue().containsAll(objDto.getValue()) && objDto.getValue().containsAll(getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getType(), getValue());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
