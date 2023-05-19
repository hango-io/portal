package org.hango.cloud.common.infra.route.pojo;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Getter
@Setter
public class RouteMapMatchInfo {
    /**
     * query: ?key=true
     * header: x-auth-key = value
     */
    private String key;
    /**
     * string match
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
