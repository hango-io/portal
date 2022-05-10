package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Pattern;
import java.io.Serializable;


/**
 * 流量染色参数相关dto
 *  @author qilu
 */
public class TrafficMarkParamDto implements Serializable {

    /**
     * 参数名称
     */
    @JSONField(name = "ParamName")
    private String paraName;
    /**
     * ParamMatch 匹配方式 默认正则匹配
     * exact: "value" for exact string match
     * prefix: "value" for prefix-based match
     * regex: "value" for ECMAscript style regex-based match
     */
    @JSONField(name = "Type")
    @Pattern(regexp = "exact|prefix|regex")
    private String type = "regex";
    /**
     * ParamMatch 匹配值
     */
    @JSONField(name = "Value")
    private String value;

    public String getParaName() {
        return paraName;
    }

    public void setParaName(String paraName) {
        this.paraName = paraName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
