package org.hango.cloud.envoy.infra.plugin.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/9
 */
public abstract class PluginHandler {


    public static final Map<String, PluginHandler> PLUGIN_USE_SUB_NAME_MAP = Maps.newHashMap();


    static {
        //21GA版本插件
        PLUGIN_USE_SUB_NAME_MAP.put("com.netease.resty", new PluginHandler() {
            @Override
            public String getName(PluginOrderItemDto item) {
                Object inline = item.getInline();
                if (Objects.isNull(inline)){
                    return StringUtils.EMPTY;
                }
                JSONObject jsonObject = JSON.parseObject(String.valueOf(inline));
                JSONObject settings = jsonObject.getJSONObject("settings");
                JSONArray plugins = settings.getJSONArray("plugins");
                if (CollectionUtils.isEmpty(plugins)) {
                    return StringUtils.EMPTY;
                }
                JSONObject plugin = plugins.getJSONObject(0);
                return plugin.getString("name");
            }
        });
        //22GA版本插件
        PLUGIN_USE_SUB_NAME_MAP.put("proxy.filters.http.rider", new PluginHandler() {
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
        });
    }

    /**
     * 获取插件名称
     *
     * @param item
     * @return
     */
    public String getName(PluginOrderItemDto item) {
        return item.getName();
    }


}
