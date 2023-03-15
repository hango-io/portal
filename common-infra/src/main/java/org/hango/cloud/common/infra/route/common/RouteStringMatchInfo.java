package org.hango.cloud.common.infra.route.common;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * RouteStringMatchInfo，定义StringMatch
 * exact: "value" for exact string match
 * prefix: "value" for prefix-based match
 * regex: "value" for ECMAscript style regex-based match
 *
 * @author hanjiahao
 */
public class RouteStringMatchInfo {
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
        if (!CollectionUtils.isEmpty(value)){
            Collections.sort(value);
        }
        return JSONObject.toJSONString(this);
    }
}
