package org.hango.cloud.dashboard.apiserver.service.impl.permission.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/6/11
 */
public abstract class BaseSpecResourceHandler {

    private static final Logger logger = LoggerFactory.getLogger(BaseSpecResourceHandler.class);

    /**
     * 自定义资源 名称提取
     *
     * @param request
     * @param <R>
     * @return
     */
    public abstract <R> R handle(HttpServletRequest request);


    protected Object getSpecResourceInfo(HttpServletRequest request, String... layers) {
        String requestBody = HttpClientUtil.parseRequestBody(request);
        JSONObject jsonObject = JSON.parseObject(requestBody);
        if (ArrayUtils.isEmpty(layers)) {
            return jsonObject;
        }
        if (jsonObject == null) {
            return StringUtils.EMPTY;
        }
        try {

            JSONObject tmpObject = jsonObject;
            for (int i = 0; i < layers.length; i++) {
                Object o = tmpObject.get(layers[i]);
                if (o == null) {
                    return o;
                }
                if (i + 1 == layers.length) {
                    return o;
                }

                tmpObject = (JSONObject) o;
            }

        } catch (Exception e) {
            logger.error("获取数据失败，错误信息为 {}", e.getMessage());
        }
        return StringUtils.EMPTY;
    }
}
