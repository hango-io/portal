package org.hango.cloud.dashboard.apiserver.util;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数校验工具类
 *
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2017/12/12 上午11:10.
 */
public class ParameterVerification {


    /**
     * 判断apiPath是否合法
     *
     * @param apiPath
     */
    public static boolean isApiPathValid(String apiPath) {
        if (StringUtils.isBlank(apiPath)) {
            return false;
        }

        //apiPath 只能包含字母、数字、/、{、}
        String pattern = "^[0-9A-Za-z\\/\\{\\}\\-\\_\\.]+$";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(apiPath);

        return m.matches();
    }


}
