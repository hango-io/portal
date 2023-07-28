package org.hango.cloud.envoy.infra.base.util;

import org.hango.cloud.common.infra.base.meta.HttpClientResponse;

/**
 * @Author zhufengwei
 * @Date 2023/6/6
 */
public class LogUtil {

    public static String buildPlaneErrorLog(HttpClientResponse response){
        return String.format("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:%s,errMsg:%s",  response.getStatusCode(), response.getResponseBody());
    }

    public static String buildPlaneExceptionLog(){
        return "调用api-plane发布接口异常";
    }
}