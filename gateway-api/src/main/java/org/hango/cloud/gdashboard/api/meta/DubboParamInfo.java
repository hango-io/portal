package org.hango.cloud.gdashboard.api.meta;

import org.hango.cloud.gdashboard.api.util.Const;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * nce_gateway_dubbo_param
 *
 * @author
 */
public class DubboParamInfo implements Serializable {
    private static final long serialVersionUID = 7779687793421990036L;
    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private Long createDate;

    /**
     * 修改时间
     */
    private Long modifyDate;

    /**
     * api id
     */
    private Long apiId;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数类型
     */
    private String paramType;

    /**
     * 是否必输项, 1表示必须输入，0表示非必须
     */
    private String required;

    /**
     * 默认值
     */
    private String defValue;

    /**
     * 描述
     */
    private String description;

    /**
     * 区分DubboInterface|DubboMethod|DubboVersion|DubboGroup|DubboParam
     */
    private String dubboType;

    private Long paramTypeId;

    private Long arrayDataTypeId;

    private String arrayDataTypeName;


    /**
     * 参数序号
     */
    private Integer paramSort;

    /**
     * 参数别名
     */
    private String paramAlias;

    public static ApiBody castToApiBody(DubboParamInfo dubboParamInfo) {
        if (dubboParamInfo == null) {
            return null;
        }
        ApiBody apiBody = new ApiBody();

        apiBody.setApiId(dubboParamInfo.getApiId());
        apiBody.setParamName(dubboParamInfo.getParamName());
        apiBody.setParamType(dubboParamInfo.getParamType());
        apiBody.setRequired(dubboParamInfo.getRequired());
        apiBody.setDefValue(dubboParamInfo.getDefValue());
        apiBody.setDescription(dubboParamInfo.getDescription());
        apiBody.setParamTypeId(dubboParamInfo.getParamTypeId());
        apiBody.setArrayDataTypeId(dubboParamInfo.getArrayDataTypeId());
        apiBody.setArrayDataTypeName(dubboParamInfo.getArrayDataTypeName());
        apiBody.setAssociationType(AssociationType.NORMAL.name());
        if (DubboType.DubboParam.name().equals(dubboParamInfo.getDubboType())) {
            apiBody.setType(Const.REQUEST_PARAM_TYPE);
        } else if (DubboType.DubboResponse.name().equals(dubboParamInfo.getDubboType())) {
            apiBody.setType(Const.RESPONSE_PARAM_TYPE);
        }
        return apiBody;
    }

    public static List<ApiBody> castToApiBody(List<DubboParamInfo> dubboParamInfo) {
        if (CollectionUtils.isEmpty(dubboParamInfo)) {
            return null;
        }
        List<ApiBody> apiBodyList = new ArrayList<>();
        for (DubboParamInfo paramInfo : dubboParamInfo) {
            apiBodyList.add(castToApiBody(paramInfo));
        }
        return apiBodyList;
    }

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

    public String getArrayDataTypeName() {
        return arrayDataTypeName;
    }

    public void setArrayDataTypeName(String arrayDataTypeName) {
        this.arrayDataTypeName = arrayDataTypeName;
    }

}