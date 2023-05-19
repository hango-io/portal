package org.hango.cloud.gdashboard.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.ISwaggerToMarkDown;
import org.hango.cloud.gdashboard.api.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;


@Service
public class SwaggerToMarkDown implements ISwaggerToMarkDown {

    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IApiBodyService apiBodyService;

    @Override
    public Map<String, String> swaggerToMd(String swagger, long apiId, List<ApiStatusCode> apiStatusCodeList, long id) {

        ApiInfo apiInfo = apiInfoService.getApi(String.valueOf(apiId));

        Map<String, Object> swaggerMap = Maps.newHashMap();
        // 禁用fastjson的循环调用,禁用fastjson自动排序
        swaggerMap = JSONObject.parseObject(swagger, Feature.DisableCircularReferenceDetect, Feature.OrderedField);
        Map<String, Object> pathsMap = (Map) swaggerMap.get("paths");
        Map<String, Object> methodMap = Maps.newHashMap();
        for (Entry<String, Object> entry : pathsMap.entrySet()) {
            methodMap = (Map) ((Map) entry.getValue()).get(StringUtils.lowerCase(apiInfo.getApiMethod()));
        }
        Map<String, Object> definitionsMap = (Map) swaggerMap.get("definitions");


        String methodString = apiInfo.getApiMethod();

        String descriptionString = apiInfo.getDescription();
        if (StringUtils.isBlank(descriptionString)) {
            descriptionString = "暂无描述";
        }
        descriptionString = convertReturnToBr(descriptionString);

        String parametersString = "";
        Stack<String> subParameterStringList = new Stack<>();

        String response200String = "";
        Stack<String> subResponseStringList = new Stack<>();

        String responsesString = "";
        String requestExampleValueString = apiInfo.getRequestExampleValue();
        String responseExampleValueString = apiInfo.getResponseExampleValue();

        List<JSONObject> parametersList = new ArrayList<>();
        parametersList = (List) methodMap.get("parameters");

        boolean flag = false;
        if (!(parametersList == null || parametersList.size() == 0)) {
            // 若请求参数不为空，生成表头
            parametersString += "| 参数名称 | 说明 | 参数类型 | 是否必填 | 备注 |\n";
            parametersString += "| --- | --- | --- | --- | --- |\n";
            for (JSONObject parameter : parametersList) {
                Map<String, Object> parameterMap = (Map) parameter;
                String requiredFlag;
                String detailedType = (String) parameterMap.get("type");
                // 判断参数是否为必须
                if ("true".equals(String.valueOf(parameterMap.get("required")))) {
                    requiredFlag = "是";
                } else {
                    requiredFlag = "否";
                }
                // 当参数类型为integer或number时，细化参数类型为integer或long,float或double
                if ("integer".equals(detailedType)) {
                    if ("int32".equals(parameterMap.get("format"))) {
                        detailedType = "integer";
                    } else if ("int64".equals(parameterMap.get("format"))) {
                        detailedType = "long";
                    }
                }

                if ("number".equals(detailedType)) {
                    if ("double".equals(parameterMap.get("format"))) {
                        detailedType = "double";
                    } else if ("float".equals(parameterMap.get("format"))) {
                        detailedType = "float";
                    }
                }

                if (parameterMap.get("type") != null) {
                    parametersString += "| " + parameterMap.get("name") + " | " + convertReturnToBr((String) parameterMap.get("description")) + " | "
                            + detailedType
                            + " | " + requiredFlag + " | |\n";
                    flag = true;
                } else {
                    Map<String, Object> schemaMap = (Map) parameterMap.get("schema");
                    generateModelString("**Body**", schemaMap, definitionsMap, subParameterStringList, id);
                }
            }

            if (!flag) {
                parametersString = "";
            } else {
                parametersString += "\n";
            }

            while (!subParameterStringList.isEmpty()) {
                String subString = subParameterStringList.pop();
                parametersString += subString;
            }
        } else {
            parametersString = "无请求参数\n";
        }

        // 生成返回参数
        Map<String, JSONObject> responsesMap = (Map) methodMap.get("responses");
        if (!(responsesMap == null)) {
            // 处理返回参数
            Map<String, JSONObject> response200Map = (Map) responsesMap.get("200");
            if (!(response200Map == null)) {
                Map<String, Object> schemaMap = (Map) response200Map.get("schema");
                if (!(schemaMap == null)) {
                    id++;
                    generateModelString("", schemaMap, definitionsMap, subResponseStringList, id);

                    while (!subResponseStringList.isEmpty()) {
                        String subString = subResponseStringList.pop();
                        response200String += subString;
                    }
                } else {
                    response200String += "返回参数被覆盖\n";
                }
            } else {
                response200String = "无返回参数\n";
            }
        } else {
            response200String = "无返回参数\n";
        }

        // 处理状态码
        if (!apiStatusCodeList.isEmpty()) {
            if (apiInfo.getType().equals(Const.API_ACTION_TYPE)) {
                responsesString += "| 错误码（Code） | 错误提示（Message） | http status code | 说明 |\n";
                responsesString += "|---|---|---|---|\n";
                for (ApiStatusCode apiStatusCode : apiStatusCodeList) {

                    responsesString += "| " + apiStatusCode.getErrorCode() + " | " + apiStatusCode.getMessage() + " | "
                            + apiStatusCode.getStatusCode() + " | " + convertReturnToBr((String) apiStatusCode.getDescription()) + " |\n";
                }
            } else {
                responsesString += "| http status code | 说明 |\n";
                responsesString += "|---|---|\n";
                for (ApiStatusCode apiStatusCode : apiStatusCodeList) {

                    responsesString += "| "
                            + apiStatusCode.getStatusCode() + " | " + convertReturnToBr((String) apiStatusCode.getDescription()) + " |\n";
                }
            }
        } else {
            responsesString = "无状态码\n";
        }


//		String markDownDoc = "### 名称\n" + apiInfo.getApiName() + "\n";
        String markDownDoc = "";
        //FIXME 删除
//        markDownDoc += "### 所属服务\n" + apiInfo.getServiceName() + "\n";
        StringBuilder url = new StringBuilder();
        url.append(apiInfo.getApiPath());
        List<ApiBody> bodyList = apiBodyService.getBody(apiId, Const.QUERYSTRING_PARAM_TYPE);
        if (!CollectionUtils.isEmpty(bodyList)) {
            if (!url.toString().contains("?")) {
                url.append("?");
            }
            bodyList.forEach(apiBody -> {
                if (url.toString().endsWith("?")) {
                    url.append(apiBody.getParamName()).append("=");
                } else {
                    url.append("&").append(apiBody.getParamName()).append("=");
                }
            });
        }
        markDownDoc += "### 请求url\n" + url.toString() + "\n";

        markDownDoc += "### Method\n";
        markDownDoc += methodString + "\n";
        markDownDoc += "### 描述\n";
        markDownDoc += descriptionString + "\n";
        markDownDoc += "### 请求参数\n";
        markDownDoc += parametersString;
        markDownDoc += "### 返回参数\n";
        markDownDoc += response200String;
        markDownDoc += "### 状态码\n";
        markDownDoc += responsesString;
        markDownDoc += "### 请求示例\n" + "```json\n";
        markDownDoc += requestExampleValueString + "\n" + "```" + "\n";
        markDownDoc += "### 返回示例\n" + "```json\n";
        markDownDoc += responseExampleValueString + "\n" + "```" + "\n";

        Map<String, String> markDownMap = Maps.newHashMap();
        markDownMap.put("id", String.valueOf(id));
        markDownMap.put("markDownDoc", markDownDoc);
        return markDownMap;
    }

