package org.hango.cloud.gdashboard.api.service;

import com.alibaba.fastjson.JSONObject;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

public interface ISwaggerToMarkDown {

    Map<String, String> swaggerToMd(String swagger, long interfaceId, List<ApiStatusCode> apiStatusCodeList, long id);

    void generateModelString(String parameterName, Map<String, Object> modelMap, Map<String, Object> definitionsMap,
                             Stack<String> subParameterStringList, long id);

    String generateArrayString(Entry<String, JSONObject> arrayMap, String subString, String requiredFlag, Map<String, Object> definitionsMap,
                               Stack<String> subParameterStringList, long id);

    String convertReturnToBr(String description);

}
