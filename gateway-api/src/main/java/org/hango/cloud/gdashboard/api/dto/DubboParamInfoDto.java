package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.gdashboard.api.util.Const;

import javax.validation.constraints.Pattern;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/16
 */
public class DubboParamInfoDto {

    /**
     * 主键
     */
    @JSONField(name = "Id")
    private Long id;

    /**
     * 创建时间
     */
    @JSONField(name = "CreateDate")
    private Long createDate;

    /**
     * 修改时间
     */
    @JSONField(name = "ModifyDate")
    private Long modifyDate;

    /**
     * api id
     */
    @JSONField(name = "ApiId")
    private Long apiId;

    /**
     * 参数名称
     */
    @JSONField(name = "ParamName")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String paramName;

    /**
     * 参数类型
     */
    @JSONField(name = "ParamType")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String paramType;

    /**
     * 是否必输项, 1表示必须输入，0表示非必须
     */
    @JSONField(name = "Required")
    private String required;

    /**
     * 默认值
     */
    @JSONField(name = "DefValue")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String defValue;

    /**
     * 描述
     */
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    /**
     * 区分DubboInterface|DubboMethod|DubboVersion|DubboGroup|DubboParam
     */
    @JSONField(name = "DubboType")
    private String dubboType;

    @JSONField(name = "ParamTypeId")
    private Long paramTypeId;

    @JSONField(name = "ArrayDataTypeId")
    private Long arrayDataTypeId;

    @JSONField(name = "ArrayDataTypeName")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String arrayDataTypeName;

    /**
     * 参数序号
     */
    @JSONField(name = "ParamSort")
    private Integer paramSort;

    /**
     * 参数别名
     */
    @JSONField(name = "ParamAlias")
    @Pattern(regexp = Const.REGEX_COMMON)
    private String paramAlias;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }

    public Long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
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

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public String getDefValue() {
        return defValue;
    }

    public void setDefValue(String defValue) {
        this.defValue = defValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDubboType() {
        return dubboType;
    }

    public void setDubboType(String dubboType) {
        this.dubboType = dubboType;
    }

    public Long getParamTypeId() {
        return paramTypeId;
    }

    public void setParamTypeId(Long paramTypeId) {
        this.paramTypeId = paramTypeId;
    }

    public Long getArrayDataTypeId() {
        return arrayDataTypeId;
    }

    public void setArrayDataTypeId(Long arrayDataTypeId) {
        this.arrayDataTypeId = arrayDataTypeId;
    }

    public String getArrayDataTypeName() {
        return arrayDataTypeName;
    }

    public void setArrayDataTypeName(String arrayDataTypeName) {
        this.arrayDataTypeName = arrayDataTypeName;
    }

    public Integer getParamSort() {
        return paramSort;
    }

    public void setParamSort(Integer paramSort) {
        this.paramSort = paramSort;
    }

    public String getParamAlias() {
        return paramAlias;
    }

    public void setParamAlias(String paramAlias) {
        this.paramAlias = paramAlias;
    }
}