    @Override
    public void generateModelString(String subParameterName, Map<String, Object> modelMap, Map<String, Object> definitionsMap,
                                    Stack<String> subParameterStringList, long id) {
        String subString = "<span id=" + id + ">" + subParameterName + "</span>\n";
        if (modelMap != null && modelMap.size() != 0) {
            List<String> requiredList = modelMap.get("required") != null ? (List) modelMap.get("required") : null;
            Map<String, JSONObject> propertiesMap = modelMap.get("properties") != null ? (Map) modelMap.get("properties") : null;

            if (!CollectionUtils.isEmpty(propertiesMap)) {
                subString += "| 参数名称 | 说明 | 参数类型 | 是否必填 | 备注 |\n";
                subString += "| --- | --- | --- | --- | --- |\n";
                subString = generateSubString(requiredList, propertiesMap, subString, definitionsMap, subParameterStringList, id);
            } else if (modelMap.get("type") instanceof String && "array".equalsIgnoreCase((String) modelMap.get("type"))) {
                Map<String, Object> jsonObjectMap = Maps.newHashMap();
                jsonObjectMap.put("items", modelMap.get("items"));
                JSONObject resultJson = new JSONObject(jsonObjectMap);
                Map<String, JSONObject> tmpMap = Maps.newHashMap();
                tmpMap.put(Const.BLANK_ARRAY_CONST, resultJson);
                for (Entry<String, JSONObject> entry : tmpMap.entrySet()) {
                    subString += "| 参数名称 | 说明 | 参数类型 | 是否必填 | 备注 |\n";
                    subString += "| --- | --- | --- | --- | --- |\n";
                    subString = generateArrayString(entry, subString, "是", definitionsMap, subParameterStringList, id);
                }
            } else if (modelMap.get("type") != null) {
                //基本类型处理，不包含key的基本类型
                subString += "| 参数名称 | 说明 | 参数类型  | 备注 |\n";
                subString += "| --- | --- | --- | --- | --- |\n";
                String format = (String) modelMap.get("format");
                String type = (String) modelMap.get("type");
                if ("integer".equals(type)) {
                    if ("int32".equals(format)) {
                        type = "integer";
                    } else if ("int64".equals(format)) {
                        type = "long";
                    }
                }
                if ("number".equals(type)) {
                    if ("double".equals(format)) {
                        type = "double";
                    } else if ("float".equals(format)) {
                        type = "float";
                    }
                }
                subString += "|  | " + modelMap.get("description") + " | " + type + "|  |\n";
            }

        } else {
            subString += "\n该模型没有详细信息\n";
        }

        subString += "\n";
        subParameterStringList.add(subString);
    }

