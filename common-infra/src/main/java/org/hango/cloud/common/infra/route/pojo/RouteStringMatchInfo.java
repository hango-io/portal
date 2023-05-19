package org.hango.cloud.common.infra.route.pojo;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * RouteStringMatchInfo，定义StringMatch
 * exact: "value" for exact string match
 * prefix: "value" for prefix-based match
 * regex: "value" for ECMAscript style regex-based match
 *
 * @author hanjiahao
 */
@Getter
@Setter
public class RouteStringMatchInfo {
    /**
     * string type
     */
    private String type;
    /**
     * match value
     */
    private List<String> value;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
