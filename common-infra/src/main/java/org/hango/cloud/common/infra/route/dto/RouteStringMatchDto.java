package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.Pattern;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 路由规则StringMatch VirtualService,StringMatch
 * exact: "value" for exact string match
 * prefix: "value" for prefix-based match
 * regex: "value" for ECMAscript style regex-based match
 */
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
    @JSONField(name = "Value")
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
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }

    @Override
    public String toString() {
        if (!CollectionUtils.isEmpty(value)){
            Collections.sort(value);
        }
        return JSONObject.toJSONString(this);
    }
}
