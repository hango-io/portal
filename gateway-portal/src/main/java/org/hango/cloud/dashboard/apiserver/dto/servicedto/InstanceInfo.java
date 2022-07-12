package org.hango.cloud.dashboard.apiserver.dto.servicedto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/6/11
 */
public class InstanceInfo {

    @JSONField(name = "InstanceName")
    @NotEmpty
    private String instanceName;

    @JSONField(name = "ParamValue")
    @NotEmpty
    private String paramValue;

    @JSONField(name = "RegexTag")
    @Max(1)
    @Min(0)
    private int regexTag;

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public int getRegexTag() {
        return regexTag;
    }

    public void setRegexTag(int regexTag) {
        this.regexTag = regexTag;
    }

}