    private String generateSubString(List<String> requiredList, Map<String, JSONObject> propertiesMap, String subString, Map<String, Object> definitionsMap,
                                     Stack<String> subParameterStringList, long id) {
        for (Entry<String, JSONObject> entry : propertiesMap.entrySet()) {

            String requiredFlag;
            String detailedType = (String) entry.getValue().get("type");
            if (!(requiredList == null)) {
                if (requiredList.contains(entry.getKey())) {
                    requiredFlag = "是";
                } else {
                    requiredFlag = "否";
                }
            } else {
                requiredFlag = "否";
            }

            if ("integer".equals(detailedType)) {
                if ("int32".equals(entry.getValue().get("format"))) {
                    detailedType = "integer";
                } else if ("int64".equals(entry.getValue().get("format"))) {
                    detailedType = "long";
                }
            }

            if ("number".equals(detailedType)) {
                if ("double".equals(entry.getValue().get("format"))) {
                    detailedType = "double";
                } else if ("float".equals(entry.getValue().get("format"))) {
                    detailedType = "float";
                }
            }

            if (!(entry.getValue().get("type") == null)) {
                // 参数不是Model类型
                if (!"array".equals(entry.getValue().get("type"))) {
                    // 参数不是Array类型
                    subString += "| " + entry.getKey() + " | " + convertReturnToBr((String) entry.getValue().get("description")) + " | "
                            + detailedType + " | " + requiredFlag + " | |\n";
                } else {
                    // 参数是Array类型
                    subString = generateArrayString(entry, subString, requiredFlag, definitionsMap, subParameterStringList, id);
                }
            } else {
                // 参数是Model类型
                id++;
                String address = (String) ((JSONObject) entry.getValue()).get("$ref");
                String subModelName = address.substring(14);
                String subparameterName = entry.getKey();
                subString += "| [" + subparameterName + "](#" + id + ") | " + convertReturnToBr((String) entry.getValue().get("description"))
                        + " | [" + subModelName + "](#" + id + ")| " + requiredFlag + " | 详细信息见下表 |\n";
                Map<String, Object> subModelMap = (Map) definitionsMap.get(subModelName);
                generateModelString("**" + subModelName + "**", subModelMap, definitionsMap, subParameterStringList, id);
            }
        }
        return subString;
    }

