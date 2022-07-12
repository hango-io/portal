package org.hango.cloud.dashboard.envoy.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * EnvoyRouteStringMatchInfo，定义StringMatch
 * exact: "value" for exact string match
 * prefix: "value" for prefix-based match
 * regex: "value" for ECMAscript style regex-based match
 *
 * @author hanjiahao
 */
public class EnvoyRouteStringMatchInfo {
    /**
     * string type
     */
    private String type;
    /**
     * match value
     */
    private List<String> value;

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
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
