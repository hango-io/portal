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
public class WebServiceParamInfo implements Serializable {

    private static final long serialVersionUID = 7143023953952293269L;

    private long id;

    private long createDate;

    private long modifyDate;

    private long apiId;

    private String paramName;

    private String paramType;

    /**
     * 区分参数的类型，包含webservice接口定义中的Service|Method|RequestParam|ResponseParam四种
     */
    private String type;

    private long paramTypeId;

    private long arrayDataTypeId;

    private String arrayDataTypeName;

    private int paramSort;

    private String description;

    public static ApiBody castToApiBody(WebServiceParamInfo webServiceParamInfo) {
        if (webServiceParamInfo == null) {
            return null;
        }
        ApiBody apiBody = new ApiBody();

        apiBody.setApiId(webServiceParamInfo.getApiId());
        apiBody.setCreateDate(System.currentTimeMillis());
        apiBody.setParamName(webServiceParamInfo.getParamName());
        apiBody.setParamType(webServiceParamInfo.getParamType());
        apiBody.setRequired("1");
        apiBody.setDescription(webServiceParamInfo.getDescription());
        apiBody.setParamTypeId(webServiceParamInfo.getParamTypeId());
        apiBody.setArrayDataTypeId(webServiceParamInfo.getArrayDataTypeId());
        apiBody.setArrayDataTypeName(webServiceParamInfo.getArrayDataTypeName());
        apiBody.setAssociationType(AssociationType.NORMAL.name());
        if (WebServiceParamType.RequestParam.name().equals(webServiceParamInfo.getType())) {
            apiBody.setType(Const.REQUEST_PARAM_TYPE);
        } else if (WebServiceParamType.ResponseParam.name().equals(webServiceParamInfo.getType())) {
            apiBody.setType(Const.RESPONSE_PARAM_TYPE);
        }
        return apiBody;
    }

    public static List<ApiBody> castToApiBody(List<WebServiceParamInfo> webServiceParamInfos) {
        if (CollectionUtils.isEmpty(webServiceParamInfos)) {
            return null;
        }
        List<ApiBody> apiBodyList = new ArrayList<>();
        for (WebServiceParamInfo paramInfo : webServiceParamInfos) {
            apiBodyList.add(castToApiBody(paramInfo));
        }
        return apiBodyList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public long getApiId() {
        return apiId;
    }

    public void setApiId(long apiId) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public int getParamSort() {
        return paramSort;
    }

    public void setParamSort(int paramSort) {
        this.paramSort = paramSort;
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