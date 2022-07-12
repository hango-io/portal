package org.hango.cloud.dashboard.apiserver.service;

import java.util.List;

public interface JsonConvertService<T> {
    /**
     * 将需要导出的数据转为json字符串
     *
     * @param data 需要导出的数据对象
     * @return json字符串
     */
    <T> String exportToJson(T data);

    /**
     * 将json字符串格式的数据转为响应的对象
     *
     * @param data json字符串
     * @param type 数据类型
     * @return 数据对象
     */
    <T> T importFromJson(String data, Class<T> type);

    /**
     * 将json字符串格式的数据转为响应的对象列表
     *
     * @param data json字符串
     * @param type 数据类型
     * @return 数据对象列表
     */
    <T> List<T> importFromJsonArray(String data, Class<T> type);

}
