package org.hango.cloud.envoy.infra.pluginmanager.handler;


import com.google.common.collect.Maps;
import org.hango.cloud.envoy.infra.pluginmanager.dto.PluginOrderItemDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hango.cloud.common.infra.base.meta.BaseConst.RIDER_PLUGIN;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/9
 */
public abstract class PluginHandler {

    public static final List<String> pluginIgnoreList = new ArrayList<>();

    public static final Map<String, PluginHandler> pluginUseSubNameList = Maps.newHashMap();

    static {
        pluginIgnoreList.add("proxy.filters.http.traffic_mark");
        //todo sx 暂时屏蔽
        pluginIgnoreList.add(RIDER_PLUGIN);
    }

    static {
//        pluginUseSubNameList.put(RIDER_PLUGIN, new RiderPluginHandler());
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
