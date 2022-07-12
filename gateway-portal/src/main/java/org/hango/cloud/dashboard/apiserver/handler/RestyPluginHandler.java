package org.hango.cloud.dashboard.apiserver.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.envoy.web.dto.PluginOrderItemDto;
import org.springframework.util.CollectionUtils;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/9
 */
public class RestyPluginHandler extends PluginHandler {

    @Override
    public String getName(PluginOrderItemDto item) {
        JSONObject settings = (JSONObject) item.getSettings();
        JSONArray plugins = settings.getJSONArray("plugins");
        if (CollectionUtils.isEmpty(plugins)) {
            return StringUtils.EMPTY;
        }
        JSONObject plugin = plugins.getJSONObject(0);
        return plugin.getString("name");
    }
}
