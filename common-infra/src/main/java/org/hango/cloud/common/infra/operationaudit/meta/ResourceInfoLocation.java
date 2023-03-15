package org.hango.cloud.common.infra.operationaudit.meta;

import com.alibaba.fastjson.JSONPath;
import org.hango.cloud.common.infra.base.meta.HttpElement;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/25
 */
public class ResourceInfoLocation {

    /**
     * 资源信息位置， request or response {@link HttpElement#REQUEST} {@link HttpElement#RESPONSE}
     */
    private HttpElement location;

    /**
     * JSON请求体、响应体获取位置 使用{@link JSONPath 解析}
     */
    private String jsonPath;

    public ResourceInfoLocation(HttpElement location, String jsonPath) {
        this.location = location;
        this.jsonPath = jsonPath;
    }

    public HttpElement getLocation() {
        return location;
    }

    public String getJsonPath() {
        return jsonPath;
    }
}
