package org.hango.cloud.common.infra.route.common;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

public class RouteRuleMapMatchInfo {
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

    @Override
    public String toString() {
        if (!CollectionUtils.isEmpty(value)){
            Collections.sort(value);
        }
        return JSONObject.toJSONString(this);
    }
}
