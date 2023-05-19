package org.hango.cloud.envoy.infra.pluginmanager.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;
import org.springframework.util.CollectionUtils;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/7/6 15:50
 **/
public class RiderPluginHandler extends PluginHandler {

    @Override
    public String getName(PluginOrderItemDto item) {
        String name = item.getName();
        JSONObject inline = (JSONObject) item.getInline();
        if (inline == null) {
            return name;
        }
        JSONObject settings = inline.getJSONObject("settings");
        JSONArray plugins = settings.getJSONArray("plugins");
        if (CollectionUtils.isEmpty(plugins)) {
            return StringUtils.EMPTY;
        }
        JSONObject plugin = plugins.getJSONObject(0);
        return plugin.getString("name");
    }
}
