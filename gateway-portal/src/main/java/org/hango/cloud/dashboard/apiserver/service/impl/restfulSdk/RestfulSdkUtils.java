package org.hango.cloud.dashboard.apiserver.service.impl.restfulSdk;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.service.impl.sdk.BodyParameter;
import org.hango.cloud.dashboard.apiserver.service.impl.sdk.SdkConst;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class RestfulSdkUtils {
    @Autowired
    private IApiParamTypeService apiParamTypeService;

    /***
     * 生成api class的name
     * 如果apiInfo有别名，则使用别名作为api class name
     * 如果没有别名，则使用path创建api class name---会导致名称相对较长
     * @param apiInfo
     * @return
     */
    public static String getApiClassName(ApiInfo apiInfo) {
        String uri = apiInfo.getApiPath();
        //如果api具有英文别名，则返回驼峰形式的API别名
        if (StringUtils.isNotBlank(apiInfo.getAliasName())) {
            return StringUtils.capitalize(apiInfo.getAliasName());
        } else if (StringUtils.isNotBlank(uri)) {
            //去除apiPath中特殊字符
            uri = uri.replaceAll("[^a-zA-Z0-9/]", "");
            String[] uriStrings = uri.substring(1).split("/");
            StringBuilder apiNameBuilder = new StringBuilder();
            for (String str : uriStrings) {
                apiNameBuilder.append(StringUtils.capitalize(str.toLowerCase()));
            }
            return StringUtils.capitalize(apiInfo.getApiMethod().toLowerCase()) + apiNameBuilder.toString();
        } else {
            return null;
        }
    }

    //构造Path List
    public static List<BodyParameter> getPathParams(List<BodyParameter> parameterList, String uri) {
        //构造发送真实uri path
        StringBuffer sb = new StringBuffer();
        //正则替换uri中的{}项目
        Matcher mth = Pattern.compile("\\{(.*?)}").matcher(uri);
        while (mth.find()) {
            String val = mth.group(1);
            parameterList.add(new BodyParameter("String", val));
        }
        return parameterList;
    }

    //判断是否是long型
    public static boolean isValidLong(String str) {
        try {
            long _v = Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void getArrayType(ApiBody apiBody) {
        if (apiBody.getArrayDataTypeId() != SdkConst.NORMAL_PARAM_TYPE) {
            ApiParamType apiParamType = apiParamTypeService.listApiParamType(apiBody.getArrayDataTypeId());
            if (apiParamType != null) {
                apiBody.setArrayDataTypeName(apiParamType.getParamType());
            }
        }
    }
}
