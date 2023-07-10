package org.hango.cloud.envoy.infra.pluginmanager.handler;


import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/9
 */
public abstract class PluginHandler {

    public static final List<String> pluginIgnoreList = new ArrayList<>();

    static {
        pluginIgnoreList.add("proxy.filters.http.traffic_mark");
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

    public static Boolean pluginFilter(PluginOrderItemDto item){
        return !pluginIgnoreList.contains(item.getName());
    }




}
