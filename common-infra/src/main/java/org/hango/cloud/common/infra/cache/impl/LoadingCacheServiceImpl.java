package org.hango.cloud.common.infra.cache.impl;

import com.google.common.cache.CacheBuilder;

import com.google.common.cache.CacheLoader;

import com.google.common.cache.LoadingCache;
import org.hango.cloud.common.infra.cache.ICacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import java.util.concurrent.TimeUnit;

/**
 * @author xianyanglin
 * @Description: 走本地缓存
 * @date 2023/3/9 14:56
 */

@Service
@ConditionalOnProperty(name = "spring.redis.sentinel.nodes", matchIfMissing = true)
public class LoadingCacheServiceImpl implements ICacheService {
    private static final Logger logger = LoggerFactory.getLogger(LoadingCacheServiceImpl.class);

    private static final LoadingCache<Object, Object> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(new CacheLoader<Object, Object>() {
                @Override
                public Object load(Object key) throws Exception {
                    return null;
                }
            });

    @Override
    public <K, V> V getValue(K key) {
        try {
            return (V) cache.get(key);
        } catch (ExecutionException e) {
            return null;
        }
    }

    @Override
    public <K, V> void setValue(K key, V value, long expire) {
        cache.put(key, value);
    }

    @Override
    public <K, V> Boolean getLock(K key, V value, long expire) {
        try {
            return cache.asMap().putIfAbsent(key, value) == null;
        } finally {
            if (expire > 0) {
                cache.put(key, value);
            }
        }
    }

    @Override
    public <K> void deleteKey(K key) {
        cache.invalidate(key);
    }

    @Override
    public <K> boolean hasKey(K key) {
        return cache.getIfPresent(key) != null;
    }

}
