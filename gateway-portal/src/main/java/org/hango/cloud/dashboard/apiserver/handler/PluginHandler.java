package org.hango.cloud.dashboard.apiserver.handler;

import org.hango.cloud.dashboard.envoy.web.dto.PluginOrderItemDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/9
 */
public abstract class PluginHandler {

    public static final List<String> pluginIgnoreList = new ArrayList<>();

    public static final Map<String, PluginHandler> pluginUseSubNameList = new HashMap<>();

    static {
        //22GA版本插件
        pluginIgnoreList.add("proxy.filters.http.metadatahub");
    }

    static {
        //22GA版本插件
        pluginUseSubNameList.put("proxy.filters.http.rider", new RiderPluginHandler());
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
