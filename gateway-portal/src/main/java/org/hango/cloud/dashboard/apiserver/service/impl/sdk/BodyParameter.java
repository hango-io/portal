package org.hango.cloud.dashboard.apiserver.service.impl.sdk;

import org.hango.cloud.gdashboard.api.dto.ApiParamDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;

/**
 * 参数数据结构 用于请求字符串，请求体及响应体
 *
 * @author Hu Yuchao(huyuchao)
 */
public class BodyParameter {
    private String type;
    private String name;

    public BodyParameter(ApiBody apiBody) {
        this.type = typeTranslate(apiBody.getParamType(), apiBody);
        this.name = apiBody.getParamName();
    }

    public BodyParameter(ApiParamDto modelParam) {
        this.type = typeTranslate(modelParam.getParamTypeName(), modelParam);
        this.name = modelParam.getParamName();
    }


    //constructor
    public BodyParameter(String type, String name) {
        this.type = type;
        this.name = name;
    }

    private static String typeTranslate(String databaseType, ApiBody apiBody) {
        if (databaseType.equals("Array")) {
            return "List<" + typeTranslate(apiBody.getArrayDataTypeName(), apiBody) + ">";
        } else if (databaseType.equals("Int")) {
            return "Integer";
        } else {
            return databaseType;
        }
    }

    private static String typeTranslate(String databaseType, ApiParamDto modelPrarm) {
        if (databaseType.equals("Array")) {
            return "List<" + typeTranslate(modelPrarm.getArrayDataTypeName(), modelPrarm) + ">";
        } else if (databaseType.equals("Int")) {
            return "Integer";
        } else {
            return databaseType;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
