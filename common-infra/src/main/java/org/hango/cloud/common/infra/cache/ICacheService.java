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
     * @param <K>
     * @param <V>
     * @return
     */
    <K, V> V getValue(K key);

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     * @param expire
     * @param <K>
     * @param <V>
     */
    <K, V> void setValue(K key, V value, long expire);

    /**
     * 获取锁
     *
     * @param key
     * @param value
     * @param expire
     * @param <K>
     * @param <V>
     * @return
     */
    <K, V> Boolean getLock(K key, V value, long expire);

    /**
     * 删除缓存
     * @param key
     * @param <K>
     */
    <K> void deleteKey(K key);

    /**
     * 判断是否存在对应的key
     * @param key
     * @return
     * @param <K>
     */
    <K> boolean hasKey(K key);
}
