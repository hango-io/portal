package org.hango.cloud.envoy.infra.plugin.meta;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/9/6
 */
@Getter
@Setter
public class SchemaInfo {
    private JSONObject inject;
    private List<JSONObject> layouts;
}
