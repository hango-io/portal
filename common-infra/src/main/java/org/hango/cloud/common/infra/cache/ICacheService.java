package org.hango.cloud.common.infra.cache;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/1/5
 */
public interface ICacheService {

    /**
     * 获取缓存值
     *
     * @param key
     */
    String getValue(String key);

    /**
     * 设置缓存
     *
     * @param key   缓存key
     * @param value  缓存value
     * @param expire 过期时间(s)
     */
    void setValue(String key, String value, Long expire);



    /**
     * 判断是否存在对应的key
     * @param key
     */
    boolean hasKey(String key);
}
