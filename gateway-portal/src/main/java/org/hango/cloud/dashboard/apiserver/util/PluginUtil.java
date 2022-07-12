package org.hango.cloud.dashboard.apiserver.util;

import java.util.List;

/**
 * @author yutao04
 * @date 2022/2/17 17:32
 */
public class PluginUtil {
    /**
     * 将插件ID字符串转化为插件ID集合
     * 主要用于转化从数据库中取出的ID集合字符串
     *
     * @param pluginIdsString 插件ID集合字符串
     * @return 插件ID集合
     */
    public static List<Long> getPluginIdList(String pluginIdsString) {
        return CommonUtil.splitStringToLongList(pluginIdsString, ",");
    }
}