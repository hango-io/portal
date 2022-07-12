package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.gdashboard.api.util.Const;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2019/1/6 下午11:13.
 */
public class WebServiceRequestParamDto {

    @JSONField(name = "ParamSort")
    private int paramSort;

    @JSONField(name = "ParamName")
    @NotEmpty
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String paramName;

    @JSONField(name = "ParamType")
    private String paramType;

    @JSONField(name = "ParamTypeId")
    private long paramTypeId;

    @JSONField(name = "ArrayDataTypeId")
    private long arrayDataTypeId;

    private String arrayDataTypeName;

    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    public int getParamSort() {
        return paramSort;
    }

    public void setParamSort(int paramSort) {
        this.paramSort = paramSort;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public long getParamTypeId() {
        return paramTypeId;
    }

    public void setParamTypeId(long paramTypeId) {
        this.paramTypeId = paramTypeId;
    }

    public long getArrayDataTypeId() {
        return arrayDataTypeId;
    }

    public void setArrayDataTypeId(long arrayDataTypeId) {
        this.arrayDataTypeId = arrayDataTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArrayDataTypeName() {
        return arrayDataTypeName;
    }

    public void setArrayDataTypeName(String arrayDataTypeName) {
        this.arrayDataTypeName = arrayDataTypeName;
    }
}