    @Override
    public String generateArrayString(Entry<String, JSONObject> arrayMap, String subString, String requiredFlag, Map<String, Object> definitionsMap,
                                      Stack<String> subParameterStringList, long id) {

        if (!(((JSONObject) arrayMap.getValue().get("items")).get("type") == null)) {
            //Array参数类型为普通类型
            String detailedType = (String) ((JSONObject) arrayMap.getValue().get("items")).get("type");
            ;
            if ("integer".equals(detailedType)) {
                if ("int32".equals(((JSONObject) arrayMap.getValue().get("items")).get("format"))) {
                    detailedType = "integer";
                } else if ("int64".equals(((JSONObject) arrayMap.getValue().get("items")).get("format"))) {
                    detailedType = "long";
                }
            }

            if ("number".equals(detailedType)) {
                if ("double".equals(((JSONObject) arrayMap.getValue().get("items")).get("format"))) {
                    detailedType = "double";
                } else if ("float".equals(((JSONObject) arrayMap.getValue().get("items")).get("format"))) {
                    detailedType = "float";
                }
            }
            if (Const.BLANK_ARRAY_CONST.equalsIgnoreCase(arrayMap.getKey())) {
                subString += "| | " + convertReturnToBr((String) arrayMap.getValue().get("description")) + " | array<span>&lt;</span>"
                        + detailedType + "<span>&gt;</span> | " + requiredFlag + " | |\n";
            } else {
                subString += "| " + arrayMap.getKey() + " | " + convertReturnToBr((String) arrayMap.getValue().get("description")) + " | array<span>&lt;</span>"
                        + detailedType + "<span>&gt;</span> | " + requiredFlag + " | |\n";
            }
        } else {
            // Array参数类型为引用类型
            id++;
            String address = (String) ((JSONObject) arrayMap.getValue().get("items")).get("$ref");
            String modelName = address.substring(14);
            String subParameterName = arrayMap.getKey();
            if (Const.BLANK_ARRAY_CONST.equalsIgnoreCase(subParameterName)) {
                subString += "| | "
                        + convertReturnToBr((String) arrayMap.getValue().get("description")) + " | array[<span>&lt;</span>" + modelName + "<span>&gt;</span>](#" + id + ") | "
                        + requiredFlag
                        + " | 详细信息见下表 |\n";
            } else {
                subString += "| [" + subParameterName + "](#" + id + ") | "
                        + convertReturnToBr((String) arrayMap.getValue().get("description")) + " | array[<span>&lt;</span>" + modelName + "<span>&gt;</span>](#" + id + ") | "
                        + requiredFlag
                        + " | 详细信息见下表 |\n";
            }
            Map<String, Object> modelMap = (Map) definitionsMap.get(modelName);
            generateModelString("**" + modelName + "**", modelMap, definitionsMap, subParameterStringList, id);
        }

        return subString;
    }

    @Override
    public String convertReturnToBr(String description) {
        if (!(description == null || description == "")) {
            description = description.replace("\n", "<br>");
        }

        if (description == "") {
            description = "null";
        }

        return description;
    }
}
