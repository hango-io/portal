package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteStringMatchInfo;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 路由规则StringMatch VirtualService,StringMatch
 * exact: "value" for exact string match
 * prefix: "value" for prefix-based match
 * regex: "value" for ECMAscript style regex-based match
 */
public class EnvoyRouteStringMatchDto {

    /**
     * StringMatch 匹配方式
     */
    @JSONField(name = "Type")
    @Pattern(regexp = "exact|prefix|regex")
    private String type;
    /**
     * StringMatch 匹配值
     */
    @JSONField(name = "Value")
    private List<String> value;

    public EnvoyRouteStringMatchDto(@Pattern(regexp = "exact|prefix|regex") String type, List<String> value) {
        this.type = type;
        this.value = value;
    }

    public EnvoyRouteStringMatchDto() {
    }

    public static EnvoyRouteStringMatchInfo toMeta(EnvoyRouteStringMatchDto dto) {
        EnvoyRouteStringMatchInfo meta = new EnvoyRouteStringMatchInfo();
        meta.setType(dto.getType());
        meta.setValue(dto.getValue().stream().map(s -> s.trim()).collect(Collectors.toList()));
        return meta;
    }

    public static EnvoyRouteStringMatchDto fromMeta(EnvoyRouteStringMatchInfo meta) {
        return BeanUtil.copy(meta, EnvoyRouteStringMatchDto.class);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnvoyRouteStringMatchDto objDto = (EnvoyRouteStringMatchDto) o;
        return Objects.equals(getType(), objDto.getType()) &&
                getValue().containsAll(objDto.getValue()) && objDto.getValue().containsAll(getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
