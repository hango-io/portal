package org.hango.cloud.dashboard.apiserver.service.impl;

import org.hango.cloud.dashboard.apiserver.service.IRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


@Component
public class RedisService implements IRedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    private static final String PROCESS_PREFIX = "process_%s";

    @Resource(name = "redisTemplate")
    private RedisTemplate client;

    @Override
    public <K, V> V getValue(K key) {
        try {
            ValueOperations<K, V> valueOperations = client.opsForValue();
            return valueOperations.get(key);
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return null;
    }

    /**
     * expire in milliseconds
     */
    @Override
    public <K, V> void setValue(K key, V value, long expire) {
        try {
            ValueOperations<K, V> valueOperations = client.opsForValue();
            valueOperations.set(key, value, expire, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public <K, V> Boolean getLock(K key, V value, long expire) {
        try {
            // in redis transaction.
            SessionCallback<Boolean> sessionCallback = new SessionCallback<Boolean>() {
                @Override
                public Boolean execute(RedisOperations operations) throws DataAccessException {
                    Boolean result = false;
                    ValueOperations valueOperation = operations.opsForValue();
                    logger.debug("获取锁{}，执行setnx操作之前", key);
                    if (valueOperation.setIfAbsent(key, value)) {
                        logger.debug("获取锁{}，执行setnx操作之后，设置expire之前", key);
                        result = operations.expire(key, expire, TimeUnit.MILLISECONDS);
                        logger.debug("获取锁{}，执行setnx操作之后，设置expire之后", key);
                        if (!result) {
                            logger.warn("set ttl fail! lockKey = " + key + ", ttl = " + expire + "s");
                        }
                    } else {
                        // 死锁检查，如果出现，进行报警
                        Long expireTime = operations.getExpire(key);
                        logger.debug("key{}, expire time: {}", key, String.valueOf(expireTime));
                        if (expireTime != null && expireTime == -1L) {
                            logger.debug("获取锁{}，发现死锁，进行死锁补偿之前", key);
                            boolean expireResult = operations.expire(key, expire, TimeUnit.MILLISECONDS);
                            logger.debug("获取锁{}，发现死锁，进行死锁补偿之后", key);
                            if (!expireResult) {
                                logger.warn("死锁补偿时，set ttl fail! lockKey = " + key + ", ttl = " + expire + "s");
                            }
                        }
                    }
                    return result;
                }
            };
            return Boolean.valueOf(String.valueOf(client.execute(sessionCallback)));
        } catch (Exception e) {
            logger.warn("redis setLock error.", e);
            throw e;
        }
    }

    @Override
    public <K> void deleteKey(K key) {
        try {
            client.delete(key);
        } catch (Exception e) {
            logger.warn("redis op error.", e);
        }
    }


    @Override
    public <K> boolean hasKey(K key) {
        try {
            return client.hasKey(key);
        } catch (Exception e) {
            logger.warn("redis op error.", e);
        }
        return false;
    }

}
