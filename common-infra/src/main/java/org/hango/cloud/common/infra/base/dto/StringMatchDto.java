package org.hango.cloud.common.infra.base.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Pattern;

/**
 * header、param对应的String Match
 *
 * @author hanjiahao
 */
public class StringMatchDto {
    @JSONField(name = "Key")
    private String key;
    /**
     * 匹配方式
     */
    @JSONField(name = "Type")
    @Pattern(regexp = "exact|prefix|regex")
    private String type;

    /**
     * 匹配值
     */
    @JSONField(name = "Value")
    private String value;

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
