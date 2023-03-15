package org.hango.cloud.gdashboard.api.util;

public class CommonUtil {
    public static String getRegexFromApi(String apiPath) {
        //1.去除path中带的参数
        if (apiPath.contains("?")) {
            String pathString[] = apiPath.split("\\?");
            apiPath = pathString[0];
        }
        //2.将path中的{tenantId}替换成*
        String regex = apiPath.replaceAll("\\{[^}]*\\}", "*");

        return regex;
    }
}
